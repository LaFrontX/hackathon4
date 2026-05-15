package com.example.blatplat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.blatplat.ui.HomeScreen
import com.example.blatplat.ui.theme.BlatPlatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlatPlatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        onPayClick = {
                            Log.d(TAG, "Pay button clicked")
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
