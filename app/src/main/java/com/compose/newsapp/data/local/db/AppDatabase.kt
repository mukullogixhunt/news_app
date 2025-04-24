package com.compose.newsapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.compose.newsapp.core.util.Constants
import com.compose.newsapp.data.local.db.converter.Converters
import com.compose.newsapp.data.local.db.dao.ArticleDao
import com.compose.newsapp.data.local.db.dao.RemoteKeyDao
import com.compose.newsapp.data.local.entity.ArticleEntity
import com.compose.newsapp.data.local.entity.RemoteKeyEntity

@Database(
    entities = [ArticleEntity::class, RemoteKeyEntity::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false // Set to true for schema migration planning in production
)
@TypeConverters(Converters::class) // Register converters
abstract class AppDatabase : RoomDatabase(){
    abstract fun articleDao(): ArticleDao
    abstract fun remoteKeyDao(): RemoteKeyDao

    // Companion object for Singleton pattern (though Hilt handles this)
    // companion object {
    //     @Volatile
    //     private var INSTANCE: AppDatabase? = null
    //     fun getInstance(context: Context): AppDatabase { ... }
    // }
}