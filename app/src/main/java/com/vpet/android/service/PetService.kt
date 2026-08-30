package com.vpet.android.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.PopupMenu
import com.vpet.android.ai.GeminiClient
import com.vpet.android.mod.PetModManager
import com.vpet.android.mod.ModConfig
import java.io.File

class PetService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var petView: ImageView
    private lateinit var bubbleView: TextView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var modManager: PetModManager
    private lateinit var geminiClient: GeminiClient

    private var currentModFolder: File? = null
    private var modConfig: ModConfig? = null
    private var currentAnimationState = "idle"
    private var currentFrameIndex = 0
    private var animationRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        modManager = PetModManager(this)

        val savedKey = com.vpet.android.utils.AppPreferences.getApiKey(this)
        geminiClient = GeminiClient(savedKey)

        petView = ImageView(this).apply {
            setImageResource(android.R.drawable.star_big_on)
        }

        bubbleView = TextView(this).apply {
            setBackgroundColor(0xAAFFFFFF.toInt())
            setPadding(16, 16, 16, 16)
            visibility = View.GONE
        }

        val petParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        val bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        setupTouchListener(petParams, bubbleParams)

        petView.setOnLongClickListener {
            showContextMenu()
            true
        }

        windowManager.addView(bubbleView, bubbleParams)
        windowManager.addView(petView, petParams)

        // โหลด Mod แรกที่พบจากระบบ
        loadFirstAvailableMod()
    }

    private fun loadFirstAvailableMod() {
        val mods = modManager.getAvailableMods()
        if (mods.isNotEmpty()) {
            currentModFolder = mods[0]
            modConfig = modManager.loadModConfig(currentModFolder!!)
            startAnimation("idle")
        } else {
            showBubble("ไม่พบโฟลเดอร์ Mod ในเครื่อง!")
        }
    }

    private fun startAnimation(stateName: String) {
        val folder = currentModFolder ?: return
        val config = modConfig ?: return
        val animData = config.animations[stateName] ?: return

        currentAnimationState = stateName
        currentFrameIndex = 0

        animationRunnable?.let { handler.removeCallbacks(it) }

        val interval = (1000 / config.fps).toLong()

        animationRunnable = object : Runnable {
            override fun run() {
                if (currentFrameIndex >= animData.frames.size) {
                    if (animData.loop) {
                        currentFrameIndex = 0
                    } else {
                        // ถ้าไม่ loop ให้เปลี่ยนเป็น nextAnimation หรือกลับไป idle
                        val next = animData.nextAnimation ?: "idle"
                        startAnimation(next)
                        return
                    }
                }

                val framePath = animData.frames[currentFrameIndex]
                val bitmap = modManager.loadFrameBitmap(folder, framePath)
                if (bitmap != null) {
                    petView.setImageBitmap(bitmap)
                }

                currentFrameIndex++
                handler.postDelayed(this, interval)
            }
        }
        handler.post(animationRunnable!!)
    }

    private fun setupTouchListener(petParams: WindowManager.LayoutParams, bubbleParams: WindowManager.LayoutParams) {
        petView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = petParams.x
                        initialY = petParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        startAnimation("touch_head") // เปลี่ยนท่าทางเมื่อแตะ
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        petParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        petParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        bubbleParams.x = petParams.x
                        bubbleParams.y = petParams.y - 120

                        windowManager.updateViewLayout(petView, petParams)
                        windowManager.updateViewLayout(bubbleView, bubbleParams)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun showContextMenu() {
        val popup = PopupMenu(this, petView)
        popup.menu.add(0, 1, 0, "ท่าทาง: Idle")
        popup.menu.add(0, 2, 1, "ท่าทาง: Talk")
        popup.menu.add(0, 3, 2, "ท่าทาง: Walking")
        popup.menu.add(0, 4, 3, "ท่าทาง: Sleep")
        popup.menu.add(0, 5, 4, "ท่าทาง: Happy")
        popup.menu.add(0, 6, 5, "ท่าทาง: Ill (ป่วย)")
        popup.menu.add(0, 7, 6, "ตั้งค่า API Key")
        popup.menu.add(0, 8, 7, "ปิดตัวละคร")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> { startAnimation("idle"); showBubble("เปลี่ยนเป็น Idle"); true }
                2 -> { startAnimation("talk"); showBubble("กำลังพูดคุย..."); true }
                3 -> { startAnimation("walking"); showBubble("กำลังเดินเล่น!"); true }
                4 -> { startAnimation("sleep"); showBubble("คร็อก... ฟี้..."); true }
                5 -> { startAnimation("happy"); showBubble("เย้! ดีใจจัง"); true }
                6 -> { startAnimation("ill"); showBubble("แง... รู้สึกไม่ค่อยสบายเลย"); true }
                7 -> {
                    val intent = Intent(this, com.vpet.android.ui.SettingsActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                    true
                }
                8 -> { stopSelf(); true }
                else -> false
            }
        }
        try {
            val field = PopupMenu::class.java.getDeclaredField("mPopup")
            field.isAccessible = true
            val mPopup = field.get(popup)
            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(mPopup, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        popup.show()
    }

    private fun showBubble(text: String) {
        bubbleView.text = text
        bubbleView.visibility = View.VISIBLE
        handler.postDelayed({
            bubbleView.visibility = View.GONE
        }, 4000)
    }

    override fun onDestroy() {
        super.onDestroy()
        animationRunnable?.let { handler.removeCallbacks(it) }
        if (::petView.isInitialized) windowManager.removeView(petView)
        if (::bubbleView.isInitialized) windowManager.removeView(bubbleView)
    }
}
