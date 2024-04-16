package com.example.master_mobile.model

data class StressData(
    val id: String,
    val lat: String,
    val lon: String,
    val stressValue: String,
    val timestamp: Long,
    val decibel: String
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