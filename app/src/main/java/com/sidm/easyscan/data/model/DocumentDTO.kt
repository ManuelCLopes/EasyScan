package com.sidm.easyscan.data.model


data class DocumentDTO(
    val user: String,
    val timestamp: String,
    val image_url: String,
    val processed_text: String)