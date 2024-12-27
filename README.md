<a href="https://www.mindinventory.com/?utm_source=gthb&utm_medium=repo&utm_campaign=lassi"><img src="https://github.com/Sammindinventory/MindInventory/blob/main/Banner.png"></a>

# AutoHighlightTTS [![](https://jitpack.io/v/Mindinventory/AutoHighlightTTS.svg)](https://jitpack.io/#Mindinventory/AutoHighlightTTS)
**AutoHighlightTTS** is a powerful and simple solution for integrating Text to Speech functionality into your Android app. It features automatic sentence highlighting with customizable styles, auto-scrolling text during playback, and options to set language, pitch, and speech rate. With inbuilt controls like play, pause, backward/forward by one sentence, AutoHighlightTTS ensures a seamless and interactive TTS experience for your users.
## Screenshots


### Image
![image](/media/img.png)

### Video


https://github.com/user-attachments/assets/0069bbd0-9b1e-4e40-af84-151fd4a29147



### Key features 


* Android 15 support.
* Simple implementation.
* highlighting current sentence.
* auto scroll text while text-to-speech play.
* Set your custom styles for text highlighting.
* Set your own language, pitch & speech Rate.
* Inbuilt Functionality Support play, pause, backward and forward[one sentence], slider position, etc.

# Usage

#### Dependencies

* Step 1. Add the JitPack repository to your project build.gradle:

    ```groovy
	    allprojects {
		    repositories {
			    ...
			    maven { url 'https://jitpack.io' }
		    }
	    }
    ``` 

    **or**
    
    If Android studio version is Arctic Fox then add it in your settings.gradle:

    ```groovy
	   dependencyResolutionManagement {
    		repositories {
        		...
        		maven { url 'https://jitpack.io' }
    		}
	   }
    ``` 
    
* Step 2. Add the dependency in your app module build.gradle:
    
    ```groovy
        dependencies {
            ...
            implementation 'com.github.Mindinventory:AutoHighlightTTS:X.X.X'
        }
    ``` 

### Implementation   

* Step 1. Initialization of the MiTextToSpeech inside your viewmodel :
    
  ```kotlin
  ...

     lateinit var instanceOfTTS: AutoHighlightTTSEngine

    init {
        initTTS(context)
    }

    private fun initTTS(context: Context): AutoHighlightTTSEngine {
        instanceOfTTS = AutoHighlightTTSEngine
            .getInstance()
            .init(context)
            .setLanguage(Locale.ENGLISH)
            .setPitchAndSpeed(1f, 1f)
            .setText(context.getString(R.string.text_to_speech_text))
        return instanceOfTTS
    }

  ... 
  ```

* Step 2. Add listeners and MITextToSpeechText inside your composable :
    
  ```kotlin
  ...
    
         var instanceOfTTS by remember {
            mutableStateOf<AutoHighlightTTSEngine?>(null)
        }

        LaunchedEffect(instanceOfTTS == null) {
            instanceOfTTS = viewModel.instanceOfTTS
        }
  
  ...

        // Listeners to get status

        tts.setOnCompletionListener {
            Log.e("TAG", "TTSScreen: Completed From Callback")
        }.setOnErrorListener {
            //Perform action for error
        }.setOnEachSentenceStartListener {
            Log.e("TAG", "TTSScreen: onEachSentenceStart is called")
        }


  ...

    // The composable function displays the text and helps us to highlight the currently spoken sentence.

        TTSComposable(
            tts = tts,
            textAlign = TextAlign.Center,
            fontFamily = fontFamily,
            fontWeight = FontWeight.ExtraLight,
            miTextHighlightBuilder = MITextHighlightBuilder(
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

  ...

  ```


  
## Additional Functions

| Functions              | Description                                                                        |
|-------------------------|------------------------------------------------------------------------------------|
| playTextToSpeech()      |  used for play TextToSpeech content                                                  |
| pauseTextToSpeech() | pause the Text-to-speech if it is currently speaking                                           |
| forwardText()         | Moves to the next sentence                                         |
| backwardText()        | Moves to the preview sentence                                                         |
| setPitchAndSpeed()       | you can customize the pitch and speech as per your requirements. (float, float) |

## Guidelines

#### Guideline for contributors
Contribution towards our repository is always welcome, we request contributors to create a pull request to the **develop** branch only.  

#### Guideline to report an issue/feature request
It would be great for us if the reporter can share the below things to understand the root cause of the issue.

* Library version
* Code snippet
* Logs if applicable
* Device specification like (Manufacturer, OS version, etc)
* Screenshot/video with steps to reproduce the issue

### Requirements

* minSdkVersion >= 24
* Androidx

# LICENSE!

MiTextToSpeech is [MIT-licensed](/LICENSE).

# Let us know!
We’d be really happy if you send us links to your projects where you use our component. Just send an email to sales@mindinventory.com And do let us know if you have any questions or suggestion regarding our work.

<a href="https://www.mindinventory.com/contact-us.php?utm_source=gthb&utm_medium=repo&utm_campaign=autohighlighttts">
<img src="https://github.com/Sammindinventory/MindInventory/blob/main/hirebutton.png" width="203" height="43"  alt="app development">
</a>
