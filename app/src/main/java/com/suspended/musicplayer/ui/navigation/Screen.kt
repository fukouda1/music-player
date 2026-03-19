package com.suspended.musicplayer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Library : Screen("library", "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
    data object Search : Screen("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    data object NowPlaying : Screen("now_playing", "Now Playing")
    data object AlbumDetail : Screen("album/{albumId}", "Album") {
        fun createRoute(albumId: Long) = "album/$albumId"
    }
    data object ArtistDetail : Screen("artist/{artistName}", "Artist") {
        fun createRoute(artistName: String) = "artist/$artistName"
    }
    data object PlaylistDetail : Screen("playlist/{playlistId}", "Playlist") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    data object GenreDetail : Screen("genre/{genreId}/{genreName}", "Genre") {
        fun createRoute(genreId: Long, genreName: String) = "genre/$genreId/$genreName"
    }

    companion object {
        val bottomNavItems = listOf(Home, Search, Library, Settings)
    }
}
