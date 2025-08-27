package io.livekit.android.example.voiceassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.livekit.android.LiveKit
import io.livekit.android.example.voiceassistant.screen.ConnectRoute
import io.livekit.android.example.voiceassistant.screen.ConnectScreen
import io.livekit.android.example.voiceassistant.screen.VoiceAssistantRoute
import io.livekit.android.example.voiceassistant.screen.VoiceAssistantScreen
import io.livekit.android.example.voiceassistant.ui.theme.LiveKitVoiceAssistantExampleTheme
import io.livekit.android.example.voiceassistant.viewmodel.VoiceAssistantViewModel
import io.livekit.android.util.LoggingLevel
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.Date
import java.util.UUID
class MainActivity : ComponentActivity() {

    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request necessary permissions
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }

        // Enable LiveKit logs
        LiveKit.loggingLevel = LoggingLevel.DEBUG

        setContent {
            val navController = rememberNavController()
            LiveKitVoiceAssistantExampleTheme(dynamicColor = false) {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                        // Setup NavHost navigation
                        NavHost(navController, startDestination = ConnectRoute) {

                            // 🔹 Screen 1: ConnectScreen
                            composable<ConnectRoute> {
                                ConnectScreen { _, _ ->
                                    val url = "wss://voiceagent-5dv0yxvk.livekit.cloud"
                                    val token =  generateLiveKitToken()
                                    runOnUiThread {
                                        navController.navigate(VoiceAssistantRoute(url, token))
                                    }
                                }
                            }

                            // 🔹 Screen 2: VoiceAssistantScreen
                            composable<VoiceAssistantRoute> {
                                val viewModel = viewModel<VoiceAssistantViewModel>()
                                VoiceAssistantScreen(
                                    viewModel = viewModel,
                                    onEndCall = {
                                        runOnUiThread { navController.navigateUp() }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    fun generateLiveKitToken(): String {
        val apiKey = "APIKHm8WfJFWYMC"   // 🔹 Your LiveKit API Key
        val apiSecret = "hF6sSw73NPfHjPM2MgymB2B5rEFKr9QHpLuRFye2Fu7" // 🔹 Your LiveKit API Secret
        val roomName = "test_room"
        val identity = "test_user_" + UUID.randomUUID().toString().take(6) // unique user each time

        val now = System.currentTimeMillis()
        val exp = Date(now + 60 * 60 * 1000)
//        val exp = Date(now + 60 * 1000)
        return Jwts.builder()
            .setIssuer(apiKey)  // API Key
            .setSubject(identity) // User identity
            .setExpiration(exp)   // Expiration
            .setIssuedAt(Date(now))
            .claim("name", identity)
            .claim("video", mapOf("room" to roomName, "roomJoin" to true))
            .signWith(SignatureAlgorithm.HS256, apiSecret.toByteArray())
            .compact()
    }
}


