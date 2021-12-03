package com.sidm.easyscan.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sidm.easyscan.data.FirebaseViewModel
import com.sidm.easyscan.databinding.FragmentRepositoryBinding
import com.sidm.easyscan.presentation.adapters.DocumentsAdapter

class HistoryFragment : Fragment(){

    private lateinit var binding: FragmentRepositoryBinding
    private lateinit var firebaseViewModel: FirebaseViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        binding = FragmentRepositoryBinding.inflate(layoutInflater)
        setup()
        loadDocuments()
        return binding.root
    }

    private fun setup() {
        val linearLayoutManager = LinearLayoutManager(context)

        binding.rvDocuments.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = DocumentsAdapter()
        }
    }

    private fun loadDocuments() {
        val adapter = binding.rvDocuments.adapter as DocumentsAdapter
        firebaseViewModel.getDocuments().observe(requireActivity(), {
            adapter.submitList(it)
        })
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        })
    }
}