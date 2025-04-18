package com.example.safetrack.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.safetrack.R
import com.example.safetrack.databinding.ActivitySplashScreeenBinding

class Splash_screeen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreeenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreeenBinding.inflate(layoutInflater)
        setContentView(binding.root)





        // Load animation for circle expansion
        val expandAnimation = AnimationUtils.loadAnimation(this, R.anim.expand_circle)

        // Start expanding the circle
        binding.circleView.startAnimation(expandAnimation)

        // Delay the text appearance after circle expansion
        binding.circleView.postDelayed({
            binding.appNameText.visibility = View.VISIBLE // Instantly show text
        }, 600) // Text appears when expansion is complete

        // Move to MainActivity after a delay
        binding.appNameText.postDelayed({
            startActivity(Intent(this, onBoard_screen_1::class.java))
            finish()
        }, 2500)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}