package com.rafalniski.scalableapp.domain

import java.time.YearMonth
import java.time.ZoneId

fun aggregateByMonth(commits: List<Commit>, zone: ZoneId): List<MonthlyCommitCount> =
    commits
        .groupingBy { YearMonth.from(it.date.atZone(zone)) }
        .eachCount()
        .map { (month, count) -> MonthlyCommitCount(month, count) }
        .sortedBy { it.month }
