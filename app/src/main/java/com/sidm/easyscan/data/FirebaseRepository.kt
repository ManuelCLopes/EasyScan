package com.sidm.easyscan.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.sql.Timestamp
import java.util.*


class FirebaseRepository{

    val TAG = "FIREBASE_REPOSITORY"
    val firestoreDB = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser


    fun getDocuments(): CollectionReference {
        return firestoreDB.collection("DocumentCollection")//.orderBy("timestamp").limitToLast(10)
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


    fun getLastDocument() {
        val docs = firestoreDB.collection("DocumentCollection").orderBy("timestamp").limitToLast(1)
        docs.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                Log.w(TAG, "Unable to retrieve data. Error=$e, snapshot=$snapshot")
                return@addSnapshotListener
            }
        }
    }

    fun createDocument(filename: String, imageUri: Uri, processedText: String) {
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                Log.d("Register", "Successfully uploaded image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveImageInfoToFirebaseDatabase(uri.toString(), processedText)
                }
            }
    }

    private fun saveImageInfoToFirebaseDatabase(imageUrl: String, processedText: String){

        val doc = hashMapOf(
            "user" to user?.displayName,
            "timestamp" to "${Timestamp(System.currentTimeMillis())}",
            "image_url" to imageUrl,
            "processed_text" to processedText,
        )

        firestoreDB.collection("DocumentCollection")
            .add(doc).addOnSuccessListener {
                return@addOnSuccessListener
            }
    }
}