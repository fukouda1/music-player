package com.suspended.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.suspended.musicplayer.ui.components.BottomNavBar
import com.suspended.musicplayer.ui.components.MiniPlayer
import com.suspended.musicplayer.ui.navigation.NavGraph
import com.suspended.musicplayer.ui.navigation.Screen
import com.suspended.musicplayer.ui.screens.settings.SettingsViewModel
import com.suspended.musicplayer.ui.screens.settings.ThemeMode
import com.suspended.musicplayer.ui.theme.MusicPlayerTheme
import com.suspended.musicplayer.ui.viewmodel.PlayerViewModel
import com.suspended.musicplayer.util.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsState()

            val darkTheme = when (settings.darkMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> null
            }

            MusicPlayerTheme(
                darkTheme = darkTheme ?: androidx.compose.foundation.isSystemInDarkTheme(),
                dynamicColor = settings.dynamicColors
            ) {
                MusicPlayerRoot()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MusicPlayerRoot() {
    val permissionState = rememberPermissionState(PermissionHelper.musicPermission)

    if (permissionState.status.isGranted) {
        MainContent()
    } else {
        PermissionScreen(
            onRequestPermission = { permissionState.launchPermissionRequest() }
        )
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }
    val showMiniPlayer = playbackState.currentSong != null && currentRoute != Screen.NowPlaying.route

    Scaffold(
        bottomBar = {
            Column {
                // Mini player
                AnimatedVisibility(
                    visible = showMiniPlayer,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    MiniPlayer(
                        playbackState = playbackState,
                        onPlayerClick = {
                            navController.navigate(Screen.NowPlaying.route) {
                                launchSingleTop = true
                            }
                        },
                        onPlayPauseClick = playerViewModel::playPause,
                        onNextClick = playerViewModel::seekToNext
                    )
                }

                // Bottom navigation
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    BottomNavBar(navController = navController)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavGraph(
                navController = navController,
                playerViewModel = playerViewModel
            )
        }
    }
}

@Composable
fun PermissionScreen(onRequestPermission: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "Music Access Required",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This app needs access to your music files to play them. Please grant the permission to continue.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}
