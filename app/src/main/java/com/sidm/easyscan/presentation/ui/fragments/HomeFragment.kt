package com.sidm.easyscan.presentation.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import android.graphics.Point
import android.graphics.Rect
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.app
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.*
import com.sidm.easyscan.R
import java.net.URI
import java.sql.Timestamp


private const val TAG = "HomeFragment"
private const val REQUEST_IMAGE_CAPTURE = 100
private const val REQUEST_READ_STORAGE = 500

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.open_camera).setOnClickListener {
            openNativeCamera()
        }

        view.findViewById<Button>(R.id.show_dialog).setOnClickListener {
            showAppDialog()
        }

        view.findViewById<Button>(R.id.show_snackbar).setOnClickListener {
            showAppSnackbar()
        }

        view.findViewById<Button>(R.id.UploadButton).setOnClickListener {
            uploadImage()
        }

        Glide.with(this)
            .load("https://github.com/android-training-program/aula-5/blob/master/imagens/fifi.jpg?raw=true")
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .into(view.findViewById(R.id.imageView))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {

            val imageBitmap = data?.extras?.get("data") as Bitmap

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(imageBitmap, 0)

            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    requireView().findViewById<TextView>(R.id.tv_hello).text = visionText.text
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }

            requireView().findViewById<ImageView>(R.id.imageView).setImageBitmap(imageBitmap)
        }else
            if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_READ_STORAGE){
                val image = data?.data
                if(image != null) {
                    requireView().findViewById<ImageView>(R.id.imageView)
                        .setImageURI(data.data)// handle chosen image
                    uploadImageFirebase(image)
                }


            }

        super.onActivityResult(requestCode, resultCode, data)
    }


    /**
     * Calling this method will open the default camera application.
     */
    private fun openNativeCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)

    }


    /**
     * Calling this method will open the gallery.
     */
    private fun uploadImage() {

        if(!checkPermissionAndRequest()){
            return
        }
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_READ_STORAGE)
    }


    private fun uploadImageFirebase(file: Uri){
        val storage = Firebase.storage
        val rootRef = storage.reference

        val riversRef = rootRef.child("images/${Timestamp(System.currentTimeMillis()).toString() 
                + "_" + (FirebaseAuth.getInstance().currentUser?.displayName)}")
        val uploadTask = riversRef.putFile(file)

        //Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            Log.e("upload teste", "Error Uploading", it)
        }.addOnSuccessListener {taskSnapshot ->
            //taskSnapshot.metadata contains  file metadata such as size, content-type, etc...
            Toast.makeText(requireContext(), "File uploaded!", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Permissions
     */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == REQUEST_READ_STORAGE){

            if(permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                uploadImage()
            }
        } else {

            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun checkPermissionAndRequest(): Boolean{

        if(ContextCompat
                .checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){

            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_STORAGE)

            return false
        }

        return true
    }


    /**
     * Calling this method will show a dialog.
     */
    private fun showAppDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.dialog_title)
        builder.setMessage(R.string.dialog_message)
        builder.apply {
            setPositiveButton(R.string.dialog_action_ok) { _, _ ->
                Toast.makeText(
                    requireContext(),
                    R.string.dialog_action_ok_selected,
                    Toast.LENGTH_SHORT
                ).show()
            }
            setNegativeButton(R.string.dialog_action_cancel) { _, _ ->
                Log.d(TAG, "Dialog cancelled")
            }
        }
        builder.create().show()
    }

    /**
     * Calling this method will show a snackbar.
     */
    private fun showAppSnackbar() {
        Snackbar.make(
            requireView(),
            R.string.snackbar_message,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.snackbar_action_thanks) {
                Toast.makeText(
                    requireContext(),
                    R.string.snackbar_action_thanks_selected,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }
}