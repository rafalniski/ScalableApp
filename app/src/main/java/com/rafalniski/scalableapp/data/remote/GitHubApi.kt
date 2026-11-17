package com.rafalniski.scalableapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {
    @GET("users/{username}/repos?per_page=100&sort=updated")
    suspend fun getRepos(@Path("username") username: String): List<RepoDto>

    @GET("repos/{owner}/{repo}/commits?per_page=100")
    suspend fun getCommits(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): List<CommitDto>
}
