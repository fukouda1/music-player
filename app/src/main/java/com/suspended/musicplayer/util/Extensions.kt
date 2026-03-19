package com.suspended.musicplayer.util

fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

fun Long.formatDurationLong(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

fun Int.formatSongCount(): String {
    return when {
        this == 1 -> "1 song"
        else -> "$this songs"
    }
}

fun Long.formatFileSize(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) {
        "%.1f MB".format(mb)
    } else {
        "%.0f KB".format(kb)
    }
}
