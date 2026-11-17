package com.rafalniski.scalableapp.ui.repodetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafalniski.scalableapp.R
import com.rafalniski.scalableapp.domain.Commit
import com.rafalniski.scalableapp.ui.components.LoadingView
import com.rafalniski.scalableapp.ui.components.MessageView
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailScreen(
    viewModel: RepoDetailViewModel = viewModel(factory = RepoDetailViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text(viewModel.repoName) }) }) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (val state = uiState) {
            RepoDetailUiState.Loading -> LoadingView(modifier)
            RepoDetailUiState.Error -> MessageView(stringResource(R.string.repo_detail_error), modifier)
            is RepoDetailUiState.Content ->
                if (state.commits.isEmpty()) {
                    MessageView(stringResource(R.string.repo_detail_empty), modifier)
                } else {
                    CommitList(state.commits, modifier)
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
