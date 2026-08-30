package com.vpet.android.mod

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ModConfig(
    val name: String,
    val fps: Int,
    val animations: List<AnimationConfig>
)

@Serializable
data class AnimationConfig(
    val id: String,
    val frames: List<String>,
    val loop: Boolean
)

class PetLoader(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadMod(modFolder: File): ModConfig {
        val configFile = File(modFolder, "config.json")
        val jsonString = if (configFile.exists()) {
            configFile.readText()
        } else {
            // Fallback default config JSON string
            """
            {
              "name": "Default Pet",
              "fps": 12,
              "animations": [
                { "id": "idle", "frames": [], "loop": true },
                { "id": "talk", "frames": [], "loop": true }
              ]
            }
            """
        }
        return json.decodeFromString(jsonString)
    }

    fun getFrameBitmap(modFolder: File, frameName: String): Bitmap? {
        val file = File(modFolder, frameName)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }
}
