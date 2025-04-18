package com.example.safetrack.model

import android.net.Uri

data class ContactModel(
    var name: String ?= null,
    var number: String ?= null,
    var key: String? = null // Add this line to hold Firebase key
)
