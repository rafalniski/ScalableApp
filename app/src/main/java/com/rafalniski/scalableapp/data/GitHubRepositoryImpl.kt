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
            api.getCommits(owner, repo).mapNotNull { it.toDomain() }
        } catch (e: HttpException) {
            if (e.code() == EMPTY_REPOSITORY_STATUS) emptyList() else throw e
        }

    private fun RepoDto.toDomain() = Repo(
        owner = owner.login,
        name = name,
        description = description,
        language = language,
        stars = stargazersCount,
    )

    private fun CommitDto.toDomain(): Commit? {
        val author = commit.author ?: return null
        val authorName = author.name ?: return null
        val authorDate = author.date ?: return null
        return Commit(
            sha = sha,
            message = commit.message.lineSequence().first(),
            author = authorName,
            date = Instant.parse(authorDate),
        )
    }

    private companion object {
        const val EMPTY_REPOSITORY_STATUS = 409
    }
}
