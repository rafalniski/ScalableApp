package com.rafalniski.scalableapp.ui.repolist

import com.rafalniski.scalableapp.FakeGitHubRepository
import com.rafalniski.scalableapp.MainDispatcherRule
import com.rafalniski.scalableapp.domain.Repo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class RepoListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGitHubRepository()

    @Test
    fun `shows repos when loading succeeds`() {
        repository.repos = listOf(Repo(owner = "owner", name = "repo"))

        val viewModel = RepoListViewModel(repository)

        assertEquals(RepoListUiState.Content(repository.repos), viewModel.uiState.value)
    }

    @Test
    fun `shows error when loading fails`() {
        repository.error = IOException()

        val viewModel = RepoListViewModel(repository)

        assertEquals(RepoListUiState.Error, viewModel.uiState.value)
    }
}
