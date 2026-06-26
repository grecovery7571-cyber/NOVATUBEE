package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteVideo(
    @PrimaryKey val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val publishedAt: String = "",
    val viewCount: String = "",
    val duration: String = "10:00",
    val isShort: Boolean = false,
    val streamUrl: String? = null,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toVideoItem(): VideoItem {
        return VideoItem(
            videoId = videoId,
            title = title,
            description = description,
            thumbnailUrl = thumbnailUrl,
            channelTitle = channelTitle,
            publishedAt = publishedAt,
            viewCount = viewCount,
            duration = duration,
            isShort = isShort,
            streamUrl = streamUrl
        )
    }

    companion object {
        fun fromVideoItem(video: VideoItem): FavoriteVideo {
            return FavoriteVideo(
                videoId = video.videoId,
                title = video.title,
                description = video.description,
                thumbnailUrl = video.thumbnailUrl,
                channelTitle = video.channelTitle,
                publishedAt = video.publishedAt,
                viewCount = video.viewCount,
                duration = video.duration,
                isShort = video.isShort,
                streamUrl = video.streamUrl
            )
        }
    }
}

@Entity(tableName = "search_history")
data class SearchHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoId: String,
    val author: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelTitle: String,
    val avatarUrl: String,
    val subscribedAt: Long = System.currentTimeMillis(),
    val isNotificationEnabled: Boolean = true
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_videos")
data class PlaylistVideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playlistId: Int,
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val duration: String = "10:00"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val videoId: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)


