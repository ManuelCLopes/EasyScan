package com.sidm.easyscan.data.model

import org.intellij.lang.annotations.Language


data class DocumentDTO(
    val id: String,
    val user: String,
    val timestamp: String,
    val image_url: String,
    val processed_text: String,
    val blocks: String,
    val lines: String,
    val words: String,
    val language: String)