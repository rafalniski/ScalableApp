package com.rafalniski.scalableapp.ui.repodetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafalniski.scalableapp.R
import com.rafalniski.scalableapp.domain.Commit
import com.rafalniski.scalableapp.ui.components.CommitActivityBar
import com.rafalniski.scalableapp.ui.components.LoadingView
import com.rafalniski.scalableapp.ui.components.MessageView
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault())
private val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailScreen(
    onBack: () -> Unit,
    viewModel: RepoDetailViewModel = viewModel(factory = RepoDetailViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.repoName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (val state = uiState) {
            RepoDetailUiState.Loading -> LoadingView(modifier)
            RepoDetailUiState.Error -> MessageView(stringResource(R.string.repo_detail_error), modifier)
            is RepoDetailUiState.Content -> {
                val month = state.currentMonth
                if (month == null) {
                    MessageView(stringResource(R.string.repo_detail_empty), modifier)
                } else {
                    Column(modifier) {
                        CommitActivityBar(
                            monthLabel = monthFormatter.format(month.month),
                            count = month.count,
                            maxCount = state.maxCount,
                            modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                        )
                        CommitList(state.commits, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CommitList(commits: List<Commit>, modifier: Modifier) {
    LazyColumn(modifier) {
        items(commits, key = { it.sha }) { commit ->
            ListItem(
                headlineContent = { Text(commit.message) },
                supportingContent = {
                    Text("${commit.author} · ${dateFormatter.format(commit.date)} · ${commit.sha.take(7)}")
                },
            )
        }
    }
}
