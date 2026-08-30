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
    val author: String? = null,
    val version: String? = "1.0",
    val fps: Int = 12,
    val animations: Map<String, AnimationData>
)

@Serializable
data class AnimationData(
    val frames: List<String>,
    val loop: Boolean = true,
    val nextAnimation: String? = null
)

class PetModManager(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    // ค้นหาโฟลเดอร์ Mod ทั้งหมดจากไดเรกทอรี /Android/data/.../mods/ หรือ External Files Dir
    fun getAvailableMods(): List<File> {
        val externalDir = context.getExternalFilesDir("mods") ?: File(context.filesDir, "mods")
        if (!externalDir.exists()) {
            externalDir.mkdirs()
        }
        return externalDir.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
    }

    // โหลด Config ของ Mod นั้นๆ
    fun loadModConfig(modFolder: File): ModConfig? {
        val configFile = File(modFolder, "config.json")
        if (!configFile.exists()) return null
        returntry {
            json.decodeFromString<ModConfig>(configFile.readText())
        } catch (e: Exception) {
            null
        }
    }

    // โหลดเฟรมภาพเคลื่อนไหวตามชื่อไฟล์ใน Mod
    fun loadFrameBitmap(modFolder: File, framePath: String): Bitmap? {
        val imgFile = File(modFolder, framePath)
        if (!imgFile.exists()) return null
        return BitmapFactory.decodeFile(imgFile.absolutePath)
    }
}
