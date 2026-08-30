package com.vpet.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vpet.android.mod.PetModManager
import com.vpet.android.service.PetService
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 100)
        }

        setContent {
            MaterialTheme {
                MainScreen(
                    onStartPet = { startPetService() },
                    onStopPet = { stopPetService() }
                )
            }
        }
    }

    private fun startPetService() {
        val intent = Intent(this, PetService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopPetService() {
        val intent = Intent(this, PetService::class.java)
        stopService(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onStartPet: () -> Unit, onStopPet: () -> Unit) {
    val context = LocalContext.current
    val modManager = remember { PetModManager(context) }
    var mods by remember { mutableStateOf(modManager.getAvailableMods()) }
    var selectedMod by remember { mutableStateOf<File?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Desktop Pet Mod Manager") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Select Character:", style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
            ) {
                items(mods) { mod ->
                    Card(
                        modifier = Modifier.padding(4.dp).clickable { selectedMod = mod },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            Text(mod.name)
                        }
                    }
                }
            }

            if (selectedMod != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onStartPet) { Text("Start Pet") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onStopPet) { Text("Stop Pet") }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Character Settings") },
                text = {
                    Text("Resize and adjustments will be implemented here.")
                },
                confirmButton = {
                    TextButton(onClick = { showSettings = false }) { Text("Save") }
                }
            )
        }
    }
}
