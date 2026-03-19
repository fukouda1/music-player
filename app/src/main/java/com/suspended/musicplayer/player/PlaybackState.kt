package com.suspended.musicplayer.player

import com.suspended.musicplayer.domain.model.Song

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1f,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1
) {
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration else 0f
}

enum class RepeatMode {
    OFF, ONE, ALL
}
