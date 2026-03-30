package com.neerajsahu.flux.androidclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.neerajsahu.flux.androidclient.core.ui.FluxApp
import com.neerajsahu.flux.androidclient.core.ui.theme.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Keep the splash screen on-screen until we know the login status
        splashScreen.setKeepOnScreenCondition { 
            viewModel.isUserLoggedIn.value == null 
        }
        
        enableEdgeToEdge()

        setContent {
            AndroidClientTheme {
                val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
                val currentUserProfile by viewModel.currentUserProfile.collectAsState()
                val isConnected by viewModel.isConnected.collectAsState(initial = true)
                val currentUserId = currentUserProfile?.userId

                FluxApp(
                    isUserLoggedIn = isUserLoggedIn,
                    currentUserId = currentUserId,
                    isConnected = isConnected
                )
            }
        }
    }
}
