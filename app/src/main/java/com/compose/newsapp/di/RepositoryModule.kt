package com.compose.newsapp.di

import com.compose.newsapp.data.repository.NewsRepositoryImpl
import com.compose.newsapp.data.repository.SettingsRepositoryImpl
import com.compose.newsapp.domain.repository.NewsRepository
import com.compose.newsapp.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Repositories often live for the app's lifetime
abstract class RepositoryModule {


    @Binds
    @Singleton // Bind the Impl to the Interface as a Singleton
    abstract fun bindNewsRepository(
        newsRepositoryImpl: NewsRepositoryImpl
    ): NewsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

}