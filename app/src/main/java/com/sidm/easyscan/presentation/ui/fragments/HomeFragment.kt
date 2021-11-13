package com.sidm.easyscan.presentation.ui.fragments

import android.Manifest
import android.R.attr.label
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sidm.easyscan.R
import com.sidm.easyscan.data.FirebaseViewModel
import com.sidm.easyscan.presentation.ui.LoginActivity
import java.io.ByteArrayOutputStream
import java.io.OutputStream


private const val REQUEST_IMAGE_CAPTURE = 100
private const val REQUEST_READ_STORAGE = 500

class HomeFragment : Fragment() {

    private var imageUri: Uri? = null
    private var processedText: String? = ""
    private lateinit var firebaseViewModel: FirebaseViewModel
    private val languageIdentification = LanguageIdentification.getClient()
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        loadLastDocument()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<FloatingActionButton>(R.id.fab_camera).setOnClickListener {
            openNativeCamera()
        }

        view.findViewById<FloatingActionButton>(R.id.fab_upload).setOnClickListener {
            selectImageFromGallery()
        }

        view.findViewById<ImageView>(R.id.btn_copy).setOnClickListener {
            copyToClipboard(this.processedText.toString())
        }

        view.findViewById<ImageView>(R.id.btn_edit).setOnClickListener {
            Toast.makeText(
                requireContext(),
                "TODO: Transform tv into TextInput",
                Toast.LENGTH_SHORT
            ).show()
        }

        view.findViewById<ImageView>(R.id.btn_delete).setOnClickListener {
            firebaseViewModel.getLastDocument().observeOnce(this.requireActivity(), {
                deleteDocument(it.id)
            })
            toggleNewDoc(view)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            imageUri = context?.let { getImageUriFromBitmap(imageBitmap) }
            imageUri?.let {
                processImage(it)

                requireView().findViewById<ImageView>(R.id.iv_new_image_doc)
                    .setImageBitmap(imageBitmap)
            }
        } else
            if (requestCode == REQUEST_READ_STORAGE && resultCode == AppCompatActivity.RESULT_OK) {
                imageUri = data?.data
                imageUri?.let {
                    processImage(it)

                    requireView().findViewById<ImageView>(R.id.iv_new_image_doc)
                        .setImageURI(imageUri)// handle chosen image
                }
            }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun toggleNewDoc(view: View) {
        if (view.findViewById<LinearLayout>(R.id.new_doc).visibility == View.GONE) {
            view.findViewById<LinearLayout>(R.id.new_doc).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.tv_title).visibility = View.VISIBLE
        } else {
            view.findViewById<LinearLayout>(R.id.new_doc).visibility = View.GONE
            view.findViewById<TextView>(R.id.tv_title).visibility = View.GONE
        }

    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager? =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(label.toString(), text)
        clipboard?.setPrimaryClip(clip)

        Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_LONG).show()

    }

    /**
     * Calling this method will open the default camera application.
     */
    private fun openNativeCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    /**
     * Method that returns processed text from an image
     */
    private fun processImage(imageUri: Uri){

        val inputImage = InputImage.fromFilePath(requireContext(), imageUri)
        var blocks: Int
        var nLines = 0
        var nWords = 0
        var language: String? = ""
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                processedText = visionText.text
                blocks = visionText.textBlocks.size
                for (i in visionText.textBlocks.indices) {
                    val lines = visionText.textBlocks[i].lines
                    nLines += lines.size
                    for (j in lines.indices) {
                        val words = lines[j].elements
                        nWords += words.size
                    }
                }
                languageIdentification.identifyLanguage(visionText.text)
                    .addOnSuccessListener {
                        language = it.toString()
                        firebaseViewModel.createDocument(imageUri, processedText!!, blocks.toString(), nLines.toString(), nWords.toString(), language!!)
                    }
                val view: View = requireView()
                toggleNewDoc(view)
                view.findViewById<TextView>(R.id.tv_new_processed_text).text = processedText
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Log.e("Error", e.message.toString())
            }
    }

    /**
     * Calling this method will open the gallery.
     */
    private fun selectImageFromGallery() {

        if (!checkPermissionAndRequest()) {
            return
        }
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_READ_STORAGE)
    }

    private fun loadLastDocument() {
        firebaseViewModel.getLastDocument().observeOnce(this.requireActivity(), { documentDTO ->
            documentDTO?.let {
                val view: View = requireView()
                view.findViewById<TextView>(R.id.tv_last_timestamp).text = documentDTO.timestamp
                view.findViewById<TextView>(R.id.tv_last_processed_text).text =
                    if(documentDTO.processed_text.length > 20)
                        (documentDTO.processed_text.subSequence(0, 20).toString() + "...")
                    else
                        documentDTO.processed_text


                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_dummy)
                    .error(R.drawable.ic_dummy)

                Glide.with(view.context)
                    .load(documentDTO.image_url)
                    .apply(requestOptions)
                    .into(view.findViewById(R.id.iv_last_image))
            }
        })
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    /**
     * Permissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_READ_STORAGE) {

            if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                selectImageFromGallery()
            }
        } else {

            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun checkPermissionAndRequest(): Boolean {

        if (ContextCompat
                .checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_STORAGE
            )

            return false
        }

        return true
    }

    private fun deleteDocument(id: String) {
        firebaseViewModel.deleteDocument(id)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id: Int = item.itemId
        return if (id == R.id.btn_logout) {
            showAppDialog()
            true
        } else super.onOptionsItemSelected(item)
    }

    /**
     * Calling this method will show a dialog.
     */
    private fun showAppDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.apply {
            setPositiveButton("Logout") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finish()
            }
            setNegativeButton("Cancel") { _, _ ->
                Log.d("TAG", "Dialog cancelled")
            }
        }
        builder.create().show()
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) saveImageInQ(bitmap)
        else saveImageOldSdk(bitmap)

    }

    private fun saveImageInQ(bitmap: Bitmap):Uri {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream?
        var imageUri: Uri?
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val contentResolver = context?.contentResolver

        contentResolver.also { resolver ->
            imageUri = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver?.openOutputStream(it) }
        }

        fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }

        contentValues.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
        }
        contentResolver?.update(imageUri!!, contentValues, null, null)

        return imageUri!!
    }

    private fun saveImageOldSdk(bitmap:Bitmap): Uri{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

}