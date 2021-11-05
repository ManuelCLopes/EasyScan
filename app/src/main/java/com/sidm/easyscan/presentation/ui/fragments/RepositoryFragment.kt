package com.sidm.easyscan.presentation.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
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
import com.sidm.easyscan.databinding.FragmentRepositoryBinding
import com.sidm.easyscan.presentation.adapters.DocumentsAdapter

private const val TAG = "RepositoryFragment"

class RepositoryFragment : Fragment(){

    private lateinit var binding: FragmentRepositoryBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentRepositoryBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onResume() {
        super.onResume()

        setup()
        loadDocuments()
    }

    private fun setup() {
        val linearLayoutManager = LinearLayoutManager(context)

        binding.rvMessages.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = DocumentsAdapter()
        }
    }

    private fun loadDocuments() {
        val docs = Firebase.firestore.collection("DocumentCollection").orderBy("timestamp").limitToLast(10)
        docs.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                Log.w(TAG, "Unable to retrieve data. Error=$e, snapshot=$snapshot")
                return@addSnapshotListener
            }

            val result = mutableListOf<DocumentDTO>()
            for (document in snapshot.documents) {
                val doc = DocumentDTO(
                    document.id,
                    "${document.data?.get("user")}",
                    "${document.data?.get("timestamp")}",
                    "${document.data?.get("image_url")}",
                    "${document.data?.get("processed_text")}"
                )

                result += doc
            }

            val adapter = binding.rvMessages.adapter as DocumentsAdapter
            adapter.submitList(result)
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {})

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id: Int = item.itemId
        return if (id == R.id.btn_sort) {
            Toast.makeText(
                requireContext(),
                "TODO: sort docs",
                Toast.LENGTH_SHORT
            ).show()
            true
        } else super.onOptionsItemSelected(item)
    }
}