package com.suspended.musicplayer.data.repository

import com.suspended.musicplayer.data.local.dao.FavoriteDao
import com.suspended.musicplayer.data.local.dao.PlaylistDao
import com.suspended.musicplayer.data.local.dao.RecentlyPlayedDao
import com.suspended.musicplayer.data.local.entity.FavoriteEntity
import com.suspended.musicplayer.data.local.entity.PlaylistEntity
import com.suspended.musicplayer.data.local.entity.PlaylistSongCrossRef
import com.suspended.musicplayer.data.local.entity.RecentlyPlayedEntity
import com.suspended.musicplayer.data.scanner.MediaScanner
import com.suspended.musicplayer.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val mediaScanner: MediaScanner,
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val recentlyPlayedDao: RecentlyPlayedDao
) : MusicRepository {

    private var cachedSongs: List<Song>? = null

    override suspend fun getAllSongs(): List<Song> {
        return cachedSongs ?: mediaScanner.scanSongs().also { cachedSongs = it }
    }

    override suspend fun getSongsForAlbum(albumId: Long): List<Song> {
        return getAllSongs().filter { it.albumId == albumId }.sortedBy { it.trackNumber }
    }

    override suspend fun getSongsForArtist(artistName: String): List<Song> {
        return getAllSongs().filter { it.artist.equals(artistName, ignoreCase = true) }
    }

    override suspend fun getSongsForGenre(genreId: Long): List<Song> {
        return mediaScanner.getSongsForGenre(genreId)
    }

    override suspend fun searchSongs(query: String): List<Song> {
        val lowerQuery = query.lowercase()
        return getAllSongs().filter { song ->
            song.title.lowercase().contains(lowerQuery) ||
                    song.artist.lowercase().contains(lowerQuery) ||
                    song.album.lowercase().contains(lowerQuery)
        }
    }

    override suspend fun getAllAlbums(): List<Album> = mediaScanner.scanAlbums()

    override suspend fun getAllArtists(): List<Artist> = mediaScanner.scanArtists()

    override suspend fun getAllGenres(): List<Genre> = mediaScanner.scanGenres()

    // Playlists
    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                val count = playlistDao.getSongCountForPlaylist(entity.id).first()
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    songCount = count,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun deletePlaylist(id: Long) {
        playlistDao.deletePlaylistById(id)
    }

    override suspend fun renamePlaylist(id: Long, newName: String) {
        playlistDao.getPlaylistById(id)?.let { entity ->
            playlistDao.updatePlaylist(entity.copy(name = newName))
        }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylistById(playlistId, songId)
    }

    override fun getPlaylistSongIds(playlistId: Long): Flow<List<Long>> {
        return playlistDao.getSongIdsForPlaylist(playlistId)
    }

    // Favorites
    override fun getFavoriteIds(): Flow<List<Long>> = favoriteDao.getAllFavoriteIds()

    override fun isFavorite(songId: Long): Flow<Boolean> = favoriteDao.isFavorite(songId)

    override suspend fun toggleFavorite(songId: Long) {
        val isFav = favoriteDao.isFavorite(songId).first()
        if (isFav) {
            favoriteDao.removeFavorite(songId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(songId = songId))
        }
    }

    // Recently Played
    override fun getRecentlyPlayed(limit: Int): Flow<List<Long>> {
        return recentlyPlayedDao.getRecentlyPlayed(limit).map { list ->
            list.map { it.songId }
        }
    }

    override fun getMostPlayed(limit: Int): Flow<List<Long>> {
        return recentlyPlayedDao.getMostPlayed(limit).map { list ->
            list.map { it.songId }
        }
    }

    override suspend fun recordPlay(songId: Long) {
        val existing = recentlyPlayedDao.getBySongId(songId)
        if (existing != null) {
            recentlyPlayedDao.upsert(
                existing.copy(
                    playedAt = System.currentTimeMillis(),
                    playCount = existing.playCount + 1
                )
            )
        } else {
            recentlyPlayedDao.upsert(RecentlyPlayedEntity(songId = songId))
        }
    }
}
