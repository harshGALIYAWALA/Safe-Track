package com.example.safetrack.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.safetrack.R
import com.example.safetrack.databinding.ActivityPrivacyPolicyScreenBinding

class privacy_policy_screen : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPolicyScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding =ActivityPrivacyPolicyScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.privacyActBtn.setOnClickListener{
            Toast.makeText(this, "you have accepted our policy", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}