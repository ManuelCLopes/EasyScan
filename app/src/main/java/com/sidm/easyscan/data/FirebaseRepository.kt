package com.sidm.easyscan.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sidm.easyscan.data.model.DocumentDTO
import java.util.*


class FirebaseRepository{

    val TAG = "FIREBASE_REPOSITORY"
    val firestoreDB = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    fun getDocuments(): CollectionReference {
        return firestoreDB.collection("DocumentCollection")
    }

    fun deleteDocument(id: String) {
        val docs = firestoreDB.collection("DocumentCollection").document(id)
        docs.addSnapshotListener{snapshot, e ->
            val ref = snapshot?.data?.let { FirebaseStorage.getInstance().getReferenceFromUrl(
                it["image_url"]
                    .toString()) }
            ref?.delete()
        }
        docs.delete().addOnSuccessListener {
            Log.w(TAG, "DELETE SUCCESSFUL $id")
        }
    }

    fun createDocument(filename: String, imageUri: Uri, tempDoc: DocumentDTO) {
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveImageInfoToFirebaseDatabase(uri.toString(), tempDoc)
                }
            }
    }

    private fun saveImageInfoToFirebaseDatabase(imageUrl: String, tempDoc: DocumentDTO){

        val doc = hashMapOf(
            "user" to tempDoc.user,
            "timestamp" to tempDoc.timestamp,
            "image_url" to imageUrl,
            "processed_text" to tempDoc.processed_text,
            "blocks" to tempDoc.blocks,
            "lines" to tempDoc.lines,
            "words" to tempDoc.words,
            "language" to tempDoc.language
        )

        firestoreDB.collection("DocumentCollection")
            .add(doc)
            .addOnSuccessListener {
                return@addOnSuccessListener
            }
    }
    fun updateDocument(tempDoc: DocumentDTO){

        val doc = hashMapOf(
            "user" to tempDoc.user,
            "timestamp" to tempDoc.timestamp,
            "image_url" to tempDoc.image_url,
            "processed_text" to tempDoc.processed_text,
            "blocks" to tempDoc.blocks,
            "lines" to tempDoc.lines,
            "words" to tempDoc.words,
            "language" to tempDoc.language
        )

        firestoreDB.collection("DocumentCollection")
            .document(tempDoc.id).update(doc as Map<String, Any>)
            .addOnSuccessListener {
                return@addOnSuccessListener
            }
    }
}