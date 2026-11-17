package com.rafalniski.scalableapp.ui.repodetail

import com.rafalniski.scalableapp.FakeGitHubRepository
import com.rafalniski.scalableapp.MainDispatcherRule
import com.rafalniski.scalableapp.domain.Commit
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

class RepoDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGitHubRepository()

    @Test
    fun `shows commits when loading succeeds`() {
        repository.commits = listOf(
            Commit(sha = "abc123", message = "Title", author = "Alex", date = Instant.EPOCH),
        )

        val viewModel = RepoDetailViewModel(repository, "owner", "repo")

        assertEquals(RepoDetailUiState.Content(repository.commits), viewModel.uiState.value)
    }

    @Test
    fun `shows error when loading fails`() {
        repository.error = IOException()

        val viewModel = RepoDetailViewModel(repository, "owner", "repo")

        assertEquals(RepoDetailUiState.Error, viewModel.uiState.value)
    }
}
