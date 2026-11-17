package com.rafalniski.scalableapp.domain

import java.time.YearMonth
import java.time.ZoneOffset

data class MonthlyCommitCount(val month: YearMonth, val count: Int)

/** Counts commits per month (UTC), returning only months present in the data, oldest first. */
fun aggregateByMonth(commits: List<Commit>): List<MonthlyCommitCount> =
    commits
        .groupingBy { YearMonth.from(it.date.atZone(ZoneOffset.UTC)) }
        .eachCount()
        .map { (month, count) -> MonthlyCommitCount(month, count) }
        .sortedBy { it.month }
