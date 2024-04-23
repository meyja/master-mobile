package com.example.master_mobile.model

data class StressData(
    val id: String,
    val lat: String,
    val lon: String,
    val stressValue: String,
    val timestamp: Long,
    val decibel: String
)