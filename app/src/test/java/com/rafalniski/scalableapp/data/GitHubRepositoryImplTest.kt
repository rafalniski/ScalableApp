package com.rafalniski.scalableapp.data

import com.rafalniski.scalableapp.data.remote.AuthorDto
import com.rafalniski.scalableapp.data.remote.CommitDetailsDto
import com.rafalniski.scalableapp.data.remote.CommitDto
import com.rafalniski.scalableapp.data.remote.GitHubApi
import com.rafalniski.scalableapp.data.remote.OwnerDto
import com.rafalniski.scalableapp.data.remote.RepoDto
import com.rafalniski.scalableapp.domain.Commit
import com.rafalniski.scalableapp.domain.GitHubError
import com.rafalniski.scalableapp.domain.Repo
import kotlinx.coroutines.test.runTest
import okhttp3.Headers.Companion.headersOf
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Instant

class GitHubRepositoryImplTest {

    private val api = FakeGitHubApi()
    private val repository = GitHubRepositoryImpl(api)

    @Test
    fun `maps repos to domain`() = runTest {
        api.repos = listOf(RepoDto(name = "repo", owner = OwnerDto(login = "owner")))

        assertEquals(listOf(Repo(owner = "owner", name = "repo")), repository.getRepos("owner"))
    }

    @Test
    fun `maps commits to domain keeping only the first message line`() = runTest {
        api.commits = listOf(
            CommitDto(
                sha = "abc123",
                commit = CommitDetailsDto(
                    message = "Title\n\nBody",
                    author = AuthorDto(name = "Alex", date = "2024-03-01T10:00:00Z"),
                ),
            ),
        )

        val expected = Commit(
            sha = "abc123",
            message = "Title",
            author = "Alex",
            date = Instant.parse("2024-03-01T10:00:00Z"),
        )
        assertEquals(listOf(expected), repository.getCommits("owner", "repo"))
    }

    @Test
    fun `returns no commits for an empty repository`() = runTest {
        api.error = httpError(409)

        assertEquals(emptyList<Commit>(), repository.getCommits("owner", "repo"))
    }

    @Test
    fun `maps IOException to Network`() = runTest {
        api.error = IOException()

        assertEquals(GitHubError.Network, errorOf { repository.getRepos("owner") })
    }

    @Test
    fun `maps exhausted rate limit to RateLimited`() = runTest {
        api.error = httpError(403, "X-RateLimit-Remaining" to "0")

        assertEquals(GitHubError.RateLimited, errorOf { repository.getRepos("owner") })
    }

    @Test
    fun `maps 429 to RateLimited`() = runTest {
        api.error = httpError(429)

        assertEquals(GitHubError.RateLimited, errorOf { repository.getRepos("owner") })
    }

    @Test
    fun `maps other HTTP errors to Unknown`() = runTest {
        api.error = httpError(403, "X-RateLimit-Remaining" to "42")

        assertEquals(GitHubError.Unknown, errorOf { repository.getCommits("owner", "repo") })
    }

    private suspend fun errorOf(block: suspend () -> Unit): Throwable? =
        runCatching { block() }.exceptionOrNull()

    private fun httpError(code: Int, vararg headers: Pair<String, String>): HttpException {
        val rawResponse = okhttp3.Response.Builder()
            .request(Request.Builder().url("https://api.github.com/").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("Error")
            .headers(headersOf(*headers.flatMap { it.toList() }.toTypedArray()))
            .build()
        return HttpException(Response.error<Any>("".toResponseBody(), rawResponse))
    }

    private class FakeGitHubApi : GitHubApi {
        var repos: List<RepoDto> = emptyList()
        var commits: List<CommitDto> = emptyList()
        var error: Throwable? = null

        override suspend fun getRepos(username: String): List<RepoDto> =
            error?.let { throw it } ?: repos

        override suspend fun getCommits(owner: String, repo: String): List<CommitDto> =
            error?.let { throw it } ?: commits
    }
}
