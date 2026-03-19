package com.suspended.musicplayer.domain.model

import android.net.Uri

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val artworkUri: Uri?,
    val songCount: Int,
    val year: Int
)
