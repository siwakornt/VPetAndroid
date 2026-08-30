package com.vpet.android.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vpet.android.R
import com.vpet.android.utils.AppPreferences

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // สร้าง Layout แบบง่ายผ่านโค้ด (หรือสามารถอ้างอิงจาก XML layout ก็ได้)
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val titleView = TextView(this).apply {
            text = "ตั้งค่า Gemini API Key"
            textSize = 20f
            setPadding(0, 0, 0, 24)
        }

        val inputApiKey = EditText(this).apply {
            hint = "กรอก Gemini API Key ที่นี่"
            setText(AppPreferences.getApiKey(this@SettingsActivity))
        }

        val saveButton = Button(this).apply {
            text = "บันทึก (Save)"
            setOnClickListener {
                val key = inputApiKey.text.toString().trim()
                AppPreferences.setApiKey(this@SettingsActivity, key)
                Toast.makeText(this@SettingsActivity, "บันทึก Key เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        layout.addView(titleView)
        layout.addView(inputApiKey)
        layout.addView(saveButton)

        setContentView(layout)
    }
}
