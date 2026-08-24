package com.vpet.android

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    VpetAnimationView()
                }
            }
        }
    }
}

@Composable
fun VpetAnimationView() {
    val context = LocalContext.current
    var frameBitmaps by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    var currentFrame by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>("") }
    var happiness by remember { mutableStateOf(100) }

    LaunchedEffect(Unit) {
        try {
            val assetManager = context.assets
            val states = assetManager.list("vpetas")?.filter { it != "Default" } ?: emptyList()
            val selectedState = states.randomOrNull() ?: "Default"

            val subFolders = assetManager.list("vpetas/$selectedState") ?: emptyArray()
            val selectedSub = subFolders.randomOrNull() ?: ""

            val animationSequences = assetManager.list("vpetas/$selectedState/$selectedSub") ?: emptyArray()
            val selectedSeq = animationSequences.randomOrNull() ?: ""

            val path = "vpetas/$selectedState/$selectedSub/$selectedSeq"
            val files = assetManager.list(path)?.filter { it.endsWith(".png") }?.sorted() ?: emptyList()

            if (files.isNotEmpty()) {
                val bitmaps = mutableListOf<android.graphics.Bitmap>()
                for (file in files) {
                    val inputStream = assetManager.open("$path/$file")
                    BitmapFactory.decodeStream(inputStream)?.let { bitmaps.add(it) }
                    inputStream.close()
                }
                frameBitmaps = bitmaps
            } else {
                errorMessage = "ไม่พบไฟล์ใน $path"
            }
        } catch (e: IOException) {
            errorMessage = e.localizedMessage
        }
    }

    LaunchedEffect(frameBitmaps) {
        if (frameBitmaps.isNotEmpty()) {
            while (true) {
                delay(125L)
                currentFrame = (currentFrame + 1) % frameBitmaps.size
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (frameBitmaps.isNotEmpty()) {
            val imageBitmap = frameBitmaps[currentFrame].asImageBitmap()
            Canvas(modifier = Modifier
                .size(250.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        happiness = (happiness + 10).coerceAtMost(100)
                    })
                }) {
                drawImage(imageBitmap)
            }
        } else {
            Text(text = errorMessage ?: "กำลังโหลดอนิเมชัน VPet...")
        }
        Text(text = "ความสุข: $happiness", modifier = Modifier.align(Alignment.BottomCenter))
    }
}
