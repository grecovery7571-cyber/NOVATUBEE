package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(video: FavoriteVideo)

    @Delete
    suspend fun deleteFavorite(video: FavoriteVideo)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE videoId = :videoId LIMIT 1)")
    fun isFavorite(videoId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE videoId = :videoId LIMIT 1)")
    suspend fun isFavoriteDirect(videoId: String): Boolean
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<SearchHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: SearchHistoryItem)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY timestamp DESC")
    fun getCommentsForVideo(videoId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Int)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(sub: SubscriptionEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE channelTitle = :channelTitle LIMIT 1)")
    fun isSubscribed(channelTitle: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE channelTitle = :channelTitle LIMIT 1)")
    suspend fun isSubscribedDirect(channelTitle: String): Boolean
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylistById(id: Int): Flow<PlaylistEntity?>
}

@Dao
interface PlaylistVideoDao {
    @Query("SELECT * FROM playlist_videos WHERE playlistId = :playlistId ORDER BY id ASC")
    fun getVideosForPlaylist(playlistId: Int): Flow<List<PlaylistVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistVideo(video: PlaylistVideoEntity)

    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun deletePlaylistVideo(playlistId: Int, videoId: String)

    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId")
    suspend fun deleteVideosForPlaylist(playlistId: Int)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}

@Database(
    entities = [
        FavoriteVideo::class,
        SearchHistoryItem::class,
        CommentEntity::class,
        SubscriptionEntity::class,
        PlaylistEntity::class,
        PlaylistVideoEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun commentDao(): CommentDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistVideoDao(): PlaylistVideoDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tubeplayer_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
