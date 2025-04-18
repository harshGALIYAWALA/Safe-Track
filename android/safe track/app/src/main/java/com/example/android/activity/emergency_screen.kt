package com.example.android.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android.R
import com.example.android.databinding.ActivityEmergencyScreenBinding


class emergency_screen : AppCompatActivity() {


    private lateinit var binding: ActivityEmergencyScreenBinding
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 10000 // 60 seconds
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100) // Max volume beep  STREAM_ALARM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmergencyScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        //start the countdown when the user enter emergency button in main activity(home)
        startTimer()

        //cancel the emergency calling
        binding.cancelButton.setOnClickListener{
            countDownTimer?.cancel() // Cancel the countdown timer
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(this, "You have canceled the emergency calling", Toast.LENGTH_SHORT).show()
            finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun startTimer() {
        countDownTimer?.cancel() // Cancel any existing timer
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 50)
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000) // Beep for 200ms
            }

            override fun onFinish() {
                toneGenerator.release() // Stop the beep generator
                val phoneNumber = "tel:9106333100" // Emergency Number
                if (ContextCompat.checkSelfPermission(this@emergency_screen,  Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    val callIntent = Intent(Intent.ACTION_CALL)
                    callIntent.data = Uri.parse(phoneNumber)
                    startActivity(callIntent)
                } else {
                    // Request permission if not granted
                    ActivityCompat.requestPermissions(this@emergency_screen, arrayOf(
                        Manifest.permission.CALL_PHONE), 1)
                }
            }
        }.start()


    }

    private fun updateTimerText() {
        val seconds = (timeLeftInMillis / 1000).toInt()
        binding.countDown.text = seconds.toString()
        if (seconds == 0){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}