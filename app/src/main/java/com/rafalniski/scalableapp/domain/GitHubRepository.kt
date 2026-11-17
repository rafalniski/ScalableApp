package com.rafalniski.scalableapp.domain

/** Throws [GitHubError] on failure. */
interface GitHubRepository {
    suspend fun getRepos(username: String): List<Repo>
    suspend fun getCommits(owner: String, repo: String): List<Commit>
}
