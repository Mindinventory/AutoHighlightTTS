package com.app.mitexttospeechsample

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TTSViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {

     lateinit var instanceOfTTS: MiTextToSpeechEngine

    init {
        initTTS(context)
    }

    private fun initTTS(context: Context): MiTextToSpeechEngine {
        instanceOfTTS = MiTextToSpeechEngine
            .getInstance()
            .init(context)
            .setLanguage(Locale.ENGLISH)
            .setPitchAndSpeed(1f, 1f)
            .setText(context.getString(R.string.text_to_speech_text))
        return instanceOfTTS
    }
}