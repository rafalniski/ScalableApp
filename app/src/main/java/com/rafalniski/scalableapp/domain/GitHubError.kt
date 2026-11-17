package com.rafalniski.scalableapp.domain

sealed class GitHubError : Exception() {
    data object Network : GitHubError()
    data object RateLimited : GitHubError()
    data object Unknown : GitHubError()
}
