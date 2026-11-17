package com.rafalniski.scalableapp

import com.rafalniski.scalableapp.domain.Commit
import com.rafalniski.scalableapp.domain.GitHubRepository
import com.rafalniski.scalableapp.domain.Repo

class FakeGitHubRepository : GitHubRepository {
    var repos: List<Repo> = emptyList()
    var commits: List<Commit> = emptyList()
    var error: Throwable? = null

    override suspend fun getRepos(username: String): List<Repo> = error?.let { throw it } ?: repos

    override suspend fun getCommits(owner: String, repo: String): List<Commit> =
        error?.let { throw it } ?: commits
}
