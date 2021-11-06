package com.sidm.easyscan.presentation.ui.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sidm.easyscan.data.FirebaseRepository
import com.sidm.easyscan.data.model.DocumentDTO

class DocumentsViewModel(
    private val repository: FirebaseRepository
    ): ViewModel() {

    private val _docsViewModel = MutableLiveData<List<DocumentDTO>>()
    val docsLiveData = _docsViewModel


}