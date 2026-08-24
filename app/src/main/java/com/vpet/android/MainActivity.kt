package com.vpet.android

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
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

    LaunchedEffect(Unit) {
        try {
            // โหลดภาพจากโฟลเดอร์ IDEL (เช่น โฟลเดอร์ย่อยแรกหรือไฟล์ข้างใน)
            val assetManager = context.assets
            val ideFolders = assetManager.list("vpetas/IDEL") ?: emptyArray()
            val targetFolder = ideFolders.firstOrNull { it.contains("Nomal") || it.contains("Normal") } ?: ideFolders.firstOrNull()

            if (targetFolder != null) {
                val files = assetManager.list("vpetas/IDEL/$targetFolder")?.sorted() ?: emptyList()
                val bitmaps = mutableListOf<android.graphics.Bitmap>()
                for (file in files) {
                    if (file.endsWith(".png")) {
                        val inputStream = assetManager.open("vpetas/IDEL/$targetFolder/$file")
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            bitmaps.add(bitmap)
                        }
                        inputStream.close()
                    }
                }
                frameBitmaps = bitmaps
            } else {
                errorMessage = "ไม่พบโฟลเดอร์ย่อยใน IDEL"
            }
        } catch (e: IOException) {
            errorMessage = e.localizedMessage
        }
    }

    // วนลูปเปลี่ยนเฟรมแอนิเมชัน
    LaunchedEffect(frameBitmaps) {
        if (frameBitmaps.isNotEmpty()) {
            while (true) {
                delay(125L) // ความเร็วเฟรม (ประมาณ 8 FPS)
                currentFrame = (currentFrame + 1) % frameBitmaps.size
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (frameBitmaps.isNotEmpty()) {
            val imageBitmap = frameBitmaps[currentFrame].asImageBitmap()
            Canvas(modifier = Modifier.size(250.dp)) {
                drawImage(imageBitmap)
            }
        } else {
            Text(text = errorMessage ?: "กำลังโหลดอนิเมชัน VPet...")
        }
    }
}
