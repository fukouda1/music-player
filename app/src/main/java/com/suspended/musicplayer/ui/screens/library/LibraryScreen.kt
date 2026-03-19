package com.suspended.musicplayer.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.suspended.musicplayer.domain.model.Song
import com.suspended.musicplayer.ui.components.SongItem
import com.suspended.musicplayer.util.formatSongCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSongClick: (List<Song>, Int) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onGenreClick: (Long, String) -> Unit,
    favoriteIds: Set<Long>,
    currentSongId: Long?,
    onFavoriteClick: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("Songs", "Albums", "Artists", "Playlists", "Genres")
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Title
        Text(
            text = "Library",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.setSelectedTab(index) },
                    text = { Text(title) }
                )
            }
        }

        // Content
        when (uiState.selectedTab) {
            0 -> SongsTab(uiState.songs, currentSongId, favoriteIds, onSongClick, onFavoriteClick)
            1 -> AlbumsTab(uiState, onAlbumClick)
            2 -> ArtistsTab(uiState, onArtistClick)
            3 -> PlaylistsTab(uiState, onPlaylistClick, { showCreatePlaylistDialog = true }, viewModel)
            4 -> GenresTab(uiState, onGenreClick)
        }
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }
}

@Composable
private fun SongsTab(
    songs: List<Song>,
    currentSongId: Long?,
    favoriteIds: Set<Long>,
    onSongClick: (List<Song>, Int) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Text(
                text = "${songs.size} songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
            SongItem(
                song = song,
                isPlaying = song.id == currentSongId,
                isFavorite = song.id in favoriteIds,
                onSongClick = { onSongClick(songs, index) },
                onFavoriteClick = { onFavoriteClick(song.id) }
            )
        }
    }
}

@Composable
private fun AlbumsTab(uiState: LibraryUiState, onAlbumClick: (Long) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 120.dp)
    ) {
        items(uiState.albums, key = { it.id }) { album ->
            Card(
                onClick = { onAlbumClick(album.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    AsyncImage(
                        model = album.artworkUri,
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    )
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = album.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistsTab(uiState: LibraryUiState, onArtistClick: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
        items(uiState.artists, key = { it.id }) { artist ->
            ListItem(
                headlineContent = {
                    Text(artist.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = {
                    Text(
                        "${artist.albumCount} albums • ${artist.songCount.formatSongCount()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingContent = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                modifier = Modifier.clickable { onArtistClick(artist.name) }
            )
        }
    }
}

@Composable
private fun PlaylistsTab(
    uiState: LibraryUiState,
    onPlaylistClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    viewModel: LibraryViewModel
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            ListItem(
                headlineContent = { Text("Create Playlist") },
                leadingContent = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                modifier = Modifier.clickable(onClick = onCreateClick)
            )
        }
        items(uiState.playlists, key = { it.id }) { playlist ->
            ListItem(
                headlineContent = {
                    Text(playlist.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = {
                    Text(playlist.songCount.formatSongCount())
                },
                leadingContent = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QueueMusic, contentDescription = null)
                        }
                    }
                },
                trailingContent = {
                    IconButton(onClick = { viewModel.deletePlaylist(playlist.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                modifier = Modifier.clickable { onPlaylistClick(playlist.id) }
            )
        }
    }
}

@Composable
private fun GenresTab(uiState: LibraryUiState, onGenreClick: (Long, String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
        items(uiState.genres, key = { it.id }) { genre ->
            ListItem(
                headlineContent = { Text(genre.name) },
                supportingContent = { Text(genre.songCount.formatSongCount()) },
                leadingContent = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Category, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier.clickable { onGenreClick(genre.id, genre.name) }
            )
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
