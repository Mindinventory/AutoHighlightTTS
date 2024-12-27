package com.app.autohighlighttts.models

data class ParagraphModel(
    val text: String,
    val totalWordOfText: Int,
    val startIndex: Int,
    val endIndex: Int,
)