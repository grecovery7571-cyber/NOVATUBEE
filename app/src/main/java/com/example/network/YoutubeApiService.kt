package com.example.network

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApiService {

    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): YoutubeSearchResponse

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails,statistics",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): YoutubeVideoDetailsResponse
}

// Retrofit response data classes modeled after YouTube API v3
data class YoutubeSearchResponse(
    val items: List<SearchItem> = emptyList()
)

data class SearchItem(
    val id: IdObject,
    val snippet: SnippetObject
)

data class IdObject(
    val kind: String = "",
    val videoId: String = ""
)

data class SnippetObject(
    val publishedAt: String = "",
    val title: String = "",
    val description: String = "",
    val channelTitle: String = "",
    val thumbnails: ThumbnailsObject
)

data class ThumbnailsObject(
    val high: ThumbnailDetail? = null,
    val medium: ThumbnailDetail? = null,
    @Json(name = "default") val defaultThumbnail: ThumbnailDetail? = null
) {
    val bestUrl: String
        get() = high?.url ?: medium?.url ?: defaultThumbnail?.url ?: ""
}

data class ThumbnailDetail(
    val url: String = ""
)

// Details model
data class YoutubeVideoDetailsResponse(
    val items: List<VideoDetailsItem> = emptyList()
)

data class VideoDetailsItem(
    val id: String,
    val contentDetails: ContentDetailsObject?,
    val statistics: StatisticsObject?
)

data class ContentDetailsObject(
    val duration: String = "" // e.g. "PT15M33S"
)

data class StatisticsObject(
    val viewCount: String = "0"
)
