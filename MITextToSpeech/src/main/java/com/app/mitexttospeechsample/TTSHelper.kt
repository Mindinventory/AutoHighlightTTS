package com.app.mitexttospeechsample

import android.os.Bundle
import android.speech.tts.TextToSpeech


/**
 * [play] Extension Function for Play The TTS.
 */
internal fun TextToSpeech.play(
    text: String,
    queueMode: Int
    = TextToSpeech.QUEUE_FLUSH,
    params: Bundle? = null,
    utteranceId: String = TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
) {
    speak(text, queueMode, params, utteranceId)
}

/**
 * [getStartAndEndOfSubstring] Function is give the start & end form this String
 */
internal fun getStartAndEndOfSubstring(str: String, sub: String): Pair<Int, Int> {
    val start = str.indexOf(sub)
    return when (start != -1) {
        true -> Pair(start, start + sub.length)
        false -> Pair(-1, -1)
    }
}

/**
 * [countWords] this function is return the count/length of string total words.
 */
internal fun countWords(paragraph: String): Int {
    // Split the paragraph into words using regular expression and count the number of words
    return paragraph.trim().split(" ").size
}

/**
 * Calculates the percentage of a slider's position within a range.
 *
 * This function computes the percentage of the slider's position relative to the range
 * defined by the start and end indices.
 *
 * @param start The starting index of the range.
 * @param end The ending index of the range.
 * @param sliderPosition The current position of the slider within the range.
 * @return The percentage of the slider's position within the range.
 */
internal fun calculatePercentage(start: Int, end: Int, sliderPosition: Int): Double {
    // Calculate the total number of positions in the range
    val totalNumbers = end - start + 1
    // Calculate the position of the slider relative to the start index
    val sliderRelativeToStart = sliderPosition - start + 1
    // Calculate the percentage of the slider's position within the range
    return (sliderRelativeToStart.toDouble() / totalNumbers.toDouble()) * 100
}
