package com.rafalniski.scalableapp.ui.repodetail

import com.rafalniski.scalableapp.FakeGitHubRepository
import com.rafalniski.scalableapp.MainDispatcherRule
import com.rafalniski.scalableapp.domain.Commit
import com.rafalniski.scalableapp.domain.MonthlyCommitCount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class RepoDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGitHubRepository()

    private val january = commitAt("2024-01-10T12:00:00Z")
    private val february = commitAt("2024-02-10T12:00:00Z")

    @Test
    fun `shows error when loading fails`() = runTest {
        repository.error = IOException()

        val viewModel = collectedViewModel()

        assertEquals(RepoDetailUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `shows no month for a repository without commits`() = runTest {
        val viewModel = collectedViewModel()

        assertEquals(RepoDetailUiState.Content(emptyList(), null, 0), viewModel.uiState.value)
    }

    @Test
    fun `starts with the oldest month`() = runTest {
        repository.commits = listOf(february, january, january)

        val viewModel = collectedViewModel()

        assertEquals(MonthlyCommitCount(YearMonth.of(2024, 1), 2), viewModel.currentMonth())
        assertEquals(2, (viewModel.uiState.value as RepoDetailUiState.Content).maxCount)
    }

    @Test
    fun `moves to the next month every 1,5 s and wraps around`() = runTest {
        repository.commits = listOf(january, february)
        val viewModel = collectedViewModel()

        nextMonth()
        assertEquals(YearMonth.of(2024, 2), viewModel.currentMonth()?.month)

        nextMonth()
        assertEquals(YearMonth.of(2024, 1), viewModel.currentMonth()?.month)
    }

    @Test
    fun `stays on the only month`() = runTest {
        repository.commits = listOf(january)
        val viewModel = collectedViewModel()

        nextMonth()

        assertEquals(YearMonth.of(2024, 1), viewModel.currentMonth()?.month)
    }

    private fun TestScope.collectedViewModel(): RepoDetailViewModel {
        val viewModel = RepoDetailViewModel(repository, "owner", "repo")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun TestScope.nextMonth() {
        advanceTimeBy(1500)
        runCurrent()
    }

    private fun RepoDetailViewModel.currentMonth() =
        (uiState.value as RepoDetailUiState.Content).currentMonth

    private fun commitAt(date: String) =
        Commit(sha = date, message = "", author = "", date = Instant.parse(date))
}
