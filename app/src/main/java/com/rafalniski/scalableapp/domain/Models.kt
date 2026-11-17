package com.rafalniski.scalableapp.domain

import java.time.Instant

data class Repo(val owner: String, val name: String)

data class Commit(
    val sha: String,
    val message: String,
    val author: String,
    val date: Instant,
)
