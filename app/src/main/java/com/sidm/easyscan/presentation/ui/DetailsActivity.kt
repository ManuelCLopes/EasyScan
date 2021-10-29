package com.sidm.easyscan.presentation.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sidm.easyscan.R




class DetailsActivity : AppCompatActivity() {

    private lateinit var processed_text: String
    private lateinit var image_url: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val b: Bundle? = intent.extras
        processed_text = b?.get("processed_text").toString()
        image_url = b?.get("image_url").toString()

        findViewById<TextView>(R.id.tv_test)?.text = processed_text

        val image_view = findViewById<ImageView>(R.id.iv_photo_details)

        Glide.with(this)
            .load(image_url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .into(image_view)


    }
}