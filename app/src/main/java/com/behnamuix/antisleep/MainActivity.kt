package com.behnamuix.antisleep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.behnamuix.antisleep.ui.theme.BarcodeScannerTheme

@OptIn(ExperimentalGetImage::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarcodeScannerTheme {
                FaceDetectorComp()
            }
        }
    }
}




