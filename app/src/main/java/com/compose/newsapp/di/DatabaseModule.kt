package com.compose.newsapp.di

import android.content.Context
import androidx.room.Room
import com.compose.newsapp.core.util.Constants
import com.compose.newsapp.data.local.db.AppDatabase
import com.compose.newsapp.data.local.db.dao.ArticleDao
import com.compose.newsapp.data.local.db.dao.RemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            // Add migrations here if needed for production
            // .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(
                     // USE THIS OVERLOAD destructiveMigrations DANGEROUSLY allow schema changes without migration.
                    // You should only use this in development!!
                    // Setting true mimics the old behavior (drops all tables).
                dropAllTables = true
            ) // Simple fallback for development
            .build()
    }

    @Provides
    @Singleton // DAOs are typically stateless, singleton is fine
    fun provideArticleDao(appDatabase: AppDatabase): ArticleDao {
        return appDatabase.articleDao()
    }

    @Provides
    @Singleton
    fun provideRemoteKeyDao(appDatabase: AppDatabase): RemoteKeyDao {
        return appDatabase.remoteKeyDao()
    }
}