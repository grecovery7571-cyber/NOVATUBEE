package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun ExoVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = true,
    speed: Float = 1.0f,
    onPlaybackEnded: () -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    // Remember the ExoPlayer reference across recompositions
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    // Effect to bind loading state, stream item, speed, playWhenReady
    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.setPlaybackSpeed(speed)

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isLoading = (playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_ENDED) {
                    onPlaybackEnded()
                }
            }
        }
        exoPlayer.addListener(listener)

        try {
            kotlinx.coroutines.awaitCancellation()
        } finally {
            exoPlayer.removeListener(listener)
        }
    }

    // Dynamic speed update
    run {
        LaunchedEffect(speed) {
            exoPlayer.setPlaybackSpeed(speed)
        }
    }

    // Disposable effect to safely release player resource
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color.Red)
        }
    }
}

@Composable
fun YouTubeNativePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    onPlaybackEnded: () -> Unit = {}
) {
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    var youTubePlayerRef by remember { mutableStateOf<YouTubePlayer?>(null) }
    val currentVideoId = rememberUpdatedState(videoId)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            YouTubePlayerView(ctx).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                enableAutomaticInitialization = false
                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayerRef = youTubePlayer
                        youTubePlayer.loadVideo(currentVideoId.value, 0f)
                    }

                    override fun onStateChange(
                        youTubePlayer: YouTubePlayer,
                        state: PlayerConstants.PlayerState
                    ) {
                        if (state == PlayerConstants.PlayerState.ENDED) {
                            onPlaybackEnded()
                        }
                    }
                })
            }
        },
        update = { view ->
            youTubePlayerRef?.let { player ->
                player.loadVideo(videoId, 0f)
            }
        }
    )
}
