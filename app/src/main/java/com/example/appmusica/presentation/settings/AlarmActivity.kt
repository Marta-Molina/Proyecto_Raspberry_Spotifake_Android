package com.example.appmusica.presentation.settings

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.appmusica.databinding.ActivityAlarmBinding
import com.example.appmusica.service.AlarmNotificationService
import com.example.appmusica.util.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private var alarmId: Int = -1
    private var initialX = 0f
    private var sliderWidth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOnLockScreen()
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmId = intent.getIntExtra("ALARM_ID", -1)
        val songName = intent.getStringExtra("SONG_NAME") ?: "Alarma"
        val artistName = intent.getStringExtra("ARTIST_NAME") ?: "Spotifake"
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        binding.tvSongName.text = songName
        binding.tvArtistName.text = artistName
        binding.tvAlarmTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        binding.sliderContainer.post {
            sliderWidth = binding.sliderContainer.width
            // Recenter thumb
            binding.sliderThumb.x = (sliderWidth / 2f) - (binding.sliderThumb.width / 2f)
        }

        if (!imageUrl.isNullOrEmpty()) {
            val fullImageUrl = if (imageUrl.startsWith("http")) imageUrl else {
                com.example.appmusica.di.NetworkModule.BASE_STATIC_URL.removeSuffix("/") + "/" + imageUrl.removePrefix("/")
            }
            val glideUrl = com.bumptech.glide.load.model.GlideUrl(fullImageUrl, com.bumptech.glide.load.model.LazyHeaders.Builder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build())
            Glide.with(this).load(glideUrl).placeholder(com.example.appmusica.R.drawable.portada_generica).circleCrop().into(binding.ivSongImage)
        }

        setupSlider()
        registerStopReceiver()
    }

    private val stopReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ACTION_STOP_ALARM_ACTIVITY") {
                finish()
            }
        }
    }

    private fun registerStopReceiver() {
        val filter = android.content.IntentFilter("ACTION_STOP_ALARM_ACTIVITY")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stopReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(stopReceiver)
        } catch (e: Exception) {}
    }

    private fun setupSlider() {
        binding.sliderThumb.setOnTouchListener { v, event ->
            if (sliderWidth <= 0) return@setOnTouchListener false
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = v.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - (binding.sliderContainer.width / 2f + binding.sliderContainer.left)
                    val newX = (sliderWidth / 2f - v.width / 2f) + deltaX
                    
                    // Limit movement
                    if (newX > 0 && newX < sliderWidth - v.width) {
                        v.x = newX
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val finalPosition = v.x + v.width / 2f
                    
                    when {
                        finalPosition > sliderWidth * 0.85f -> stopAlarm()
                        finalPosition < sliderWidth * 0.15f -> snoozeAlarm()
                        else -> {
                            // Reset thumb to center
                            v.animate().x((sliderWidth / 2f) - (v.width / 2f)).setDuration(200).start()
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun stopAlarm() {
        val serviceIntent = Intent(this, AlarmNotificationService::class.java).apply {
            action = "STOP_ALARM"
        }
        startService(serviceIntent)
        finish()
    }

    private fun snoozeAlarm() {
        val songName = binding.tvSongName.text.toString()
        val artistName = binding.tvArtistName.text.toString()
        val songUrl = intent.getStringExtra("SONG_URL") // Recuperar de nuevo para asegurar
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        val scheduler = AlarmScheduler(this)
        scheduler.snooze(alarmId, songName, songUrl, artistName, imageUrl)
        stopAlarm()
    }

    private fun showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}
