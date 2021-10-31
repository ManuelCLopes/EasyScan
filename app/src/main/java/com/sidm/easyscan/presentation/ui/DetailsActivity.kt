package com.sidm.easyscan.presentation.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.Menu
import android.view.MenuItem
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

        findViewById<FloatingActionButton>(R.id.fab_edit).setOnClickListener {
            Toast.makeText(
                applicationContext,
                "TODO: Transform tv into TextInput",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(com.sidm.easyscan.R.menu.details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id: Int = item.itemId
        return if (id == com.sidm.easyscan.R.id.btn_delete) {
            Toast.makeText(
                applicationContext,
                "TODO: Delete doc",
                Toast.LENGTH_SHORT
            ).show()
            true
        } else super.onOptionsItemSelected(item)
    }

}