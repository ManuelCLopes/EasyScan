package com.sidm.easyscan.data

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sidm.easyscan.data.model.DocumentDTO
import java.util.*


class FirebaseRepository{

    private val firestoreDB = FirebaseFirestore.getInstance()


    fun getDocuments(): CollectionReference {
        return firestoreDB.collection("DocumentCollection")
    }

    fun createDocument(filename: String, imageUri: Uri, tempDoc: DocumentDTO, isOnline: Boolean) {
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        val docRef = firestoreDB.collection("DocumentCollection").document()
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveImageInfoToFirebaseDatabase(uri.toString(), tempDoc, docRef)
                }
            }
        if(!isOnline){
            saveImageInfoToFirebaseDatabase(imageUri.toString(), tempDoc, docRef)
        }
    }

    private fun saveImageInfoToFirebaseDatabase(imageUrl: String, tempDoc: DocumentDTO, documentReference: DocumentReference){

        val doc = hashMapOf(
            "user" to tempDoc.user,
            "timestamp" to tempDoc.timestamp,
            "image_url" to imageUrl,
            "processed_text" to tempDoc.processed_text,
            "lines" to tempDoc.lines,
            "words" to tempDoc.words,
            "language" to tempDoc.language
        )

        documentReference
            .set(doc)
            .addOnSuccessListener {
                return@addOnSuccessListener
            }
    }
    fun updateDocument(tempDoc: DocumentDTO){

        val doc = hashMapOf(
            "timestamp" to tempDoc.timestamp,
            "processed_text" to tempDoc.processed_text,
        )

        firestoreDB.collection("DocumentCollection")
            .document(tempDoc.id).update(doc as Map<String, Any>)
            .addOnSuccessListener {
                return@addOnSuccessListener
            }
    }

    fun deleteImageFromStorage(imageURL: String){
        FirebaseStorage.getInstance().getReferenceFromUrl(imageURL).delete()
    }

    fun deleteDocument(id: String): Task<Void> {
        return firestoreDB.collection("DocumentCollection")
            .document(id).delete()
    }
}