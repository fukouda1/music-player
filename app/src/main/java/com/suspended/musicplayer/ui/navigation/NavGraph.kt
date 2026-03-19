package com.suspended.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.suspended.musicplayer.domain.model.Song
import com.suspended.musicplayer.player.PlaybackState
import com.suspended.musicplayer.ui.screens.albums.AlbumDetailScreen
import com.suspended.musicplayer.ui.screens.artists.ArtistDetailScreen
import com.suspended.musicplayer.ui.screens.home.HomeScreen
import com.suspended.musicplayer.ui.screens.library.LibraryScreen
import com.suspended.musicplayer.ui.screens.nowplaying.NowPlayingScreen
import com.suspended.musicplayer.ui.screens.playlists.PlaylistDetailScreen
import com.suspended.musicplayer.ui.screens.search.SearchScreen
import com.suspended.musicplayer.ui.screens.settings.SettingsScreen
import com.suspended.musicplayer.ui.viewmodel.PlayerViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    playerViewModel: PlayerViewModel
) {
    val playbackState by playerViewModel.playbackState.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val favoriteIds by playerViewModel.favoriteIds.collectAsState()
    val currentSongId = playbackState.currentSong?.id

    val onSongClick: (List<Song>, Int) -> Unit = { songs, index ->
        playerViewModel.playSongs(songs, index)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSongClick = onSongClick,
                onAlbumClick = { navController.navigate(Screen.AlbumDetail.createRoute(it)) },
                onPlaylistClick = { navController.navigate(Screen.PlaylistDetail.createRoute(it)) },
                favoriteIds = favoriteIds,
                currentSongId = currentSongId,
                onFavoriteClick = playerViewModel::toggleFavorite
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onSongClick = onSongClick,
                favoriteIds = favoriteIds,
                currentSongId = currentSongId,
                onFavoriteClick = playerViewModel::toggleFavorite
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onSongClick = onSongClick,
                onAlbumClick = { navController.navigate(Screen.AlbumDetail.createRoute(it)) },
                onArtistClick = { navController.navigate(Screen.ArtistDetail.createRoute(it)) },
                onPlaylistClick = { navController.navigate(Screen.PlaylistDetail.createRoute(it)) },
                onGenreClick = { id, name -> navController.navigate(Screen.GenreDetail.createRoute(id, name)) },
                favoriteIds = favoriteIds,
                currentSongId = currentSongId,
                onFavoriteClick = playerViewModel::toggleFavorite
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.NowPlaying.route) {
            val currentSong = playbackState.currentSong
            val isFav = currentSong?.id?.let { it in favoriteIds } ?: false

            NowPlayingScreen(
                playbackState = playbackState,
                currentPosition = currentPosition,
                isFavorite = isFav,
                onBackClick = { navController.popBackStack() },
                onPlayPauseClick = playerViewModel::playPause,
                onNextClick = playerViewModel::seekToNext,
                onPreviousClick = playerViewModel::seekToPrevious,
                onSeek = playerViewModel::seekTo,
                onShuffleClick = playerViewModel::toggleShuffle,
                onRepeatClick = playerViewModel::toggleRepeatMode,
                onFavoriteClick = {
                    currentSong?.id?.let { playerViewModel.toggleFavorite(it) }
                },
                onSpeedClick = playerViewModel::setPlaybackSpeed,
                onSleepTimerClick = { playerViewModel.setSleepTimer(30) }
            )
        }

        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable
            AlbumDetailScreen(
                albumId = albumId,
                onBackClick = { navController.popBackStack() },
                onSongClick = onSongClick,
                favoriteIds = favoriteIds,
                currentSongId = currentSongId,
                onFavoriteClick = playerViewModel::toggleFavorite
            )
        }

        composable(
            route = Screen.ArtistDetail.route,
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: return@composable
            ArtistDetailScreen(
                artistName = artistName,
                onBackClick = { navController.popBackStack() },
                onSongClick = onSongClick,
                favoriteIds = favoriteIds,
                currentSongId = currentSongId,
                onFavoriteClick = playerViewModel::toggleFavorite
            )
        }

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            PlaylistDetailScreen(
                playlistId = playlistId,
                onBackClick = { navController.popBackStack() },
                onSongClick = onSongClick,
                favoriteIds = favoriteIds,
                currentSongId = currentSongId,
                onFavoriteClick = playerViewModel::toggleFavorite
            )
        }

        composable(
            route = Screen.GenreDetail.route,
            arguments = listOf(
                navArgument("genreId") { type = NavType.LongType },
                navArgument("genreName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val genreName = backStackEntry.arguments?.getString("genreName") ?: return@composable
            // Reuse ArtistDetailScreen layout pattern for genre
            ArtistDetailScreen(
                artistName = genreName,
                onBackClick = { navController.popBackStack() },
                onSongClick = onSongClick,
                favoriteIds = favoriteIds,
                currentSongId = currentSongId,
                onFavoriteClick = playerViewModel::toggleFavorite
            )
        }
    }
}
