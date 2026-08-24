package com.vpet.android

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var frameBitmaps by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    var currentFrame by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>("") }
    var happiness by remember { mutableStateOf(100) }
    var importStatus by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputDir = File(context.filesDir, "custom_pet")
                if (outputDir.exists()) outputDir.deleteRecursively()
                outputDir.mkdirs()

                if (inputStream != null) {
                    VpaExtractor.extractVpa(context, inputStream, outputDir)
                    importStatus = "นำเข้าสำเร็จ!"
                }
            } catch (e: Exception) {
                importStatus = "นำเข้าล้มเหลว: ${e.localizedMessage}"
            }
        }
    }

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

        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { launcher.launch("application/zip") }) {
                Text("Import Character (.vpa)")
            }
            if (importStatus.isNotEmpty()) {
                Text(text = importStatus, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "ความสุข: $happiness")
        }
    }
}
