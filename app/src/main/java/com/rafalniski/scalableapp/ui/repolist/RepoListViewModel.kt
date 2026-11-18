package com.rafalniski.scalableapp.ui.repolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rafalniski.scalableapp.ScalableApplication
import com.rafalniski.scalableapp.domain.GitHubRepository
import com.rafalniski.scalableapp.domain.Repo
import com.rafalniski.scalableapp.ui.runCatchingCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val GITHUB_USERNAME = "mralexgray"

sealed interface RepoListUiState {
    data object Loading : RepoListUiState
    data object Error : RepoListUiState
    data class Content(val repos: List<Repo>) : RepoListUiState
}

class RepoListViewModel(repository: GitHubRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RepoListUiState>(RepoListUiState.Loading)
    val uiState: StateFlow<RepoListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = runCatchingCancellable { repository.getRepos(GITHUB_USERNAME) }.fold(
                onSuccess = { RepoListUiState.Content(it) },
                onFailure = { RepoListUiState.Error },
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ScalableApplication
                RepoListViewModel(app.container.gitHubRepository)
            }
        }
    }
}
