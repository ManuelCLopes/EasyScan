package com.sidm.easyscan.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.*
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sidm.easyscan.R
import com.sidm.easyscan.util.UtilFunctions
import java.io.ByteArrayOutputStream

class ImageProcessing {
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("europe-west3")
    private val languageIdentification = LanguageIdentification.getClient()
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val utilFunctions: UtilFunctions = UtilFunctions()


    fun getTextOnline(imageUri: Uri, imageBitmap: Bitmap, firebaseViewModel: FirebaseViewModel, view: View) {
        // Convert bitmap to base64 encoded string
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        val base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        // Create json request to cloud vision
        val request = JsonObject()
        // Add image to request
        val image = JsonObject()
        image.add("content", JsonPrimitive(base64encoded))
        request.add("image", image)
        //Add features to the request
        val feature = JsonObject()
        feature.add("type", JsonPrimitive("TEXT_DETECTION"))
        val features = JsonArray()
        features.add(feature)
        request.add("features", features)

        var processedText: String? = ""
        var nLines = 0
        var nWords = 0

        analyzeImage(request.toString())
            .addOnFailureListener { exception ->
                Log.d("gcloud functions", exception.message.toString())
            }
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d("gcloud functions", task.result.toString())
                } else {
                    if (!task.result.asJsonArray[0].asJsonObject.has("fullTextAnnotation")) {
                        Toast.makeText(
                            view.context,
                            "Couldn't read any text from the image. Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                        utilFunctions.toggleProgressCircle(view)
                    } else {
                        val annotation =
                            task.result.asJsonArray[0].asJsonObject["fullTextAnnotation"].asJsonObject
                        processedText = annotation["text"].asString
                        for (page in annotation["pages"].asJsonArray) {
                            for (block in page.asJsonObject["blocks"].asJsonArray) {
                                for (para in block.asJsonObject["paragraphs"].asJsonArray) {
                                    nLines += 1
                                    for (word in para.asJsonObject["words"].asJsonArray) {
                                        nWords += 1
                                    }
                                }
                            }
                        }

                        val a = analyzeSentiment(processedText!!)
                        Log.d("gcloud functions", a.toString())


                        languageIdentification.identifyLanguage(processedText.toString())
                            .addOnSuccessListener { lang ->
                                firebaseViewModel.createDocument(
                                    imageUri,
                                    processedText!!,
                                    "",
                                    nLines.toString(),
                                    nWords.toString(),
                                    lang
                                )
                                utilFunctions.toggleNewDoc(view)
                                view.findViewById<TextView>(R.id.tv_new_processed_text).text =
                                    processedText
                            }
                    }
                }
            }
    }

    fun getTextOffline(context: Context, imageUri: Uri, firebaseViewModel: FirebaseViewModel, view: View) {

        val inputImage = InputImage.fromFilePath(context, imageUri)
        var nLines = 0
        var nWords = 0
        var processedText: String? = ""

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (visionText.text == "") {
                    Toast.makeText(
                        view.context,
                        "Couldn't read any text from the image. Please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                    utilFunctions.toggleProgressCircle(view)
                } else {

                    processedText = visionText.text
                    for (i in visionText.textBlocks.indices) {
                        val lines = visionText.textBlocks[i].lines
                        nLines += lines.size
                        for (j in lines.indices) {
                            val words = lines[j].elements
                            nWords += words.size
                        }
                    }

                    languageIdentification.identifyLanguage(visionText.text)
                        .addOnSuccessListener { lang ->
                            firebaseViewModel.createDocument(
                                imageUri,
                                processedText!!,
                                "",
                                nLines.toString(),
                                nWords.toString(),
                                lang
                            )
                            utilFunctions.toggleNewDoc(view)
                            view.findViewById<TextView>(R.id.tv_new_processed_text).text =
                                processedText
                        }
                        .addOnFailureListener { e ->
                            Log.e("Error", e.message.toString())
                        }
                }
            }
    }

    private fun analyzeSentiment(originalText: String){
        // Create json request to cloud vision
        val request = JsonObject()
        val text = JsonObject()
        text.add("content", JsonPrimitive(originalText))
        request.add("text", text)
        analyzeText(request.toString())
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d("gcloud functions", "Failed reading")
                } else {
                    Log.d("gcloud functions", task.result.toString())
                }
            }
    }

    private fun analyzeImage(requestJson: String): Task<JsonElement> {
        return functions
            .getHttpsCallable("analyzeImage")
            .call(requestJson)
            .continueWith { task ->
                val result = task.result.data
                JsonParser.parseString(Gson().toJson(result))
            }
    }

    private fun analyzeText(requestJson: String): Task<JsonElement> {
        return functions
            .getHttpsCallable("analyzeSentiment")
            .call(requestJson)
            .continueWith { task ->
                val result = task.result.data
                JsonParser.parseString(Gson().toJson(result))
            }
    }
}