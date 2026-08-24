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
    var installedMods by remember { mutableStateOf(ModManager.getInstalledMods(context)) }
    var selectedMod by remember { mutableStateOf(installedMods.firstOrNull() ?: "") }
    var selectedAnimation by remember { mutableStateOf("IDEL") }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val modName = "mod_" + System.currentTimeMillis()
                val modsDir = File(context.filesDir, "mods/$modName")
                modsDir.mkdirs()

                if (inputStream != null) {
                    VpaExtractor.extractVpa(context, inputStream, modsDir)
                    importStatus = "นำเข้าสำเร็จ: $modName"
                    installedMods = ModManager.getInstalledMods(context)
                    if (selectedMod.isEmpty()) selectedMod = modName
                }
            } catch (e: Exception) {
                importStatus = "นำเข้าล้มเหลว: ${e.localizedMessage}"
            }
        }
    }

    LaunchedEffect(selectedMod, selectedAnimation) {
        if (selectedMod.isNotEmpty()) {
            val bitmaps = ModManager.loadModFrames(context, selectedMod, selectedAnimation)
            if (bitmaps.isNotEmpty()) {
                frameBitmaps = bitmaps
                errorMessage = null
            } else {
                errorMessage = "ไม่พบเฟรมแอนิเมชันสำหรับ $selectedAnimation ใน mod $selectedMod"
            }
        } else {
            // Fallback ไปใช้ assets เดิม
            try {
                val assetManager = context.assets
                val path = "vpetas/IDEL"
                val files = assetManager.list(path)?.filter { it.endsWith(".png") }?.sorted() ?: emptyList()
                if (files.isNotEmpty()) {
                    val bitmaps = mutableListOf<android.graphics.Bitmap>()
                    for (file in files) {
                        assetManager.open("$path/$file").use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)?.let { bitmaps.add(it) }
                        }
                    }
                    frameBitmaps = bitmaps
                }
            } catch (e: IOException) {
                errorMessage = e.localizedMessage
            }
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
            val imageBitmap = frameBitmaps[currentFrame % frameBitmaps.size].asImageBitmap()
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (installedMods.isNotEmpty()) {
                Text("เลือก Mod: $selectedMod", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }

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
