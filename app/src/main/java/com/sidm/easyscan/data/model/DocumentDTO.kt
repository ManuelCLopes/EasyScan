package com.sidm.easyscan.data.model


data class DocumentDTO(
    val id: String,
    val user: String,
    val timestamp: String,
    val image_url: String,
    val processed_text: String)