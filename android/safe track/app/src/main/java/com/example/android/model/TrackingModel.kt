package com.example.android.model

data class TrackingModel(
    var latitude: String ?= null,
    var longitude: String ?= null,
    var timestamp: String ?= null,
    var isSharing: Boolean ?= null,
)
