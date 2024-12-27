package com.app.autohighlighttts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.app.autohighlighttts.models.ParagraphModel
import java.util.Locale


class AutoHighlightTTSEngine {

    /**
     * Create Singleton Object
     */
    companion object {
        private var instance: AutoHighlightTTSEngine? = null
        fun getInstance(): AutoHighlightTTSEngine {
            if (instance == null) {
                instance = AutoHighlightTTSEngine()
            }
            return instance!!
        }
    }


    lateinit var autoHighlightTTS: TextToSpeech
    lateinit var mainText: String
    private lateinit var currentSpokenSentenceCopy: String

    var playOrPauseTTS = mutableStateOf(false)
    var totalWords: Int = 0
    var currentCount = mutableIntStateOf(0)
    var listOfStringOfParagraph: List<ParagraphModel> = emptyList()
    var highlightTextPair = mutableStateOf(Pair(0, 0))
    var sliderPosition by mutableFloatStateOf(0f)

    private var stopPosition: Pair<Int, Int> = Pair(0, 0)
    private var defLanguage = Locale.getDefault()
    private var onEachSentenceStartListener: (() -> Unit)? = null
    private var onDoneListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    private var onHighlightListener: ((Pair<Int, Int>) -> Unit)? = null


    /**
     * Initialization of [AutoHighlightTTSEngine]
     */
    fun init(app: Context): AutoHighlightTTSEngine {
        autoHighlightTTS = TextToSpeech(app) {
            if (it == TextToSpeech.SUCCESS) {
                autoHighlightTTS.language = defLanguage
            }
        }
        return this
    }


    /**
     * When we set the language to English in Android TTS,
     * it tells the system to use English pronunciation rules and phonemes to generate speech from the text you provide.
     */
    fun setLanguage(local: Locale): AutoHighlightTTSEngine {
        this.defLanguage = local
        return this
    }


    /**
     * [pauseTextToSpeech] pause the Text-to-speech if it is currently speaking.
     */
    fun pauseTextToSpeech(): AutoHighlightTTSEngine {
        if (autoHighlightTTS.isSpeaking) {
            autoHighlightTTS.stop()
        }
        playOrPauseTTS.value = false
        return this
    }

    /**
     * [setText] is used for the set text to MiTextToSpeech.
     * @param text is string text.
     */
    fun setText(text: String): AutoHighlightTTSEngine {
        mainText = text
        totalWords = countWords(mainText)

        // Split text into paragraphs using regex
        listOfStringOfParagraph = mainText.split("\\.\\s*".toRegex())
            // Filter out empty paragraphs
            .filter { it.isNotEmpty() }
            // Map each paragraph to a ParagraphModel
            .mapIndexed { _, paragraph ->
                // Calculate word count and range for each paragraph
                val startWordIndex = countWords(mainText.substring(0, mainText.indexOf(paragraph)))
                val endWordIndex = startWordIndex + countWords(paragraph) - 1
                ParagraphModel(paragraph, countWords(paragraph), startWordIndex, endWordIndex)
            }

        return this
    }

    /**
     * [playTextToSpeech] is used for play TextToSpeech content.
     */
    fun playTextToSpeech(): AutoHighlightTTSEngine {
        if (mainText.isNotBlank()) {
            // Adjust currentSpokenSentenceCopy if a stop position is set
            currentSpokenSentenceCopy = if (stopPosition.second != 0) {
                currentSpokenSentenceCopy.substring(stopPosition.second)
            } else {
                // Otherwise, set currentSpokenSentenceCopy to the next sentence
                listOfStringOfParagraph[currentCount.intValue].text
            }

            // Play the current sentence
            autoHighlightTTS.play(currentSpokenSentenceCopy)

            // Highlight the text corresponding to the current sentence
            highlightTextPair.value =
                getStartAndEndOfSubstring(
                    mainText,
                    listOfStringOfParagraph[currentCount.intValue].text
                )

            playOrPauseTTS.value = true

            // Set the UtteranceProgressListener for handling TTS events
            autoHighlightTTS.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onEachSentenceStartListener?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    stopPosition = Pair(0, 0)

                    // If there are more sentences to speak
                    if (currentCount.intValue < listOfStringOfParagraph.size - 1) {
                        // Move to the next sentence
                        currentSpokenSentenceCopy =
                            listOfStringOfParagraph[++currentCount.intValue].text
                        // Speak the next sentence
                        autoHighlightTTS.speak(
                            currentSpokenSentenceCopy,
                            TextToSpeech.QUEUE_FLUSH,
                            null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
                        )
                        // Highlight the text corresponding to the next sentence
                        highlightTextPair.value = getStartAndEndOfSubstring(
                            mainText,
                            currentSpokenSentenceCopy
                        )
                        // Update the progress
                        updateProgress(currentCount.intValue)
                    } else {
                        // Reset values when all content is spoken
                        playOrPauseTTS.value = false
                        currentCount.intValue = 0
                        sliderPosition = 0f
                        highlightTextPair.value = Pair(0, 0)

                        // Call onDoneListener when the entire content is spoken
                        onDoneListener?.invoke()
                    }
                }

                // Handle TTS errors
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onErrorListener?.invoke(utteranceId ?: "")
                }

