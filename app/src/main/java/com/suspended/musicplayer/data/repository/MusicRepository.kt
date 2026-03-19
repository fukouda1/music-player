package com.suspended.musicplayer.data.repository

import com.suspended.musicplayer.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    // Songs
    suspend fun getAllSongs(): List<Song>
    suspend fun getSongsForAlbum(albumId: Long): List<Song>
    suspend fun getSongsForArtist(artistName: String): List<Song>
    suspend fun getSongsForGenre(genreId: Long): List<Song>
    suspend fun searchSongs(query: String): List<Song>

    // Albums
    suspend fun getAllAlbums(): List<Album>

    // Artists
    suspend fun getAllArtists(): List<Artist>

    // Genres
    suspend fun getAllGenres(): List<Genre>

    // Playlists
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(id: Long)
    suspend fun renamePlaylist(id: Long, newName: String)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    fun getPlaylistSongIds(playlistId: Long): Flow<List<Long>>

    // Favorites
    fun getFavoriteIds(): Flow<List<Long>>
    fun isFavorite(songId: Long): Flow<Boolean>
    suspend fun toggleFavorite(songId: Long)

    // Recently Played
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<Long>>
    fun getMostPlayed(limit: Int = 50): Flow<List<Long>>
    suspend fun recordPlay(songId: Long)
}
