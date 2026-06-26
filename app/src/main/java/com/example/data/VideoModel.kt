package com.example.data

import java.io.Serializable

data class VideoItem(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val publishedAt: String = "",
    val viewCount: String = "",
    val duration: String = "10:00",
    val isShort: Boolean = false,
    val streamUrl: String? = null // For fallback direct MP4 play strings
) : Serializable {
    val isYouTubeVideo: Boolean
        get() = streamUrl == null
}
