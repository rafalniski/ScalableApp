package com.rafalniski.scalableapp.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RepoDto(val name: String, val owner: OwnerDto)

@Serializable
data class OwnerDto(val login: String)

@Serializable
data class CommitDto(val sha: String, val commit: CommitDetailsDto)

@Serializable
data class CommitDetailsDto(val message: String, val author: AuthorDto)

@Serializable
data class AuthorDto(val name: String, val date: String)
