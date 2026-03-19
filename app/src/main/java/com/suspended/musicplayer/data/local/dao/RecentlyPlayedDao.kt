package com.suspended.musicplayer.data.local.dao

import androidx.room.*
import com.suspended.musicplayer.data.local.entity.RecentlyPlayedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {

    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<RecentlyPlayedEntity>>

    @Query("SELECT * FROM recently_played ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayed(limit: Int = 50): Flow<List<RecentlyPlayedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentlyPlayedEntity)

    @Query("SELECT * FROM recently_played WHERE songId = :songId")
    suspend fun getBySongId(songId: Long): RecentlyPlayedEntity?

    @Query("DELETE FROM recently_played")
    suspend fun clearAll()
}
