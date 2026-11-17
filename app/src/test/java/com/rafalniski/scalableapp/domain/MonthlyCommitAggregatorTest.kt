package com.rafalniski.scalableapp.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.YearMonth

class MonthlyCommitAggregatorTest {

    @Test
    fun `returns no months for no commits`() {
        assertEquals(emptyList<MonthlyCommitCount>(), aggregateByMonth(emptyList()))
    }

    @Test
    fun `counts commits per month in chronological order`() {
        val commits = listOf(
            commitAt("2024-03-10T12:00:00Z"),
            commitAt("2024-01-05T12:00:00Z"),
            commitAt("2024-03-01T12:00:00Z"),
        )

        val expected = listOf(
            MonthlyCommitCount(YearMonth.of(2024, 1), 1),
            MonthlyCommitCount(YearMonth.of(2024, 3), 2),
        )
        assertEquals(expected, aggregateByMonth(commits))
    }

    @Test
    fun `groups by month in UTC`() {
        val commits = listOf(
            commitAt("2024-01-31T23:59:59Z"),
            commitAt("2024-02-01T00:00:00Z"),
        )

        val expected = listOf(
            MonthlyCommitCount(YearMonth.of(2024, 1), 1),
            MonthlyCommitCount(YearMonth.of(2024, 2), 1),
        )
        assertEquals(expected, aggregateByMonth(commits))
    }

    private fun commitAt(date: String) =
        Commit(sha = date, message = "", author = "", date = Instant.parse(date))
}
