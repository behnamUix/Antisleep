package com.behnamuix.antisleep

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.behnamuix.antisleep.Utils.MySoundPlayer

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun FaceDetectorComp() {
    val context = LocalContext.current
    var eyeAlertShown by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var resultText by remember { mutableStateOf("چهره‌ای شناسایی نشده") }

    // 🔹 Face Detector
    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .build()
    )

    // 🔹 Permission
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(
                    context,
                    "اجازه دوربین داده نشد",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    LaunchedEffect(Unit) {
        MySoundPlayer.init(context)
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CameraPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onImage = { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        faceDetector.process(image)
                            .addOnSuccessListener { faces ->
                                if (faces.isNotEmpty()) {
                                    val face = faces.first()

                                    val smile = (face.smilingProbability ?: 0f) * 100
                                    val leftEye = (face.leftEyeOpenProbability ?: 0f) * 100
                                    val rightEye = (face.rightEyeOpenProbability ?: 0f) * 100

// زاویه‌های سر
                                    val headX = face.headEulerAngleX  // بالا/پایین
                                    val headY = face.headEulerAngleY  // چپ/راست
                                    val headZ = face.headEulerAngleZ  // کج شدن

                                    if ((rightEye < 10 && leftEye < 10) && !eyeAlertShown) {

                                        MySoundPlayer.play()
                                        Toast.makeText(context, "بیدار شو!!", Toast.LENGTH_LONG)
                                            .show()
                                        eyeAlertShown = true
                                    } else if (rightEye >= 10 && leftEye >= 10) {
                                        // وقتی دوباره چشم‌ها باز شد Flag ریست شود
                                        eyeAlertShown = false
                                        MySoundPlayer.stop()
                                    }
                                    val smileText = String.format("%.2f", smile)
                                    val eyeTextL = String.format("%.2f", leftEye)
                                    val eyeTextR = String.format("%.2f", rightEye)
                                    val headXText = String.format("%.2f", headX)
                                    val headYText = String.format("%.2f", headY)
                                    val headZText = String.format("%.2f", headZ)


                                    resultText =
                                        "😄 درصد خندیدن: % $smileText\n" +
                                                "👁️ چشم چپ: % $eyeTextL\n" +
                                                "👁️ چشم راست: % $eyeTextR\n" +
                                                "⬆️⬇️ سر بالا/پایین: $headXText°\n" +
                                                "⬅️➡️ سر چپ/راست: $headYText°\n" +
                                                "🔄 کج شدن سر: $headZText°"

                                } else {
                                    resultText = "چهره‌ای شناسایی نشده"
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            )

            // 🔹 Result Text
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImage: (ImageProxy) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({

                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            onImage
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

fun drawableToBitmap(
    context: Context, drawableResId: Int
): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableResId)!!
    return drawable.toBitmap()
}


