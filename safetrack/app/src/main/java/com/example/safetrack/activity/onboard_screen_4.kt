package com.example.safetrack.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.safetrack.R
import com.example.safetrack.databinding.ActivityOnboardScreen4Binding

class onboard_screen_4 : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardScreen4Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnboardScreen4Binding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.nextBtn.setOnClickListener {
            val intent = Intent(this, register_screen::class.java)
            startActivity(intent)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}