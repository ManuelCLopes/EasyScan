package com.sidm.easyscan.presentation.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class DetailsActivity : AppCompatActivity() {

    private lateinit var processed_text: String
    private lateinit var image_url: String
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.sidm.easyscan.R.layout.activity_details)

        val b: Bundle? = intent.extras
        processed_text = b?.get("processed_text").toString()
        image_url = b?.get("image_url").toString()
        id = b?.get("id").toString()

        findViewById<TextView>(com.sidm.easyscan.R.id.tv_test)?.text = processed_text
        findViewById<TextView>(com.sidm.easyscan.R.id.et_test)?.text = processed_text

        val image_view = findViewById<ImageView>(com.sidm.easyscan.R.id.iv_photo_details)

        Glide.with(this)
            .load(image_url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .into(image_view)

        findViewById<FloatingActionButton>(com.sidm.easyscan.R.id.fab_edit).setOnClickListener {
            val switcher = findViewById<View>(com.sidm.easyscan.R.id.my_switcher) as ViewSwitcher
            switcher.showNext() //or switcher.showPrevious();

            val myTV = switcher.findViewById<View>(com.sidm.easyscan.R.id.tv_test) as TextView
            myTV.text = processed_text
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(com.sidm.easyscan.R.menu.details_appbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id: Int = item.itemId
        return if (id == com.sidm.easyscan.R.id.btn_delete) {
            showAppDialog()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun showAppDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(com.sidm.easyscan.R.string.delete_dialog_title)
        builder.setMessage(com.sidm.easyscan.R.string.delete_dialog_message)
        builder.apply {
            setPositiveButton(com.sidm.easyscan.R.string.delete_dialog_action_ok) { _, _ ->
                deleteDocument(id)
                Toast.makeText(
                    applicationContext,
                    "Document deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            setNegativeButton(com.sidm.easyscan.R.string.delete_dialog_action_cancel) { _, _ ->
            }
        }
        builder.create().show()
    }

    private fun deleteDocument(id: String) {
        val docs = Firebase.firestore.collection("DocumentCollection").document(id)
        docs.addSnapshotListener{snapshot, e ->
            val ref = snapshot?.data?.let { FirebaseStorage.getInstance().getReferenceFromUrl(
                it["image_url"]
                    .toString()) }
            ref?.delete()
        }
        docs.delete().addOnSuccessListener {
            Log.w("TAG", "DELETE SUCCESSFUL $id")
            finish()
        }
    }

}