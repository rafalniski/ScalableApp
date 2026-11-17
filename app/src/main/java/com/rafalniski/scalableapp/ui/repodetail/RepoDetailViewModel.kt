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
import com.rafalniski.scalableapp.domain.MonthlyCommitCount
import com.rafalniski.scalableapp.domain.aggregateByMonth
import com.rafalniski.scalableapp.ui.RepoDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed interface RepoDetailUiState {
    data object Loading : RepoDetailUiState
    data object Error : RepoDetailUiState
    data class Content(
        val commits: List<Commit>,
        /** Month currently shown by the activity bar; null when there are no commits. */
        val currentMonth: MonthlyCommitCount?,
        val maxCount: Int,
    ) : RepoDetailUiState
}

class RepoDetailViewModel(
    repository: GitHubRepository,
    owner: String,
    val repoName: String,
    private val monthInterval: Duration = 1500.milliseconds,
) : ViewModel() {

    private val commits = MutableStateFlow<Result<List<Commit>>?>(null)

    /** Cycles through months only while collected, i.e. while the screen is visible. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RepoDetailUiState> = commits
        .flatMapLatest { result ->
            when {
                result == null -> flowOf(RepoDetailUiState.Loading)
                result.isFailure -> flowOf(RepoDetailUiState.Error)
                else -> contentFlow(result.getOrThrow())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RepoDetailUiState.Loading)

    init {
        viewModelScope.launch {
            commits.value = runCatching { repository.getCommits(owner, repoName) }
        }
    }

    private fun contentFlow(commits: List<Commit>): Flow<RepoDetailUiState> {
        val months = aggregateByMonth(commits)
        val maxCount = months.maxOfOrNull { it.count } ?: 0
        if (months.isEmpty()) return flowOf(RepoDetailUiState.Content(commits, null, maxCount))
        return monthIndices(months.size).map { RepoDetailUiState.Content(commits, months[it], maxCount) }
    }

    private fun monthIndices(monthCount: Int): Flow<Int> = flow {
        var index = 0
        while (true) {
            emit(index)
            delay(monthInterval)
            index = (index + 1) % monthCount
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
