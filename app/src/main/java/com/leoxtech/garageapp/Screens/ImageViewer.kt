package com.leoxtech.garageapp.Screens

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.leoxtech.garageapp.R
import com.leoxtech.garageapp.databinding.ActivityImageViewerBinding

class ImageViewer : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val image = intent.getStringExtra("image")

        Glide.with(this).load(Uri.parse(image)).into(binding.imageView)

        binding.cardBack.setOnClickListener {
            finish()
        }

    }
}