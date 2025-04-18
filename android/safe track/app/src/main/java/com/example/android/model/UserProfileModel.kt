package com.example.android.model

data class UserProfileModel(
    val firstName: String ?= null,
    val lastName: String ?= null,
    val email: String ?= null,
    val phoneNumber: String ?= null,
    val password: String ?= null,
    val gender: String ?= null
)
