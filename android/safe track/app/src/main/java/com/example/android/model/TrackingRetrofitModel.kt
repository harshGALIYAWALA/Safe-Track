package com.example.android.model

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class CreatedAt(
    val date: String,
    val time: String
)

data class TrackingRetrofitModel(
    val uid: String,
    val location: Location?,
    val createdAt: CreatedAt?
)