                // Handle range start events (highlighting)
                override fun onRangeStart(
                    utteranceId: String?,
                    start: Int,
                    end: Int,
                    frame: Int
                ) {
                    super.onRangeStart(utteranceId, start, end, frame)
                    // Update the stop position
                    stopPosition = Pair(start, end)
                    // Increment the slider position
                    if (sliderPosition < totalWords) {
                        sliderPosition += 1f
                    }
                    // Invoke the onHighlightListener
                    onHighlightListener?.invoke(Pair(start, end))
                }
            })
        } else {
            onErrorListener?.invoke("Text to speech text is empty")
        }
        return this
    }


    /**
     * Updates the slider progress based on the current index.
     */
    private fun updateProgress(currentIndex: Int = currentCount.intValue): AutoHighlightTTSEngine {
        sliderPosition = listOfStringOfParagraph[currentIndex].startIndex.toFloat()
        return this
    }

    /**
     * [sliderToUpdate] is responsible for calculating and update the value when user changes the slider.
     * @param currentIndex slider current position
     */
    fun sliderToUpdate(currentIndex: Int): AutoHighlightTTSEngine {
        //below condition is work when the over current slider word count is grater then total word count.
        if (currentIndex >= totalWords) {
            currentCount.intValue = 0
            sliderPosition = listOfStringOfParagraph[currentCount.intValue].startIndex.toFloat()
            highlightFunction()
            pauseTextToSpeech()
            return this
        }

        /**
         * paragraph we are finding the [ParagraphModel] base on currentIndex
         */
        val paragraph: ParagraphModel =
            listOfStringOfParagraph.firstOrNull { it.startIndex < currentIndex && it.endIndex >= currentIndex }
                ?: return this // No paragraph found for current index

        currentCount.intValue = listOfStringOfParagraph.indexOf(paragraph)


        /**
         * On Sliding we are calculating percentage and based on that whether that word is in between or not,
         * If it's in between then we are skipping that word.
         */
        val percentage =
            calculatePercentage(paragraph.startIndex, paragraph.endIndex, currentIndex) / 100
        val splitIndex = (paragraph.text.length * percentage).toInt()
        var adjustedSplitIndex = splitIndex
        if (paragraph.text.length > splitIndex) {
            if (paragraph.text[splitIndex] != ' ' || paragraph.text[splitIndex - 1] != ' ') {
                while (true) {
                    if (paragraph.text[++adjustedSplitIndex] != ' ') break
                }
            }
        }
        stopPosition = Pair(0, adjustedSplitIndex)
        currentSpokenSentenceCopy = paragraph.text

        sliderPosition = currentIndex.toFloat()

        highlightFunction()
        pauseTextToSpeech()

        return this
    }

    /**
     * [forwardText] Moves to the next sentence.
     */
    fun forwardText(): AutoHighlightTTSEngine {
        // Check if there is a next sentence
        if (currentCount.intValue < listOfStringOfParagraph.size - 1) {
            // Reset stopPosition
            stopPosition = Pair(0, 0)

            // Move to the next sentence and update currentSpokenSentenceCopy
            currentSpokenSentenceCopy = listOfStringOfParagraph[++currentCount.intValue].text

            // Apply highlighting
            highlightFunction()

            // If TTS is enabled, stop current playback and play the next sentence
            if (playOrPauseTTS.value) {
                pauseTextToSpeech()
                autoHighlightTTS.play(currentSpokenSentenceCopy)
                playOrPauseTTS.value = true
            }

            // Update progress
            updateProgress()
        }
        return this
    }


    /**
     * [backwardText] using this function we go one sentence back
     */
    fun backwardText(): AutoHighlightTTSEngine {
        if (currentCount.intValue <= 0) {
            return this
        }

        stopPosition = Pair(0, 0)
        currentCount.intValue--
        val currentSentence = listOfStringOfParagraph[currentCount.intValue].text

        if (playOrPauseTTS.value) {
            pauseTextToSpeech()
            autoHighlightTTS.play(currentSentence)
            playOrPauseTTS.value = true
        }

        highlightFunction()
        updateProgress()

        return this
    }

    /**
     * [highlightFunction] is responsible for Highlight the text which are speaking.
     */
    private fun highlightFunction(): AutoHighlightTTSEngine {
        highlightTextPair.value =
            getStartAndEndOfSubstring(mainText, listOfStringOfParagraph[currentCount.intValue].text)
        return this
    }

    /**
     * [setOnCompletionListener] is trigger when speech is successfully complete.
     */
    fun setOnCompletionListener(onDoneListener: () -> Unit): AutoHighlightTTSEngine {
        this.onDoneListener = onDoneListener
        return this
    }

    /**
     * [setOnErrorListener] is used for getting error of Text-to-speech.
     */
    fun setOnErrorListener(onErrorListener: (String) -> Unit): AutoHighlightTTSEngine {
        this.onErrorListener = onErrorListener
        return this
    }

    /**
     * [setOnEachSentenceStartListener] is used for getting each sentence start callback
     */
    fun setOnEachSentenceStartListener(onEachSentenceStartListener: () -> Unit): AutoHighlightTTSEngine {
        this.onEachSentenceStartListener = onEachSentenceStartListener
        return this
    }


    /**
     * @param pitch Speech pitch. 1.0 is the normal pitch, lower values lower the tone of
     * the synthesized voice, greater values increase it.
     * @param speed Speech rate. 1.0 is the normal speech rate, lower values slow down
     * the speech (0.5 is half the normal speech rate), greater values accelerate it (2.0 is
     * twice the normal speech rate).
     */
    fun setPitchAndSpeed(pitch: Float = 1f, speed: Float = 1f): AutoHighlightTTSEngine {
        autoHighlightTTS.setPitch(pitch)
        autoHighlightTTS.setSpeechRate(speed)
        return this
    }
}