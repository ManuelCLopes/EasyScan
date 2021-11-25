package com.sidm.easyscan.presentation.ui.fragments

import android.Manifest
import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.sidm.easyscan.R
import com.sidm.easyscan.data.FirebaseViewModel
import com.sidm.easyscan.data.ImageProcessing
import com.sidm.easyscan.presentation.ui.LoginActivity
import com.sidm.easyscan.util.UtilFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val REQUEST_IMAGE_CAPTURE = 100
private const val REQUEST_READ_STORAGE = 500

class HomeFragment : Fragment() {

    private var imageUri: Uri? = null
    private var processedText: String? = ""
    private lateinit var firebaseViewModel: FirebaseViewModel
    private val imageProcessing: ImageProcessing = ImageProcessing()
    private val utilFunctions: UtilFunctions = UtilFunctions()
    private var spinner: ProgressBar? = null

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
        spinner = requireView().findViewById(R.id.progressBar)


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
            spinner!!.visibility = View.GONE

            utilFunctions.toggleNewDoc(view)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val scaledBitmap = utilFunctions.scaleBitmapDown(imageBitmap, 640)
            imageUri = context?.let { utilFunctions.getImageUriFromBitmap(requireContext(), scaledBitmap) }
            imageUri?.let {
                spinner!!.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.IO).launch {
                    processImage(it, scaledBitmap)
                }
                requireView().findViewById<ImageView>(R.id.iv_new_image_doc)
                    .setImageBitmap(scaledBitmap)
            }
        } else
            if (requestCode == REQUEST_READ_STORAGE && resultCode == AppCompatActivity.RESULT_OK) {
                imageUri = data?.data
                val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, imageUri!!))
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                }
                val scaledBitmap = utilFunctions.scaleBitmapDown(imageBitmap, 640)

                imageUri?.let {
                    spinner!!.visibility = View.VISIBLE
                    processImage(it, scaledBitmap)

                    requireView().findViewById<ImageView>(R.id.iv_new_image_doc)
                        .setImageURI(imageUri)// handle chosen image

                }
            }
        super.onActivityResult(requestCode, resultCode, data)
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
    private fun processImage(imageUri: Uri, imageBitmap: Bitmap?) {
        val context = requireContext()

        if (!utilFunctions.isOnline(context)) {
            imageProcessing.getTextOffline(context, imageUri, firebaseViewModel, requireView())
        } else {
            imageProcessing.getTextOnline(imageUri, imageBitmap!!, firebaseViewModel, requireView())
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
            showLogoutDialog()
            true
        } else super.onOptionsItemSelected(item)
    }

    /**
     * Calling this method will show a dialog.
     */
    private fun showLogoutDialog() {
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

}