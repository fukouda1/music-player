package com.suspended.musicplayer.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.suspended.musicplayer.domain.model.Song
import com.suspended.musicplayer.ui.components.AlbumCard
import com.suspended.musicplayer.ui.components.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (List<Song>, Int) -> Unit,
    onAlbumClick: (Long) -> Unit,
    favoriteIds: Set<Long>,
    currentSongId: Long?,
    onFavoriteClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Good ${getGreeting()}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Shuffle all button
        if (uiState.allSongs.isNotEmpty()) {
            item {
                Button(
                    onClick = {
                        val shuffled = uiState.allSongs.shuffled()
                        onSongClick(shuffled, 0)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shuffle All (${uiState.allSongs.size} songs)")
                }
            }
        }

        // Recently Played
        if (uiState.recentSongs.isNotEmpty()) {
            item {
                SectionHeader("Recently Played")
            }
            items(uiState.recentSongs.take(5), key = { "recent_${it.id}" }) { song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentSongId,
                    isFavorite = song.id in favoriteIds,
                    onSongClick = {
                        onSongClick(uiState.recentSongs, uiState.recentSongs.indexOf(song))
                    },
                    onFavoriteClick = { onFavoriteClick(song.id) }
                )
            }
        }

        // Albums
        if (uiState.recentAlbums.isNotEmpty()) {
            item {
                SectionHeader("Albums")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.recentAlbums, key = { it.id }) { album ->
                        AlbumCard(
                            title = album.name,
                            subtitle = album.artist,
                            artworkUri = album.artworkUri,
                            onClick = { onAlbumClick(album.id) }
                        )
                    }
                }
            }
        }

        // Most Played
        if (uiState.mostPlayedSongs.isNotEmpty()) {
            item {
                SectionHeader("Most Played")
            }
            items(uiState.mostPlayedSongs.take(5), key = { "most_${it.id}" }) { song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentSongId,
                    isFavorite = song.id in favoriteIds,
                    onSongClick = {
                        onSongClick(uiState.mostPlayedSongs, uiState.mostPlayedSongs.indexOf(song))
                    },
                    onFavoriteClick = { onFavoriteClick(song.id) }
                )
            }
        }

        // All Songs header
        if (uiState.allSongs.isNotEmpty()) {
            item {
                SectionHeader("All Songs")
            }
            itemsIndexed(uiState.allSongs, key = { _, s -> "all_${s.id}" }) { index, song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentSongId,
                    isFavorite = song.id in favoriteIds,
                    onSongClick = { onSongClick(uiState.allSongs, index) },
                    onFavoriteClick = { onFavoriteClick(song.id) }
                )
            }
        }

        // Loading
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Empty state
        if (!uiState.isLoading && uiState.allSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No music found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add music files to your device to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Morning"
        hour < 17 -> "Afternoon"
        else -> "Evening"
    }
}
