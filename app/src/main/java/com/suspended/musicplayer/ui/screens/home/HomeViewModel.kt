package com.suspended.musicplayer.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suspended.musicplayer.data.repository.MusicRepository
import com.suspended.musicplayer.domain.model.Album
import com.suspended.musicplayer.domain.model.Playlist
import com.suspended.musicplayer.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentSongs: List<Song> = emptyList(),
    val mostPlayedSongs: List<Song> = emptyList(),
    val recentAlbums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val allSongs: List<Song> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observePlaylists()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val songs = repository.getAllSongs()
            val albums = repository.getAllAlbums()

            repository.getRecentlyPlayed(10).combine(
                repository.getMostPlayed(10)
            ) { recentIds, mostPlayedIds ->
                val recentSongs = recentIds.mapNotNull { id -> songs.find { it.id == id } }
                val mostPlayedSongs = mostPlayedIds.mapNotNull { id -> songs.find { it.id == id } }
                _uiState.update {
                    it.copy(
                        allSongs = songs,
                        recentSongs = recentSongs,
                        mostPlayedSongs = mostPlayedSongs,
                        recentAlbums = albums.take(10),
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            repository.getAllPlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }
}
