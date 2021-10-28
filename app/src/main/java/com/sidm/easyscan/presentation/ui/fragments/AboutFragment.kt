package com.sidm.easyscan.presentation.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sidm.easyscan.R
import com.sidm.easyscan.data.model.DocumentDTO
import com.sidm.easyscan.databinding.FragmentChatBinding
import com.sidm.easyscan.presentation.adapters.DocumentsAdapter

private const val TAG = "AboutFragment"

class AboutFragment : Fragment(){

    private lateinit var binding: FragmentChatBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onResume() {
        super.onResume()

        setup()
        loadDocuments()
    }

    private fun setup() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true

        binding.rvMessages.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = DocumentsAdapter()
        }


    }

    private fun loadDocuments() {
        val docs = Firebase.firestore.collection("DocumentCollection").orderBy("timestamp")
        docs.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                Log.w(TAG, "Unable to retrieve data. Error=$e, snapshot=$snapshot")
                return@addSnapshotListener
            }

            val messages = mutableListOf<DocumentDTO>()
            for (document in snapshot.documents) {
                val message = DocumentDTO(
                    "${document.data?.get("user")}",
                    "${document.data?.get("timestamp")}",
                    "${document.data?.get("image_url")}",
                    "${document.data?.get("processed_text")}"
                )

                messages += message
            }

            val adapter = binding.rvMessages.adapter as DocumentsAdapter

            adapter.submitList(messages)
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {})

        }
    }

}