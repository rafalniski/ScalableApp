package com.rafalniski.scalableapp.ui.repolist

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafalniski.scalableapp.R
import com.rafalniski.scalableapp.domain.Repo
import com.rafalniski.scalableapp.ui.components.LoadingView
import com.rafalniski.scalableapp.ui.components.MessageView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoListScreen(
    onRepoClick: (Repo) -> Unit,
    viewModel: RepoListViewModel = viewModel(factory = RepoListViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text(GITHUB_USERNAME) }) }) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (val state = uiState) {
            RepoListUiState.Loading -> LoadingView(modifier)
            RepoListUiState.Error -> MessageView(stringResource(R.string.repo_list_error), modifier)
            is RepoListUiState.Content ->
                if (state.repos.isEmpty()) {
                    MessageView(stringResource(R.string.repo_list_empty), modifier)
                } else {
                    RepoList(state.repos, onRepoClick, modifier)
                }
        }
    }
}

@Composable
private fun RepoList(repos: List<Repo>, onRepoClick: (Repo) -> Unit, modifier: Modifier) {
    LazyColumn(modifier) {
        items(repos, key = { it.name }) { repo ->
            ListItem(
                headlineContent = { Text(repo.name) },
                supportingContent = repo.description?.let { { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis) } },
                trailingContent = { Text(listOfNotNull(repo.language, "★ ${repo.stars}").joinToString(" · ")) },
                modifier = Modifier.clickable { onRepoClick(repo) },
            )
        }
    }
}
