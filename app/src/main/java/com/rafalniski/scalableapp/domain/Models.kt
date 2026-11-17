package com.rafalniski.scalableapp.domain

import java.time.Instant
import java.time.YearMonth

data class Repo(val owner: String, val name: String)

data class Commit(
    val sha: String,
    val message: String,
    val author: String,
    val date: Instant,
)

data class MonthlyCommitCount(val month: YearMonth, val count: Int)
