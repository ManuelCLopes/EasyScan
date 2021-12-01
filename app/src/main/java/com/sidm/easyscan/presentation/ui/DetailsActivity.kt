package com.sidm.easyscan.presentation.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.sidm.easyscan.R
import com.sidm.easyscan.data.FirebaseViewModel


class DetailsActivity : AppCompatActivity() {

    private lateinit var id: String
    private val firebaseViewModel: FirebaseViewModel = FirebaseViewModel()

    private var editMode: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val tv = findViewById<TextView>(R.id.tv_test)
        val et = findViewById<EditText>(R.id.et_test)
        id = intent.extras?.get("id").toString()
        firebaseViewModel.getSpecificDocument(id).observe(this, {documentDTO ->
            tv.text = documentDTO.processed_text

            findViewById<TextView>(R.id.tv_lines)?.text = documentDTO.lines
            findViewById<TextView>(R.id.tv_words)?.text = documentDTO.words
            findViewById<TextView>(R.id.tv_lang)?.text = documentDTO.language

            findViewById<TextView>(R.id.tv_sentiment)?.text = documentDTO.sentiment
            findViewById<TextView>(R.id.tv_sentiment_score)?.text = documentDTO.sentimentMagnitude
            findViewById<TextView>(R.id.tv_classification)?.text = documentDTO.classification

            val imageView = findViewById<ImageView>(R.id.iv_photo_details)

            Glide.with(this)
                .load(documentDTO.image_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(imageView)

            val fabEdit = findViewById<FloatingActionButton>(R.id.fab_edit)
            val fabClose = findViewById<FloatingActionButton>(R.id.fab_close)

            fabClose.setOnClickListener {

                fabEdit.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_edit,
                        null
                    )
                )
                tv.visibility = View.VISIBLE
                fabClose.visibility = View.GONE
                et.visibility = View.GONE

                editMode = !editMode
            }

            fabEdit.setOnClickListener {
                if(editMode){
                    documentDTO.processed_text = et.text.toString()
                    firebaseViewModel.updateDocument(documentDTO)
                    fabEdit.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_edit, null))
                    tv.visibility = View.VISIBLE
                    fabClose.visibility = View.GONE
                    et.visibility = View.GONE
                    Toast.makeText(applicationContext, "Content saved successfully!", Toast.LENGTH_LONG).show()
                }else{
                    fabEdit.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_check, null))
                    et.setText(tv.text)
                    tv.visibility = View.GONE
                    fabClose.visibility = View.VISIBLE
                    et.visibility = View.VISIBLE
                    et.requestFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
                }
                editMode = !editMode
            }

            findViewById<ImageView>(R.id.iv_photo_details).setOnClickListener {
                zoomImage(documentDTO.image_url)
            }

            findViewById<ImageView>(R.id.expanded_image).setOnClickListener {
                it.visibility = View.GONE
                fabEdit.visibility = View.VISIBLE
                fabEdit.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_edit,
                        null
                    )
                )
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.details_appbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id: Int = item.itemId
        return if (id == R.id.btn_delete) {
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

    private fun zoomImage(imageResUrl: String) {

        val expandedImageView: ImageView = findViewById(R.id.expanded_image)
        Glide.with(this)
            .load(imageResUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .into(expandedImageView)


        if(editMode){
            val et = findViewById<EditText>(R.id.et_test)
            findViewById<TextView>(R.id.tv_test).visibility = View.VISIBLE
            et.visibility = View.GONE
            findViewById<FloatingActionButton>(R.id.fab_close).visibility = View.GONE
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
            editMode = !editMode
        }
        findViewById<FloatingActionButton>(R.id.fab_edit).visibility = View.GONE
        expandedImageView.visibility = View.VISIBLE
    }

}