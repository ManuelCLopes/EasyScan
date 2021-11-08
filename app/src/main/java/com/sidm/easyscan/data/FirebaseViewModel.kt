package com.sidm.easyscan.data

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sidm.easyscan.data.model.DocumentDTO
import java.util.*

class FirebaseViewModel: ViewModel() {

    private val TAG = "FIREBASE_VIEW_MODEL"
    private val firebaseRepository = FirebaseRepository()
    private var docs :MutableLiveData<List<DocumentDTO>> = MutableLiveData()
    private var lastDoc: MutableLiveData<DocumentDTO> = MutableLiveData()

    fun getDocuments(): MutableLiveData<List<DocumentDTO>> {

        firebaseRepository.getDocuments()
            .orderBy("timestamp").limitToLast(10)
            .addSnapshotListener { snapshot, e ->
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
                docs.postValue(result)
            }
        Log.d("docs", docs.toString())
        return docs
    }

    fun getLastDocument(): MutableLiveData<DocumentDTO> {
        firebaseRepository.getDocuments()
            .orderBy("timestamp").limitToLast(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.w(TAG, "Unable to retrieve data. Error=$e, snapshot=$snapshot")
                    return@addSnapshotListener
                }

                val result = DocumentDTO(
                    snapshot.documents[0].id,
                    "${snapshot.documents[0].data?.get("user")}",
                    "${snapshot.documents[0].data?.get("timestamp")}",
                    "${snapshot.documents[0].data?.get("image_url")}",
                    "${snapshot.documents[0].data?.get("processed_text")}"
                )
                lastDoc.postValue(result)
            }

        return lastDoc
    }

    fun deleteDocument(id: String) {
        firebaseRepository.deleteDocument(id)
    }

    fun createDocument(imageUri: Uri, processedText: String){
        val filename = UUID.randomUUID().toString()
        firebaseRepository.createDocument(filename, imageUri, processedText)
    }

}