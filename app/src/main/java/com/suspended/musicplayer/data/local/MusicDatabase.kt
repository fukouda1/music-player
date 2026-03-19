package com.suspended.musicplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.suspended.musicplayer.data.local.dao.FavoriteDao
import com.suspended.musicplayer.data.local.dao.PlaylistDao
import com.suspended.musicplayer.data.local.dao.RecentlyPlayedDao
import com.suspended.musicplayer.data.local.entity.FavoriteEntity
import com.suspended.musicplayer.data.local.entity.PlaylistEntity
import com.suspended.musicplayer.data.local.entity.PlaylistSongCrossRef
import com.suspended.musicplayer.data.local.entity.RecentlyPlayedEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        FavoriteEntity::class,
        RecentlyPlayedEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
}
