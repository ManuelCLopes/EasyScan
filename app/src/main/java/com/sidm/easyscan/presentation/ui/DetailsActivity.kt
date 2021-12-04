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
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.sidm.easyscan.R
import com.sidm.easyscan.data.FirebaseViewModel
import com.sidm.easyscan.util.UtilFunctions
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt


class DetailsActivity : AppCompatActivity() {

    private lateinit var id: String
    private lateinit var imageUrl: String
    private val firebaseViewModel: FirebaseViewModel = FirebaseViewModel()
    private val utilFunctions: UtilFunctions = UtilFunctions()
    private var editMode: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val tv = findViewById<TextView>(R.id.tv_test)
        val et = findViewById<EditText>(R.id.et_test)
        val copyIcon = findViewById<ImageView>(R.id.ic_copy)
        id = intent.extras?.get("id").toString()

        firebaseViewModel.getSpecificDocument(id).observe(this, {documentDTO ->
            if(documentDTO.user == "null"){
                finish()
            }
            tv.text = documentDTO.processed_text
            findViewById<TextView>(R.id.tv_lines)?.text = documentDTO.lines
            findViewById<TextView>(R.id.tv_words)?.text = documentDTO.words
            findViewById<TextView>(R.id.tv_lang)?.text = documentDTO.language

            if(documentDTO.classification == "null" || documentDTO.classification == "[]"){
                findViewById<CardView>(R.id.card_classification).visibility = View.GONE
            }else{
                findViewById<CardView>(R.id.card_classification).visibility = View.VISIBLE
                findViewById<TextView>(R.id.tv_classification)?.text =
                    separateClassificationCategories(documentDTO.classification)
            }

            val slider =findViewById<Slider>(R.id.slider)
            slider.isEnabled = false
            if(documentDTO.sentiment == "null"){
                findViewById<CardView>(R.id.card_sentiment).visibility = View.GONE
            } else{
                findViewById<CardView>(R.id.card_sentiment).visibility = View.VISIBLE
                slider.value =documentDTO.sentiment.toFloat()
                slider.thumbRadius = (10 + 7 * documentDTO.sentimentMagnitude.toFloat()).roundToInt()
            }

            imageUrl = documentDTO.image_url
            val imageView = findViewById<ImageView>(R.id.iv_photo_details)
            Glide.with(baseContext)
                .load(documentDTO.image_url)
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
                copyIcon.visibility = View.VISIBLE
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
                editMode = !editMode
            }
            fabEdit.setOnClickListener {
                if(editMode){
                    documentDTO.processed_text = et.text.toString()
                    firebaseViewModel.updateDocument(documentDTO)
                    fabEdit.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_edit, null))
                    tv.text = et.text
                    tv.visibility = View.VISIBLE
                    fabClose.visibility = View.GONE
                    et.visibility = View.GONE
                    copyIcon.visibility = View.VISIBLE
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
                    Toast.makeText(applicationContext, "Content saved successfully!", Toast.LENGTH_LONG).show()
                }else{
                    fabEdit.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_check, null))
                    et.setText(tv.text)
                    tv.visibility = View.GONE
                    fabClose.visibility = View.VISIBLE
                    et.visibility = View.VISIBLE
                    copyIcon.visibility = View.GONE
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

            copyIcon.setOnClickListener {
                utilFunctions.copyToClipboard(this.applicationContext, this, tv.text as String)
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
        builder.setTitle(R.string.delete_dialog_title)
        builder.setMessage(R.string.delete_dialog_message)
        builder.apply {
            setPositiveButton(R.string.delete_dialog_action_ok) { _, _ ->
                deleteDocument()
                Toast.makeText(
                    applicationContext,
                    "Document deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            setNegativeButton(R.string.delete_dialog_action_cancel) { _, _ ->
            }
        }
        builder.create().show()
    }

    private fun deleteDocument() {

        firebaseViewModel.deleteImageFromStorage(imageUrl)
        firebaseViewModel.deleteDocument(id).addOnCompleteListener {
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

    private fun separateClassificationCategories(classificationOriginal: String): String{
        var text = classificationOriginal
        text = text.replace("[{", "")
        text = text.replace("{", "")
        text = text.replace("}", "")
        text = text.replace("]", "")
        text = text.replace("}]", "")
        text = text.replace("/", "")
        Log.d("category", text)

        val result: List<String> = text.split(",").map { it.trim() }
        var res: String
        var percentage = ""
        var resultString = ""
        for (i in result){
            val item: List<String> = i.split("=").map { it.trim() }
            if(item[0] == "confidence") {
                percentage = (BigDecimal(item[1].toDouble()).setScale(4, RoundingMode.HALF_EVEN).toFloat() * 100).toString() + "%"
            }else{
                res = item[1] + ": " + percentage
                resultString += if(resultString!=""){"\n"}else{""} + res
            }
        }
        return resultString
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
}