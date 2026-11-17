package com.rafalniski.scalableapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafalniski.scalableapp.ui.repodetail.RepoDetailScreen
import com.rafalniski.scalableapp.ui.repolist.RepoListScreen
import kotlinx.serialization.Serializable

@Serializable
data object RepoList

@Serializable
data class RepoDetail(val owner: String, val name: String)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = RepoList) {
        composable<RepoList> {
            RepoListScreen(onRepoClick = { navController.navigate(RepoDetail(it.owner, it.name)) })
        }
        composable<RepoDetail> {
            RepoDetailScreen(onBack = { navController.navigateUp() })
        }
    }
}
