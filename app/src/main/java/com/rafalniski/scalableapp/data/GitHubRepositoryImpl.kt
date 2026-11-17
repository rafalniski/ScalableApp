package com.rafalniski.scalableapp.data

import com.rafalniski.scalableapp.data.remote.CommitDto
import com.rafalniski.scalableapp.data.remote.GitHubApi
import com.rafalniski.scalableapp.data.remote.RepoDto
import com.rafalniski.scalableapp.domain.Commit
import com.rafalniski.scalableapp.domain.GitHubRepository
import com.rafalniski.scalableapp.domain.Repo
import retrofit2.HttpException
import java.time.Instant

class GitHubRepositoryImpl(private val api: GitHubApi) : GitHubRepository {

    override suspend fun getRepos(username: String): List<Repo> =
        api.getRepos(username).map { it.toDomain() }

    override suspend fun getCommits(owner: String, repo: String): List<Commit> =
        try {
            api.getCommits(owner, repo).map { it.toDomain() }
        } catch (e: HttpException) {
            // GitHub responds with 409 Conflict for a repository without commits.
            if (e.code() == HTTP_CONFLICT) emptyList() else throw e
        }

    private fun RepoDto.toDomain() = Repo(owner = owner.login, name = name)

    private fun CommitDto.toDomain() = Commit(
        sha = sha,
        message = commit.message.lineSequence().first(),
        author = commit.author.name,
        date = Instant.parse(commit.author.date),
    )

    private companion object {
        const val HTTP_CONFLICT = 409
    }
}
