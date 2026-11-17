package com.rafalniski.scalableapp.domain

import java.time.YearMonth
import java.time.ZoneOffset

fun aggregateByMonth(commits: List<Commit>): List<MonthlyCommitCount> =
    commits
        .groupingBy { YearMonth.from(it.date.atZone(ZoneOffset.UTC)) }
        .eachCount()
        .map { (month, count) -> MonthlyCommitCount(month, count) }
        .sortedBy { it.month }
