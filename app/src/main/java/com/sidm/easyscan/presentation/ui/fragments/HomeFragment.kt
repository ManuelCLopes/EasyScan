package com.sidm.easyscan.presentation.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sidm.easyscan.R
import com.sidm.easyscan.data.model.DocumentDTO
import com.sidm.easyscan.presentation.ui.LoginActivity
import java.io.ByteArrayOutputStream
import java.sql.Timestamp
import java.util.*
import android.R.attr.label

import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProvider
import com.sidm.easyscan.data.FirebaseViewModel


private const val REQUEST_IMAGE_CAPTURE = 100
private const val REQUEST_READ_STORAGE = 500

class HomeFragment : Fragment() {

    private var imageUri: Uri? = null
    private var processedText: String? = ""
    private lateinit var new_doc_id: String
    private lateinit var firebaseViewModel: FirebaseViewModel


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
            deleteDocument(new_doc_id)
            toggleNewDoc(view)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {

            val imageBitmap = data?.extras?.get("data") as Bitmap

            imageUri = context?.let { getImageUriFromBitmap(it, imageBitmap) }
            imageUri?.let {
                val image = InputImage.fromFilePath(requireContext(), it)
                processText(image)

            }
            uploadImageToFirebaseStorage()
            requireView().findViewById<ImageView>(R.id.iv_new_image_doc).setImageBitmap(imageBitmap)
        } else
            if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_READ_STORAGE) {
                imageUri = data?.data
                imageUri?.let { it ->
                    val image = InputImage.fromFilePath(requireContext(), it)

                    processText(image)
                    uploadImageToFirebaseStorage()
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
    private fun processText(inputImage: InputImage) {

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val view: View = requireView()
                processedText = visionText.text
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


    private fun uploadImageToFirebaseStorage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        imageUri?.let { it ->
            ref.putFile(it)
                .addOnSuccessListener {
                    Log.d("Register", "Successfully uploaded image: ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener {
                        saveImageInfoToFirebaseDatabase(it.toString())
                    }
                }
        }
    }

    private fun saveImageInfoToFirebaseDatabase(imageUrl: String) {

        val doc = hashMapOf(
            "user" to FirebaseAuth.getInstance().currentUser?.displayName,
            "timestamp" to "${Timestamp(System.currentTimeMillis())}",
            "image_url" to imageUrl,
            "processed_text" to processedText,
        )

        val database = FirebaseFirestore.getInstance()
        database.collection("DocumentCollection")
            .add(doc).addOnSuccessListener {
                new_doc_id = it.id
            }
    }

    private fun loadLastDocument() {
        firebaseViewModel.getLastDocument().observeOnce(this.requireActivity(), { documentDTO ->
            val view: View = requireView()
            view.findViewById<TextView>(R.id.tv_last_user).text = documentDTO.user
            view.findViewById<TextView>(R.id.tv_last_timestamp).text = documentDTO.timestamp
            view.findViewById<TextView>(R.id.tv_last_processed_text).text =
                documentDTO.processed_text


            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_dummy)
                .error(R.drawable.ic_dummy)

            Glide.with(view.context)
                .load(documentDTO.image_url)
                .apply(requestOptions)
                .into(view.findViewById(R.id.iv_last_image))

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

    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

}