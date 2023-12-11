package com.example.master_mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.master_mobile.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}