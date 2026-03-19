package com.suspended.musicplayer.ui.screens.playlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.suspended.musicplayer.domain.model.Song
import com.suspended.musicplayer.ui.components.SongItem
import com.suspended.musicplayer.util.formatSongCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onBackClick: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    favoriteIds: Set<Long>,
    currentSongId: Long?,
    onFavoriteClick: (Long) -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistSongs(playlistId)
    }

    val songs by viewModel.playlistSongs.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add songs")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = songs.size.formatSongCount(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (songs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { onSongClick(songs, 0) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play All")
                            }
                            OutlinedButton(onClick = { onSongClick(songs.shuffled(), 0) }) {
                                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Shuffle")
                            }
                        }
                    }
                }
            }

            if (songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.QueueMusic,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No songs yet", style = MaterialTheme.typography.bodyLarge)
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("Add songs")
                            }
                        }
                    }
                }
            }

            itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentSongId,
                    isFavorite = song.id in favoriteIds,
                    onSongClick = { onSongClick(songs, index) },
                    onFavoriteClick = { onFavoriteClick(song.id) },
                    onMoreClick = { viewModel.removeSongFromPlaylist(playlistId, song.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddSongsDialog(
            availableSongs = allSongs.filter { song -> songs.none { it.id == song.id } },
            onDismiss = { showAddDialog = false },
            onAdd = { songId ->
                viewModel.addSongToPlaylist(playlistId, songId)
            }
        )
    }
}

@Composable
private fun AddSongsDialog(
    availableSongs: List<Song>,
    onDismiss: () -> Unit,
    onAdd: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Songs") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                itemsIndexed(availableSongs) { _, song ->
                    ListItem(
                        headlineContent = { Text(song.title, maxLines = 1) },
                        supportingContent = { Text(song.artist, maxLines = 1) },
                        trailingContent = {
                            IconButton(onClick = { onAdd(song.id) }) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }
                    )
                }
                if (availableSongs.isEmpty()) {
                    item {
                        Text(
                            "No more songs to add",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
