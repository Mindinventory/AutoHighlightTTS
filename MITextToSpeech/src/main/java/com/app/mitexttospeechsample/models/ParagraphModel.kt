package com.app.mitexttospeechsample.models

data class ParagraphModel(
    val text: String,
    val totalWordOfText: Int,
    val startIndex: Int,
    val endIndex: Int,
)