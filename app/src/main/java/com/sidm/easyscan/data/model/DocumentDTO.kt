package com.sidm.easyscan.data.model


data class DocumentDTO(
    val id: String,
    val user: String,
    val timestamp: String,
    val image_url: String,
    var processed_text: String,
    val blocks: String,
    val lines: String,
    val words: String,
    val language: String)