package com.vpet.android.ai

import android.graphics.Bitmap
import android.util.Base64
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.io.ByteArrayOutputStream

class GeminiClient(private val apiKey: String) {
    private val client = HttpClient(CIO)

    suspend fun analyzeScreenshot(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=$apiKey"

        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                add(buildJsonObject {
                    putJsonArray("parts") {
                        add(buildJsonObject {
                            put("text", "วิเคราะห์หน้าจอนี้และให้คำแนะนำสั้นๆ น่ารักๆ ในฐานะสัตว์เลี้ยงบนหน้าจอ")
                        })
                        add(buildJsonObject {
                            put("inline_data", buildJsonObject {
                                put("mime_type", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    }
                })
            }
        }

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }

        val responseText = response.bodyAsText()
        val jsonElement = Json.parseToJsonElement(responseText)

        // Extract text response safely
        return try {
            jsonElement.jsonObject["candidates"]
                ?.jsonArray?.get(0)
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.get(0)
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content ?: "เข้าใจแล้วครับ!"
        } catch (e: Exception) {
            "เกิดข้อผิดพลาดในการวิเคราะห์"
        }
    }
}
