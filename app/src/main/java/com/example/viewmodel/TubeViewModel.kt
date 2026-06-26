package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TubeViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "TubeViewModel"
    private val prefs: SharedPreferences = application.getSharedPreferences("TubePrefs", Context.MODE_PRIVATE)

    // DB & Repo initialization
    private val db = AppDatabase.getDatabase(application)
    private val repository = TubeRepository(db.favoriteDao(), db.searchHistoryDao())

    // --- STATES ---
    private val defaultApiKey = "AIzaSyCuCHwxsd9rklBT46WKS5TZufgT-RXYRE8"
    private val _apiKey = MutableStateFlow(
        prefs.getString("yt_api_key", defaultApiKey).let {
            if (it.isNullOrBlank()) defaultApiKey else it
        }
    )
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _videos = MutableStateFlow<List<VideoItem>>(emptyList())
    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    private val _homeVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val homeVideos: StateFlow<List<VideoItem>> = _homeVideos.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- USER PROFILE STATES ---
    private val _userName = MutableStateFlow(prefs.getString("user_name", "Alex Mercer") ?: "Alex Mercer")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString("user_email", "premium@novatube.com") ?: "premium@novatube.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isUserPremium = MutableStateFlow(prefs.getBoolean("user_is_premium", true))
    val isUserPremium: StateFlow<Boolean> = _isUserPremium.asStateFlow()

    private val _userAvatarId = MutableStateFlow(prefs.getInt("user_avatar_id", 3))
    val userAvatarId: StateFlow<Int> = _userAvatarId.asStateFlow()

    // --- REACTIVE DATABASE FLOWS ---
    val subscriptions: StateFlow<List<SubscriptionEntity>> = db.subscriptionDao().getAllSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = db.playlistDao().getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- API CONNECTION STATE ---
    private val _apiTestStatus = MutableStateFlow<String?>(null)
    val apiTestStatus: StateFlow<String?> = _apiTestStatus.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    // --- FREE-FORM FLOATING SIZING ---
    private val _floatingCustomWidth = MutableStateFlow(260f)
    val floatingCustomWidth: StateFlow<Float> = _floatingCustomWidth.asStateFlow()

    // Room reactive streams
    val favorites: StateFlow<List<FavoriteVideo>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchHistory: StateFlow<List<SearchHistoryItem>> = repository.searchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- PLAYER STATES ---
    private val _currentPlayingVideo = MutableStateFlow<VideoItem?>(null)
    val currentPlayingVideo: StateFlow<VideoItem?> = _currentPlayingVideo.asStateFlow()

    private val _isFloatingWindowActive = MutableStateFlow(false)
    val isFloatingWindowActive: StateFlow<Boolean> = _isFloatingWindowActive.asStateFlow()

    // Comments State Flow
    private val _comments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val comments: StateFlow<List<CommentEntity>> = _comments.asStateFlow()
    private var commentsCollectionJob: Job? = null

    // Draggable Float Player dimensions and positions
    private val _floatingX = MutableStateFlow(60f)
    val floatingX: StateFlow<Float> = _floatingX.asStateFlow()

    private val _floatingY = MutableStateFlow(320f)
    val floatingY: StateFlow<Float> = _floatingY.asStateFlow()

    // preset sizes: "S" = 180dp, "M" = 260dp, "L" = 340dp
    private val _floatingSizePreset = MutableStateFlow("M") 
    val floatingSizePreset: StateFlow<String> = _floatingSizePreset.asStateFlow()

    val isPiPEnabled = MutableStateFlow(true)
    val currentSpeed = MutableStateFlow(1.0f)

    // --- SLEEP TIMER STATE ---
    private val _sleepTimerRemainingSeconds = MutableStateFlow(0)
    val sleepTimerRemainingSeconds: StateFlow<Int> = _sleepTimerRemainingSeconds.asStateFlow()

    private val _isSleepTimerRunning = MutableStateFlow(false)
    val isSleepTimerRunning: StateFlow<Boolean> = _isSleepTimerRunning.asStateFlow()

    private var sleepTimerJob: Job? = null

    init {
        // Load initial home feed
        refreshHomeFeed()
    }

    // --- TIMING / CONTROL SETTINGS ---
    fun saveApiKey(newKey: String) {
        prefs.edit().putString("yt_api_key", newKey.trim()).apply()
        _apiKey.value = newKey.trim()
        refreshHomeFeed()
    }

    fun setPlayingVideo(video: VideoItem?, makeFloating: Boolean = false) {
        _currentPlayingVideo.value = video
        _isFloatingWindowActive.value = makeFloating
        
        // Handle comment reactive collection
        commentsCollectionJob?.cancel()
        if (video == null) {
            _comments.value = emptyList()
        } else {
            val videoId = video.videoId
            commentsCollectionJob = viewModelScope.launch {
                db.commentDao().getCommentsForVideo(videoId).collect { list ->
                    if (list.isEmpty()) {
                        // Generate rich engaging mock comments for realistic YT player feeling
                        val mockComments = listOf(
                            CommentEntity(
                                videoId = videoId,
                                author = "@gotech_enthusiast",
                                text = "This is exceptionally well-explained! Love the dynamic floating window feature, absolutely brilliant and lag-free.",
                                likes = 142,
                                timestamp = System.currentTimeMillis() - 7200000
                            ),
                            CommentEntity(
                                videoId = videoId,
                                author = "@shreyas_k",
                                text = "Awesome interface! Feels so smooth. Finally, a real YouTube player that lets you browse, read comments and play in the background properly.",
                                likes = 89,
                                timestamp = System.currentTimeMillis() - 86400000
                            ),
                            CommentEntity(
                                videoId = videoId,
                                author = "@techie_girl",
                                text = "The picture-in-picture mode and sleep timer are super useful for night listening. Outstanding implementation, kudos! 🔥🚀",
                                likes = 64,
                                timestamp = System.currentTimeMillis() - 172800000
                            ),
                            CommentEntity(
                                videoId = videoId,
                                author = "@minimalist_dev",
                                text = "The custom slate visual styling of this app is top notch. It fits Material Design 3 beautifully.",
                                likes = 12,
                                timestamp = System.currentTimeMillis() - 259200000
                            )
                        )
                        // Bulk insert to Room
                        mockComments.forEach { db.commentDao().insertComment(it) }
                    } else {
                        _comments.value = list
                    }
                }
            }
        }
    }

    fun addComment(videoId: String, author: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val userComment = CommentEntity(
                videoId = videoId,
                author = if (author.isBlank()) "@You" else author,
                text = text.trim(),
                likes = 0,
                timestamp = System.currentTimeMillis()
            )
            db.commentDao().insertComment(userComment)
        }
    }

    fun likeComment(comment: CommentEntity) {
        viewModelScope.launch {
            val updated = comment.copy(likes = comment.likes + 1)
            db.commentDao().insertComment(updated)
        }
    }

    fun toggleFloatingPlayer(active: Boolean) {
        _isFloatingWindowActive.value = active
    }

    fun updateFloatingPosition(dx: Float, dy: Float) {
        _floatingX.value = (_floatingX.value + dx).coerceAtLeast(0f)
        _floatingY.value = (_floatingY.value + dy).coerceAtLeast(0f)
    }

    fun setFloatingSizePreset(preset: String) {
        if (preset in listOf("S", "M", "L")) {
            _floatingSizePreset.value = preset
            _floatingCustomWidth.value = when (preset) {
                "S" -> 180f
                "M" -> 260f
                else -> 340f
            }
        }
    }

    // --- CORE LOGICS: SEARCH & FETCH ---
    fun searchVideos(query: String) {
        _searchQuery.value = query
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.saveSearchQuery(query)
                val results = repository.searchVideos(query, _apiKey.value)
                _videos.value = results
            } catch (e: Exception) {
                Log.e(tag, "Search fail: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshHomeFeed() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val feed = repository.getHomeFeed(_apiKey.value)
                _homeVideos.value = feed
            } catch (e: Exception) {
                Log.e(tag, "Home feed fail: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- SLEEP TIMER OPERATIONS ---
    fun startSleepTimer(minutes: Int) {
        stopSleepTimer()
        if (minutes <= 0) return

        _isSleepTimerRunning.value = true
        _sleepTimerRemainingSeconds.value = minutes * 60

        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes * 60
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _sleepTimerRemainingSeconds.value = remaining
            }
            // Shut off player automatically
            shutOffPlayback()
        }
    }

    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _isSleepTimerRunning.value = false
        _sleepTimerRemainingSeconds.value = 0
    }

    private fun shutOffPlayback() {
        _currentPlayingVideo.value = null
        _isFloatingWindowActive.value = false
        stopSleepTimer()
    }

    // --- FAVORITES ACTION PASSTHROUGH ---
    fun toggleFavorite(video: VideoItem) {
        viewModelScope.launch {
            val isFav = repository.isFavoriteDirect(video.videoId)
            if (isFav) {
                repository.removeFavorite(video)
            } else {
                repository.addFavorite(video)
            }
        }
    }

    fun deleteHistoryItem(query: String) {
        viewModelScope.launch {
            repository.deleteHistoryQuery(query)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    // --- USER PROFILE ACTIONS ---
    fun updateProfile(name: String, email: String, avatarId: Int, isPremium: Boolean) {
        prefs.edit().apply {
            putString("user_name", name.trim())
            putString("user_email", email.trim())
            putInt("user_avatar_id", avatarId)
            putBoolean("user_is_premium", isPremium)
        }.apply()
        _userName.value = name.trim()
        _userEmail.value = email.trim()
        _userAvatarId.value = avatarId
        _isUserPremium.value = isPremium
    }

    // --- SUBSCRIPTIONS ACTIONS ---
    fun toggleSubscription(channelTitle: String, avatarUrl: String = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200") {
        viewModelScope.launch {
            val isSubscribed = db.subscriptionDao().isSubscribedDirect(channelTitle)
            if (isSubscribed) {
                db.subscriptionDao().deleteSubscription(SubscriptionEntity(channelTitle, avatarUrl))
            } else {
                db.subscriptionDao().insertSubscription(SubscriptionEntity(channelTitle, avatarUrl))
                
                // Simulate a gorgeous upload notification after subscribing!
                delay(1200L)
                val isStillSubscribed = db.subscriptionDao().isSubscribedDirect(channelTitle)
                if (isStillSubscribed) {
                    val sampleNotifications = listOf(
                        NotificationEntity(
                            title = "$channelTitle uploaded a new video",
                            message = "1 A.M Study Session 📚 - ambient beats to focus/relax",
                            videoId = "jfKfPfyJRdk",
                            channelTitle = channelTitle,
                            thumbnailUrl = "https://images.unsplash.com/photo-1518173946687-a4c8a383392c?w=600"
                        ),
                        NotificationEntity(
                            title = "Exclusive Premiere from $channelTitle",
                            message = "Retro Waves Synth Live - late night beats 🌌",
                            videoId = "4xDzrJKXOOY",
                            channelTitle = channelTitle,
                            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600"
                        )
                    )
                    db.notificationDao().insertNotification(sampleNotifications.random())
                }
            }
        }
    }

    // --- PLAYLISTS ACTIONS ---
    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            db.playlistDao().insertPlaylist(PlaylistEntity(name = name.trim()))
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            db.playlistVideoDao().deleteVideosForPlaylist(playlist.id)
            db.playlistDao().deletePlaylist(playlist)
        }
    }

    fun addVideoToPlaylist(playlistId: Int, video: VideoItem) {
        viewModelScope.launch {
            db.playlistVideoDao().insertPlaylistVideo(
                PlaylistVideoEntity(
                    playlistId = playlistId,
                    videoId = video.videoId,
                    title = video.title,
                    thumbnailUrl = video.thumbnailUrl,
                    channelTitle = video.channelTitle,
                    duration = video.duration
                )
            )
        }
    }

    fun removeVideoFromPlaylist(playlistId: Int, videoId: String) {
        viewModelScope.launch {
            db.playlistVideoDao().deletePlaylistVideo(playlistId, videoId)
        }
    }

    fun getVideosForPlaylist(playlistId: Int): Flow<List<PlaylistVideoEntity>> {
        return db.playlistVideoDao().getVideosForPlaylist(playlistId)
    }

    fun isSubscribedFlow(channelTitle: String): Flow<Boolean> {
        return db.subscriptionDao().isSubscribed(channelTitle)
    }

    // --- NOTIFICATION ACTIONS ---
    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            db.notificationDao().markAsRead(id)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            db.notificationDao().clearAllNotifications()
        }
    }

    // --- API CONNECTION TESTING ---
    fun removeApiKey() {
        prefs.edit().remove("yt_api_key").apply()
        _apiKey.value = defaultApiKey
        _apiTestStatus.value = null
        refreshHomeFeed()
    }

    fun testApiKeyConnection(key: String) {
        if (key.isBlank()) {
            _apiTestStatus.value = "Error: API Key is blank"
            return
        }
        _isTestingApi.value = true
        _apiTestStatus.value = "Testing connection..."
        
        viewModelScope.launch {
            try {
                val response = repository.apiService.searchVideos(
                    query = "lofi study",
                    maxResults = 1,
                    apiKey = key.trim()
                )
                if (response.items.isNotEmpty()) {
                    _apiTestStatus.value = "Success: API Key is active and responding!"
                } else {
                    _apiTestStatus.value = "Success: Connected, but response empty"
                }
            } catch (e: Exception) {
                val errMsg = e.message ?: "Unknown error"
                Log.e(tag, "API key test failure: $errMsg", e)
                _apiTestStatus.value = "Failure: $errMsg"
            } finally {
                _isTestingApi.value = false
            }
        }
    }

    // --- FLOATING WIDTH ACTIONS ---
    fun updateFloatingWidth(delta: Float) {
        _floatingCustomWidth.value = (_floatingCustomWidth.value + delta).coerceIn(120f, 420f)
    }

    override fun onCleared() {
        super.onCleared()
        stopSleepTimer()
    }
}
