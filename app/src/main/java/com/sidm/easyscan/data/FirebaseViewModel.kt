package com.sidm.easyscan.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sidm.easyscan.data.model.DocumentDTO

class FirebaseViewModel: ViewModel() {

    val TAG = "FIREBASE_VIEW_MODEL"
    val firebaseRepository = FirebaseRepository()
    var docs :MutableLiveData<List<DocumentDTO>> = MutableLiveData()

    fun getDocuments(): MutableLiveData<List<DocumentDTO>> {

        firebaseRepository.loadDocuments()
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

}