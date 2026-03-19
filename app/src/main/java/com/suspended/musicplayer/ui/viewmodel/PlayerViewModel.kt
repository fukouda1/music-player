package com.suspended.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suspended.musicplayer.data.repository.MusicRepository
import com.suspended.musicplayer.domain.model.Song
import com.suspended.musicplayer.player.MusicServiceConnection
import com.suspended.musicplayer.player.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val repository: MusicRepository
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = musicServiceConnection.playbackState

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    val favoriteIds: StateFlow<Set<Long>> = repository.getFavoriteIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    // Sleep timer
    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerRemaining: StateFlow<Long?> = _sleepTimerRemaining.asStateFlow()

    init {
        musicServiceConnection.connect()
        startPositionUpdater()
    }

    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (true) {
                _currentPosition.value = musicServiceConnection.getCurrentPosition()
                delay(200)
            }
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        musicServiceConnection.playSongs(songs, startIndex)
        viewModelScope.launch {
            songs.getOrNull(startIndex)?.let { song ->
                repository.recordPlay(song.id)
            }
        }
    }

    fun playPause() = musicServiceConnection.playPause()
    fun seekToNext() = musicServiceConnection.seekToNext()
    fun seekToPrevious() = musicServiceConnection.seekToPrevious()
    fun seekTo(position: Long) = musicServiceConnection.seekTo(position)
    fun toggleShuffle() = musicServiceConnection.toggleShuffle()
    fun toggleRepeatMode() = musicServiceConnection.toggleRepeatMode()
    fun setPlaybackSpeed(speed: Float) = musicServiceConnection.setPlaybackSpeed(speed)

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(songId)
        }
    }

    fun setSleepTimer(minutes: Int) {
        viewModelScope.launch {
            val durationMs = minutes * 60 * 1000L
            _sleepTimerRemaining.value = durationMs
            var remaining = durationMs
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                _sleepTimerRemaining.value = remaining
            }
            _sleepTimerRemaining.value = null
            musicServiceConnection.pause()
        }
    }

    fun cancelSleepTimer() {
        _sleepTimerRemaining.value = null
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.disconnect()
    }
}
