package com.example.echorooms

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.echorooms.theme.EchoRoomsTheme

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    
    // Request permissions at startup for high-fidelity sensory features
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      requestPermissions(
        arrayOf(
          android.Manifest.permission.RECORD_AUDIO,
          android.Manifest.permission.POST_NOTIFICATIONS
        ),
        101
      )
    } else {
      requestPermissions(
        arrayOf(android.Manifest.permission.RECORD_AUDIO),
        101
      )
    }

    setContent {
      EchoRoomsTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }

  override fun onStart() {
    super.onStart()
    com.example.echorooms.hardware.ParallaxSensorListener.start(this)
  }

  override fun onStop() {
    super.onStop()
    com.example.echorooms.hardware.ParallaxSensorListener.stop()
  }
}
