package com.rafalniski.scalableapp

import com.rafalniski.scalableapp.data.GitHubRepositoryImpl
import com.rafalniski.scalableapp.data.remote.GitHubApi
import com.rafalniski.scalableapp.domain.GitHubRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class AppContainer {

    private val okHttpClient = OkHttpClient.Builder().apply {
        val token = BuildConfig.GITHUB_TOKEN
        if (token.isNotBlank()) {
            addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder().header("Authorization", "Bearer $token").build(),
                )
            }
        }
    }.build()

    private val api: GitHubApi = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(
            Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()),
        )
        .build()
        .create()

    val gitHubRepository: GitHubRepository = GitHubRepositoryImpl(api)
}
