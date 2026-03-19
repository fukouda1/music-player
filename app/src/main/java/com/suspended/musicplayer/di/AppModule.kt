package com.suspended.musicplayer.di

import android.content.Context
import androidx.room.Room
import com.suspended.musicplayer.data.local.MusicDatabase
import com.suspended.musicplayer.data.local.dao.FavoriteDao
import com.suspended.musicplayer.data.local.dao.PlaylistDao
import com.suspended.musicplayer.data.local.dao.RecentlyPlayedDao
import com.suspended.musicplayer.data.repository.MusicRepository
import com.suspended.musicplayer.data.repository.MusicRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "music_player.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePlaylistDao(db: MusicDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideFavoriteDao(db: MusicDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideRecentlyPlayedDao(db: MusicDatabase): RecentlyPlayedDao = db.recentlyPlayedDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
}
