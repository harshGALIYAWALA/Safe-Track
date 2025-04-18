package com.example.android.activity

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.R
import com.example.android.adapter.ContactAdapter
import com.example.android.databinding.ActivityContactScreenBinding
import com.example.android.model.ContactModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class contact_screen : AppCompatActivity() {

    private lateinit var binding : ActivityContactScreenBinding
    private lateinit var contactAdapter : ContactAdapter
    private val contactList : ArrayList<ContactModel> = ArrayList()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding =ActivityContactScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialize firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // fetching Contacts from firebase
        fetchContactsFromFirebase()



        binding.addContacts.setOnClickListener{
            pickContact()
        }

        binding.contactRecyclerView.layoutManager = LinearLayoutManager(this)
        contactAdapter = ContactAdapter(contactList)
        binding.contactRecyclerView.adapter = contactAdapter

        binding.backPress.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



    private val contactPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { contactUri ->
                    retrieveContactInfo(contactUri)
                }
            }
        }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        contactPickerLauncher.launch(intent)
    }


    private fun retrieveContactInfo(contactUri: Uri) {
        val cursor: Cursor? = contentResolver.query(
            contactUri,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val contact = ContactModel(name, number)
                saveContactToFirebase(contact)
            }
        } ?: Toast.makeText(this, "Failed to retrieve contact", Toast.LENGTH_SHORT).show()
    }

    private fun saveContactToFirebase(contact: ContactModel) {
        val userId = auth.currentUser?.uid
        if(userId != null) {
            val contactref = database.getReference("user").child(userId).child("contacts")
            val newContactRef = contactref.push()

            // assign unique key
            contact.key = newContactRef.key

            newContactRef.setValue(contact).addOnSuccessListener{
                contactList.add(contact)
                contactAdapter.notifyItemInserted(contactList.size - 1) // after adding new contact it notify adapter to make a new item block in recyclerview
                Toast.makeText(this, "Contact saved!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to save contact", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchContactsFromFirebase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val contactRef = database.getReference("user").child(userId).child("contacts")
            contactRef.addListenerForSingleValueEvent(object : ValueEventListener { // 🔥 Change here
                override fun onDataChange(snapshot: DataSnapshot) {
                    contactList.clear()
                    for (contactSnapshot in snapshot.children) {
                        val contact = contactSnapshot.getValue(ContactModel::class.java)
                        contact?.key = contactSnapshot.key // Capture key from Firebase
                        contact?.let { contactList.add(it) }
                    }
                    contactAdapter.notifyDataSetChanged()//  Notify adapter to refresh the UI

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@contact_screen, "Failed to load contacts", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


}