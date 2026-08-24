package com.vpet.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

object ModManager {
    fun getInstalledMods(context: Context): List<String> {
        val modsDir = File(context.filesDir, "mods")
        if (!modsDir.exists()) return emptyList()
        return modsDir.listFiles { file -> file.isDirectory }?.map { it.name } ?: emptyList()
    }

    fun loadModFrames(context: Context, modName: String, animationType: String): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val modDir = File(context.filesDir, "mods/$modName/$animationType")
        if (!modDir.exists()) return emptyList()

        val files = modDir.listFiles { file -> file.extension.lowercase() == "png" }?.sortedBy { it.name } ?: emptyList()
        for (file in files) {
            BitmapFactory.decodeFile(file.absolutePath)?.let { bitmaps.add(it) }
        }
        return bitmaps
    }
}
