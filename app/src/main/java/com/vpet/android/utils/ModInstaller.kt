package com.vpet.android.utils

import android.content.Context
import java.io.*
import java.util.zip.ZipInputStream

object ModInstaller {
    fun installVpa(context: Context, inputStream: InputStream): Boolean {
        val modsDir = context.getExternalFilesDir("mods") ?: File(context.filesDir, "mods")
        val targetDir = File(modsDir, "Imported_" + System.currentTimeMillis())
        if (!targetDir.exists()) targetDir.mkdirs()

        return try {
            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val file = File(targetDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    entry = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
