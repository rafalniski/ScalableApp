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
import com.rafalniski.scalableapp.ui.runCatchingCancellable
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
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

sealed interface RepoDetailUiState {
    data object Loading : RepoDetailUiState
    data object Error : RepoDetailUiState
    data class Content(
        val commits: List<Commit>,
        val currentMonth: MonthlyCommitCount?,
        val maxCount: Int,
    ) : RepoDetailUiState
}

class RepoDetailViewModel(
    repository: GitHubRepository,
    owner: String,
    val repoName: String,
) : ViewModel() {

    private val commits = MutableStateFlow<Result<List<Commit>>?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RepoDetailUiState> = commits
        .flatMapLatest { result ->
            when {
                result == null -> flowOf(RepoDetailUiState.Loading)
                result.isFailure -> flowOf(RepoDetailUiState.Error)
                else -> contentFlow(result.getOrThrow())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RepoDetailUiState.Loading)

    init {
        viewModelScope.launch {
            commits.value = runCatchingCancellable { repository.getCommits(owner, repoName) }
        }
    }

    private fun contentFlow(commits: List<Commit>): Flow<RepoDetailUiState> {
        val months = aggregateByMonth(commits, ZoneId.systemDefault())
        val maxCount = months.maxOfOrNull { it.count } ?: 0
        if (months.isEmpty()) return flowOf(RepoDetailUiState.Content(commits, null, maxCount))
        return monthIndices(months.size).map { RepoDetailUiState.Content(commits, months[it], maxCount) }
    }

    private fun monthIndices(monthCount: Int): Flow<Int> = flow {
        var index = 0
        while (true) {
            emit(index)
            delay(MONTH_DISPLAY_MILLIS.milliseconds)
            val isLastMonth = index == monthCount - 1
            index = if (isLastMonth) 0 else index + 1
        }
    }

    companion object {
        private const val MONTH_DISPLAY_MILLIS = 1500L
        private const val STOP_TIMEOUT_MILLIS = 5000L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ScalableApplication
                val route = createSavedStateHandle().toRoute<RepoDetail>()
                RepoDetailViewModel(app.container.gitHubRepository, route.owner, route.name)
            }
        }
    }
}
