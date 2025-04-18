package com.example.android.model

data class ContactModel(
    var name: String ?= null,
    var number: String ?= null,
    var key: String? = null // Add this line to hold Firebase key
)
