package com.app.mitexttospeechsample

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.app.mitexttospeechsample.composable.MITextHighlightBuilder
import com.app.mitexttospeechsample.composable.MITextToSpeechText

@Composable
fun TTSComposable(
    tts: MiTextToSpeechEngine,
    miTextHighlightBuilder: MITextHighlightBuilder,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    var textLineHeight by remember { mutableIntStateOf(0) }
    val lineOfWord = remember { mutableStateListOf<Int>() }
    var currentScrollingCount by remember { mutableIntStateOf(0) }
    val scrollable = rememberScrollState()

    LaunchedEffect(tts.sliderPosition) {
        if (tts.sliderPosition == 0f || (tts.totalWords - 2 < tts.sliderPosition)) {
            scrollable.animateScrollTo(0)
            currentScrollingCount = 0
        }

        while (lineOfWord.size > currentScrollingCount && tts.sliderPosition > lineOfWord[currentScrollingCount]) {
            currentScrollingCount += 1
        }

        while (lineOfWord.size > currentScrollingCount && currentScrollingCount > 0 && tts.sliderPosition <= lineOfWord[currentScrollingCount] - 1) {
            currentScrollingCount -= 1
        }
        scrollable.scrollTo(textLineHeight * currentScrollingCount)
    }

    MITextToSpeechText(
        modifier = modifier.verticalScroll(scrollable),
        color = color,
        fontSize = fontSize,
        textAlign = textAlign,
        fontFamily = fontFamily,
        style = style,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        miTextHighlightBuilder = miTextHighlightBuilder,
        onTextLayout = { textLayoutResult ->
            if (lineOfWord.isEmpty()) {
                val lineCount = textLayoutResult.lineCount
                var lineOffset = 0
                var wordCount = 0
                for (i in 0 until lineCount) {
                    val lineEndIndex = textLayoutResult.getLineEnd(
                        lineIndex = i, visibleEnd = true
                    )
                    val lineContent =
                        tts.mainText.substring(lineOffset, lineEndIndex)
                    wordCount += lineContent.split(" ").count() - 1
                    if (lineOfWord.isEmpty()) {
                        wordCount += 1
                    }
                    lineOfWord.add(wordCount)
                    lineOffset = lineEndIndex
                    textLineHeight = (textLayoutResult.size.height / lineCount)
                }
            }
            onTextLayout.invoke(textLayoutResult)
        },
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
    )
}

