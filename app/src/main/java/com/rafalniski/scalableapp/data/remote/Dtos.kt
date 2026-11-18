package com.rafalniski.scalableapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoDto(
    val name: String,
    val owner: OwnerDto,
    val description: String? = null,
    val language: String? = null,
    @SerialName("stargazers_count") val stargazersCount: Int,
)

@Serializable
data class OwnerDto(val login: String)

@Serializable
data class CommitDto(val sha: String, val commit: CommitDetailsDto)

@Serializable
data class CommitDetailsDto(val message: String, val author: AuthorDto? = null)

@Serializable
data class AuthorDto(val name: String? = null, val date: String? = null)
