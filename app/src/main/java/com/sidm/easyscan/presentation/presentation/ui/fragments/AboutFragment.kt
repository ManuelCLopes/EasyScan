package com.sidm.easyscan.presentation.presentation.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sidm.easyscan.databinding.FragmentChatBinding
import com.sidm.easyscan.presentation.data.model.Document
import com.sidm.easyscan.presentation.presentation.adapters.DocumentsAdapter

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

        binding.btnSend.setOnClickListener {
            val message = binding.etContent.text.toString()
            sendMessage(message)

            binding.etContent.text.clear()
        }
    }

    private fun loadDocuments() {
        val docs = Firebase.firestore.collection("DocumentCollection").orderBy("timestamp")
        docs.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                Log.w(TAG, "Unable to retrieve data. Error=$e, snapshot=$snapshot")
                return@addSnapshotListener
            }

            Log.d(TAG, "New data retrieved:${snapshot.documents.size}")

            val messages = mutableListOf<Document>()
            for (document in snapshot.documents) {
                val message = Document(
                    document.id,
                    "${document.data?.get("user")}",
                    "${document.data?.get("photo")}",
                    "${document.data?.get("timestamp")}"
                )

                messages += message
            }

            for (message in messages) {
                Log.d(TAG, message.toString())
            }

            val adapter = binding.rvMessages.adapter as DocumentsAdapter

            adapter.submitList(messages)
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            })

        }
    }

    private fun sendMessage(content: String) {
        /*
        val message = hashMapOf(
            "user" to String,
            "photo" to content,
            "timestamp" to "${System.currentTimeMillis()}"
        )

        val db = Firebase.firestore
        val id: String = db.collection("collection_name").document().id
        db.collection("DocumentsCollection").document(id)
            .set(message)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document. Error:$e") }


         */
        Toast.makeText(requireContext(), "Not implemented", Toast.LENGTH_SHORT).show()
    }


}