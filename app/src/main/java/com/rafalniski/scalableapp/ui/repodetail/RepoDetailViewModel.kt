package com.rafalniski.scalableapp.ui.repodetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.rafalniski.scalableapp.ScalableApplication
import com.rafalniski.scalableapp.domain.Commit
import com.rafalniski.scalableapp.domain.GitHubRepository
import com.rafalniski.scalableapp.ui.RepoDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RepoDetailUiState {
    data object Loading : RepoDetailUiState
    data object Error : RepoDetailUiState
    data class Content(val commits: List<Commit>) : RepoDetailUiState
}

class RepoDetailViewModel(
    repository: GitHubRepository,
    owner: String,
    val repoName: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RepoDetailUiState>(RepoDetailUiState.Loading)
    val uiState: StateFlow<RepoDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = runCatching { repository.getCommits(owner, repoName) }.fold(
                onSuccess = { RepoDetailUiState.Content(it) },
                onFailure = { RepoDetailUiState.Error },
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ScalableApplication
                val route = createSavedStateHandle().toRoute<RepoDetail>()
                RepoDetailViewModel(app.container.gitHubRepository, route.owner, route.name)
            }
        }
    }
}
