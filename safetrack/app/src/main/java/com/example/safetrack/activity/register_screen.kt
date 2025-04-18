package com.example.safetrack.activity

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.safetrack.databinding.ActivityRegisterScreenBinding
import com.example.safetrack.model.UserProfileModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class register_screen : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var email: String
    private lateinit var number: String
    private lateinit var password: String
    private lateinit var confirmPassword: String
    private lateinit var gender: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialize firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val arrayList = arrayOf("Select Gender", "Female", "Male", "others")
        val genderAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, arrayList)
        binding.genderSpinner.adapter = genderAdapter

        binding.registerbtn.setOnClickListener{

            firstName = binding.fNameID.text.toString().trim()
            lastName = binding.lNameID.text.toString().trim()
            email = binding.emailID.text.toString().trim()
            number = binding.numberID.text.toString().trim()
            password = binding.passwordToggle.text.toString().trim()
            confirmPassword = binding.confirmPasswordID.text.toString().trim()
            gender = binding.genderSpinner.selectedItem.toString()

           if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || number.isBlank() || password.isBlank() || confirmPassword.isBlank() || gender == "Select Gender"){
               Toast.makeText(this, "All fields must filled", Toast.LENGTH_SHORT).show()
           } else{
               createAccount(email, password)
               Log.d("userAccount", "in validation ?")
           }

        }

        // redirect to login screen
        binding.direactToLogin.setOnClickListener{
            startActivity(Intent(this, login_screen::class.java))
            finish()
        }

        binding.termAndCondition.setOnClickListener{
            startActivity(Intent(this, privacy_policy_screen::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.example.safetrack.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                Toast.makeText(this, "account is successfully created", Toast.LENGTH_SHORT).show()
                //save user data in Firebase realTime DB
                saveUserData()
                startActivity(Intent(this, login_screen::class.java))
                finish()
            } else{
                Toast.makeText(this, "account is successfully failed! ${task.exception?.message + task} " , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData() {
        firstName = binding.fNameID.text.toString().trim()
        lastName = binding.lNameID.text.toString().trim()
        email = binding.emailID.text.toString().trim()
        number = binding.numberID.text.toString().trim()
        password = binding.passwordToggle.text.toString().trim()
        gender = binding.genderSpinner.selectedItem.toString()

        val user = UserProfileModel(firstName, lastName, email, number, password, gender)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database.getReference("user").child(userId).setValue(user)
    }

}

