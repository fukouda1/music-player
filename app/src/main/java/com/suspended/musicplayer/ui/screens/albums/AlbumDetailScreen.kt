package com.suspended.musicplayer.ui.screens.albums

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.suspended.musicplayer.domain.model.Song
import com.suspended.musicplayer.ui.components.SongItem
import com.suspended.musicplayer.util.formatSongCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: Long,
    onBackClick: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    favoriteIds: Set<Long>,
    currentSongId: Long?,
    onFavoriteClick: (Long) -> Unit,
    viewModel: AlbumsViewModel = hiltViewModel()
) {
    LaunchedEffect(albumId) {
        viewModel.loadAlbumSongs(albumId)
    }

    val songs by viewModel.albumSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val albumName = songs.firstOrNull()?.album ?: "Album"
    val artistName = songs.firstOrNull()?.artist ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Album header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = songs.firstOrNull()?.artworkUri,
                        contentDescription = albumName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$artistName • ${songs.size.formatSongCount()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { if (songs.isNotEmpty()) onSongClick(songs, 0) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play")
                        }
                        OutlinedButton(onClick = { if (songs.isNotEmpty()) onSongClick(songs.shuffled(), 0) }) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Shuffle")
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentSongId,
                    isFavorite = song.id in favoriteIds,
                    showTrackNumber = true,
                    onSongClick = { onSongClick(songs, index) },
                    onFavoriteClick = { onFavoriteClick(song.id) }
                )
            }
        }
    }
}
