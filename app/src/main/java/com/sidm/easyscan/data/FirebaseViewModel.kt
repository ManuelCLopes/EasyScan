package com.sidm.easyscan.data

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.MetadataChanges
import com.sidm.easyscan.data.model.DocumentDTO
import java.sql.Timestamp
import java.util.*

class FirebaseViewModel: ViewModel() {

    private val tag = "FIREBASE_VIEW_MODEL"
    private val firebaseRepository = FirebaseRepository()
    var docs :MutableLiveData<List<DocumentDTO>> = MutableLiveData(listOf())
    private var lastDoc: MutableLiveData<DocumentDTO> = MutableLiveData()
    private var specificDoc: MutableLiveData<DocumentDTO> = MutableLiveData()

    fun getDocuments():MutableLiveData<List<DocumentDTO>> {

         firebaseRepository.getDocuments()
            .orderBy("timestamp")
             .whereEqualTo("user", FirebaseAuth.getInstance().currentUser!!.uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.w(tag, "Unable to retrieve data. Error=$e, snapshot=$snapshot")
                    return@addSnapshotListener
                }

                val result = mutableListOf<DocumentDTO>()

                for (document in snapshot.documents) {
                    val doc = DocumentDTO(
                        document.id,
                        "${document.data?.get("user")}",
                        "${document.data?.get("timestamp")}",
                        "${document.data?.get("image_url")}",
                        "${document.data?.get("processed_text")}",
                        "${document.data?.get("lines")}",
                        "${document.data?.get("words")}",
                        "${document.data?.get("language")}",
                        "",
                        "",
                        ""
                    )

                    result += doc

                }
                docs.postValue(result)
            }
        return docs
    }

    fun getLastDocument(): MutableLiveData<DocumentDTO> {
        firebaseRepository.getDocuments()
            .orderBy("timestamp").limitToLast(1)
            .whereEqualTo("user", FirebaseAuth.getInstance().currentUser!!.uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.w(tag, "Unable to retrieve data. Error=$e, snapshot=$snapshot")
                    return@addSnapshotListener
                }
                if (snapshot.documents.size != 0) {

                    val result = DocumentDTO(
                        snapshot.documents[0].id,
                        "${snapshot.documents[0].data?.get("user")}",
                        "${snapshot.documents[0].data?.get("timestamp")}",
                        "${snapshot.documents[0].data?.get("image_url")}",
                        "${snapshot.documents[0].data?.get("processed_text")}",
                        "${snapshot.documents[0].data?.get("lines")}",
                        "${snapshot.documents[0].data?.get("words")}",
                        "${snapshot.documents[0].data?.get("language")}",
                        "",
                        "",
                        ""
                    )
                    lastDoc.postValue(result)
                }
            }

        return lastDoc
    }

    fun getSpecificDocument(id: String): MutableLiveData<DocumentDTO> {
        firebaseRepository.getDocuments().document(id)
            .addSnapshotListener (MetadataChanges.INCLUDE){ doc, e ->
                if (e != null || doc == null) {
                    Log.w(tag, "Unable to retrieve data. Error=$e, snapshot=$doc")
                    return@addSnapshotListener
                }
                val result = DocumentDTO(
                    doc.id,
                    "${doc.data?.get("user")}",
                    "${doc.data?.get("timestamp")}",
                    "${doc.data?.get("image_url")}",
                    "${doc.data?.get("processed_text")}",
                    "${doc.data?.get("lines")}",
                    "${doc.data?.get("words")}",
                    "${doc.data?.get("language")}",
                    "${doc.data?.get("sentiment")}",
                    "${doc.data?.get("sentimentMagnitude")}",
                    "${doc.data?.get("classification")}"
                )
                specificDoc.postValue(result)
            }

        return specificDoc
    }

    fun createDocument(imageUri: Uri, processedText: String,lines: String, words: String, language: String, isOnline: Boolean) {
        val tempDoc = DocumentDTO(
            "",
            FirebaseAuth.getInstance().currentUser!!.uid,
            "${Timestamp(System.currentTimeMillis())}",
            imageUri.path.toString(),
            processedText,
            lines,
            words,
            language,
            "",
            "",
            ""
        )

        val filename = UUID.randomUUID().toString()
        firebaseRepository.createDocument(filename, imageUri, tempDoc, isOnline)
    }

    fun updateDocument(tempDoc: DocumentDTO){
        firebaseRepository.updateDocument(tempDoc)
    }

    fun deleteImageFromStorage(imageURL: String){
        firebaseRepository.deleteImageFromStorage(imageURL)
    }

    fun getSpecificDocumentReference(id:String): DocumentReference {
        return firebaseRepository.getDocuments().document(id)
    }

    fun deleteDocument(id: String): Task<Void> {

        return firebaseRepository.deleteDocument(id)
    }

}