package com.example

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TubeViewModel

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    private val viewModel: TubeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Black
                ) {
                    MainAppScreen(
                        viewModel = viewModel,
                        onEnterPiP = { triggerPiP() }
                    )
                }
            }
        }
    }

    // Standard override to trigger PiP mode when home button is pressed or swiped out
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (viewModel.currentPlayingVideo.value != null && viewModel.isPiPEnabled.value) {
            triggerPiP()
        }
    }

    private fun triggerPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val aspectRatio = Rational(16, 9)
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()
                enterPictureInPictureMode(params)
                Log.d(tag, "Successfully entered native picture-in-picture mode")
            } catch (e: Exception) {
                Log.e(tag, "Failed to enter Picture-In-Picture mode: ${e.message}")
            }
        }
    }
}
