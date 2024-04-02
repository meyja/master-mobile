package com.example.master_mobile.model

import java.sql.Timestamp

data class StressData(
    val stressLevel: String,
    val lat: String,
    val lon: String
)

data class TempStressData(
    val _id: String,
    val lat: String,
    val lon: String,
    val dataPoint: String,
    val timestamp: String,
    val sessionId: String,
    val __v: String
)