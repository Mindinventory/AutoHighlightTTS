package com.app.autohighlighttts

import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.autohighlighttts.composable.AutoHighlightTTSBuilder
import com.app.autohighlighttts.ui.theme.Amaranth
import com.app.autohighlighttts.ui.theme.fontFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.app.autohighlightttssample.R


@Composable
fun TTSScreen(viewModel: AutoHighlightTTSViewModel = hiltViewModel()) {

    var instanceOfTTS by remember { mutableStateOf<AutoHighlightTTSEngine?>(null) }
    LaunchedEffect(instanceOfTTS == null) {
        instanceOfTTS = viewModel.instanceOfTTS
    }

    instanceOfTTS?.let { tts ->
        ComposableLifecycle { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    tts.apply {
                        if (autoHighlightTTS.isSpeaking) {
                            pauseTextToSpeech()
                        }
                    }
                }

                else -> {}
            }
        }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        tts.setOnCompletionListener {
            Log.e("TAG", "TTSScreen: Completed From Callback")
            scope.launch {
                Toast.makeText(context, "Completed", Toast.LENGTH_LONG).show()
            }
        }.setOnErrorListener {
            scope.launch {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }.setOnEachSentenceStartListener {
            Log.e("TAG", "TTSScreen: onEachSentenceStart is called")
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp)
        ) {

            Text(
                text = stringResource(id = R.string.text_to_speech),
                color = Amaranth,
                fontWeight = FontWeight.Medium,
                fontSize = 30.sp,
                fontFamily = fontFamily,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, bottom = 20.dp),
                textAlign = TextAlign.Center
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(end = 10.dp)
            ) {
                AutoHighlightTTSComposable(
                    tts = tts,
                    textAlign = TextAlign.Center,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.ExtraLight,
                    autoHighlightTTSBuilder = AutoHighlightTTSBuilder(
                        text = tts.mainText,
                        tts.highlightTextPair.value,
                        style = SpanStyle(
                            fontFamily = fontFamily,
                            color = Amaranth,
                            fontWeight = FontWeight.Bold,
                        )
                    ),
                    style = TextStyle(
                        fontSize = 20.sp, color = Color.Black,
                        lineHeight = 35.sp
                    ),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            BottomStorySection(tts)
        }
    } ?: Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * This Composable Is Used For Track and Control the Progress.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomStorySection(instanceOfTTS: AutoHighlightTTSEngine) {
    var test by remember {
        mutableFloatStateOf(0f)
    }
    val sliderValue = animateFloatAsState(
        targetValue = instanceOfTTS.sliderPosition,
        animationSpec = tween(durationMillis = 100),
        label = ""
    )

    Column(
        modifier = Modifier.padding(vertical = 20.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Slider(
            value = sliderValue.value,
            onValueChange = {
                instanceOfTTS.sliderPosition = it
                test = it
            },
            valueRange = 0f..(instanceOfTTS.totalWords + 1).toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent, // Make the default thumb transparent
                activeTrackColor = Amaranth,
            ),
            onValueChangeFinished = {
                instanceOfTTS.sliderToUpdate((test.roundToInt()))
            },
            track = {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp) // Match slider height
                ) {
                    val trackHeight = 8.dp.toPx() // Track height
                    val activeTrackWidth =
                        (sliderValue.value / instanceOfTTS.totalWords) * size.width

                    // Draw inactive track (gray)
                    drawRoundRect(
                        color = Color.LightGray,
                        size = Size(width = size.width, height = trackHeight),
                        cornerRadius = CornerRadius(trackHeight / 2),
                        topLeft = Offset(0f, (size.height - trackHeight) / 2)
                    )

                    // Draw active track (blue)
                    drawRoundRect(
                        color = Amaranth,
                        size = Size(width = activeTrackWidth, height = trackHeight),
                        cornerRadius = CornerRadius(trackHeight / 2),
                        topLeft = Offset(0f, (size.height - trackHeight) / 2)
                    )
                }
            },
            thumb = {
                // Draw the custom thumb
                Canvas(
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    val thumbRadius = 12.dp.toPx() // Thumb radius, proportional to slider height

                    val thumbCenterX = (sliderValue.value * size.width).coerceIn(
                        thumbRadius,
                        size.width - thumbRadius
                    ) // Constrain thumb within bounds
                    val thumbCenterY = size.height / 2 // Thumb Y position, centered

                    // Draw the outer transparent circle (thumb)
                    drawCircle(
                        color = Amaranth, // Semi-transparent white
                        center = Offset(thumbCenterX, thumbCenterY),
                        radius = thumbRadius
                    )
                }
            },
        )

        StoryReadingController(instanceOfTTS)
    }
}

/**
 * Control the Reading Like Moving to Next , Skip or Forward.
 */
@Composable
fun StoryReadingController(instanceOfTTS: AutoHighlightTTSEngine) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        CommonImageButton(
            modifier = Modifier,
            image = R.drawable.ic_skip_previous,
            enabled = (instanceOfTTS.currentCount.intValue == 0).not(),
            color = if ((instanceOfTTS.currentCount.intValue == 0)) Color.LightGray else Amaranth
        ) {
            instanceOfTTS.backwardText()
        }
        Box(
            Modifier
                .clip(CircleShape)
                .background(Amaranth)
                .size(64.dp)
                .clickable {
                    if (instanceOfTTS.playOrPauseTTS.value) {
                        instanceOfTTS.pauseTextToSpeech()
                    } else {
                        instanceOfTTS.playTextToSpeech()
                    }
                }, contentAlignment = Alignment.Center
        ) {
            Image(
                painterResource(id = if (instanceOfTTS.playOrPauseTTS.value) R.drawable.ic_pause else R.drawable.ic_play_white),
                contentDescription = "",
                modifier = Modifier.size(24.dp)
            )
        }
        CommonImageButton(
            modifier = Modifier, image = R.drawable.ic_skip_next,
            enabled = (instanceOfTTS.currentCount.intValue >= instanceOfTTS.listOfStringOfParagraph.size - 1).not(),
            color = if ((instanceOfTTS.currentCount.intValue >= instanceOfTTS.listOfStringOfParagraph.size - 1)) Color.LightGray else Amaranth
        ) {
            instanceOfTTS.forwardText()
        }
    }
}


@Composable
fun CommonImageButton(
    @DrawableRes image: Int,
    modifier: Modifier = Modifier,
    color: Color = Amaranth, enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    IconButton(
        enabled = enabled,
        onClick = { onClick() },
    ) {
        Icon(
            painter = painterResource(id = image),
            contentDescription = "",
            modifier = modifier.size(32.dp),
            tint = color
        )
    }
}

@Composable
fun ComposableLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}


