package com.suspended.musicplayer.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.suspended.musicplayer.domain.model.Album
import com.suspended.musicplayer.domain.model.Artist
import com.suspended.musicplayer.domain.model.Genre
import com.suspended.musicplayer.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val artworkUri = Uri.parse("content://media/external/audio/albumart")

    suspend fun scanSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection, projection, selection, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )
                val albumArtUri = ContentUris.withAppendedId(artworkUri, albumId)

                songs.add(
                    Song(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown Artist",
                        album = cursor.getString(albumCol) ?: "Unknown Album",
                        albumId = albumId,
                        duration = cursor.getLong(durationCol),
                        uri = contentUri,
                        artworkUri = albumArtUri,
                        trackNumber = cursor.getInt(trackCol),
                        year = cursor.getInt(yearCol),
                        genre = "",
                        dateAdded = cursor.getLong(dateAddedCol),
                        size = cursor.getLong(sizeCol),
                        path = cursor.getString(dataCol) ?: ""
                    )
                )
            }
        }
        songs
    }

    suspend fun scanAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()

        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR
        )

        context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Audio.Albums.ALBUM} ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val countCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                albums.add(
                    Album(
                        id = id,
                        name = cursor.getString(nameCol) ?: "Unknown Album",
                        artist = cursor.getString(artistCol) ?: "Unknown Artist",
                        artworkUri = ContentUris.withAppendedId(artworkUri, id),
                        songCount = cursor.getInt(countCol),
                        year = cursor.getInt(yearCol)
                    )
                )
            }
        }
        albums
    }

    suspend fun scanArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<Artist>()

        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )

        context.contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Audio.Artists.ARTIST} ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val albumCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val songCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)

            while (cursor.moveToNext()) {
                artists.add(
                    Artist(
                        id = cursor.getLong(idCol),
                        name = cursor.getString(nameCol) ?: "Unknown Artist",
                        albumCount = cursor.getInt(albumCountCol),
                        songCount = cursor.getInt(songCountCol)
                    )
                )
            }
        }
        artists
    }

    suspend fun scanGenres(): List<Genre> = withContext(Dispatchers.IO) {
        val genres = mutableListOf<Genre>()

        val projection = arrayOf(
            MediaStore.Audio.Genres._ID,
            MediaStore.Audio.Genres.NAME
        )

        context.contentResolver.query(
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Audio.Genres.NAME} ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val songCount = getGenreSongCount(id)
                if (songCount > 0) {
                    genres.add(
                        Genre(
                            id = id,
                            name = cursor.getString(nameCol) ?: "Unknown",
                            songCount = songCount
                        )
                    )
                }
            }
        }
        genres
    }

    private fun getGenreSongCount(genreId: Long): Int {
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
        return context.contentResolver.query(uri, null, null, null, null)?.use {
            it.count
        } ?: 0
    }

    suspend fun getSongsForAlbum(albumId: Long): List<Song> {
        return scanSongs().filter { it.albumId == albumId }.sortedBy { it.trackNumber }
    }

    suspend fun getSongsForArtist(artistName: String): List<Song> {
        return scanSongs().filter { it.artist.equals(artistName, ignoreCase = true) }
    }

    suspend fun getSongsForGenre(genreId: Long): List<Song> = withContext(Dispatchers.IO) {
        val songIds = mutableListOf<Long>()
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
        context.contentResolver.query(
            uri,
            arrayOf(MediaStore.Audio.Genres.Members.AUDIO_ID),
            null, null, null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.AUDIO_ID)
            while (cursor.moveToNext()) {
                songIds.add(cursor.getLong(idCol))
            }
        }
        scanSongs().filter { it.id in songIds }
    }
}
