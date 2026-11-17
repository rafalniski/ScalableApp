package com.rafalniski.scalableapp.data

import com.rafalniski.scalableapp.data.remote.CommitDto
import com.rafalniski.scalableapp.data.remote.GitHubApi
import com.rafalniski.scalableapp.data.remote.RepoDto
import com.rafalniski.scalableapp.domain.Commit
import com.rafalniski.scalableapp.domain.GitHubError
import com.rafalniski.scalableapp.domain.GitHubRepository
import com.rafalniski.scalableapp.domain.Repo
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant

class GitHubRepositoryImpl(private val api: GitHubApi) : GitHubRepository {

    override suspend fun getRepos(username: String): List<Repo> =
        request { api.getRepos(username) }.map { it.toDomain() }

    override suspend fun getCommits(owner: String, repo: String): List<Commit> =
        request {
            try {
                api.getCommits(owner, repo)
            } catch (e: HttpException) {
                // GitHub responds with 409 Conflict for a repository without commits.
                if (e.code() == HTTP_CONFLICT) emptyList() else throw e
            }
        }.map { it.toDomain() }

    private suspend fun <T> request(block: suspend () -> T): T =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw GitHubError.Network
        } catch (e: HttpException) {
            throw if (e.isRateLimited()) GitHubError.RateLimited else GitHubError.Unknown
        } catch (e: Exception) {
            throw GitHubError.Unknown
        }

    private fun HttpException.isRateLimited(): Boolean =
        code() == HTTP_TOO_MANY_REQUESTS ||
            (code() == HTTP_FORBIDDEN && response()?.headers()?.get("X-RateLimit-Remaining") == "0")

    private fun RepoDto.toDomain() = Repo(owner = owner.login, name = name)

    private fun CommitDto.toDomain() = Commit(
        sha = sha,
        message = commit.message.lineSequence().first(),
        author = commit.author.name,
        date = Instant.parse(commit.author.date),
    )

    private companion object {
        const val HTTP_FORBIDDEN = 403
        const val HTTP_CONFLICT = 409
        const val HTTP_TOO_MANY_REQUESTS = 429
    }
}
