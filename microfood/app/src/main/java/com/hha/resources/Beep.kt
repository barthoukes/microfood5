import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object BeepManager {
    private var toneGen: ToneGenerator? = null
    private const val VOLUME = 80 // 0-100

    fun init() {
        release() // Clean up any existing instance
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, VOLUME)
        } catch (e: Exception) {
            Log.e("Beep", "ToneGenerator initialization failed", e)
        }
    }

    fun error(context: Context? = null) {
        context?.let { adjustVolume(it) }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                initIfNeeded()
                toneGen?.startTone(ToneGenerator.TONE_SUP_ERROR, 150)
            } catch (e: Exception) {
                Log.e("Beep", "Error beep failed", e)
            }
        }
    }

    fun locateDevice() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                initIfNeeded()
                (ToneGenerator.TONE_DTMF_0..ToneGenerator.TONE_DTMF_9).forEach { toneType ->
                    toneGen?.startTone(toneType, 200)
                    Thread.sleep(250) // Slightly longer than tone duration
                }
            } catch (e: Exception) {
                Log.e("Beep", "Locate device failed", e)
            }
        }
    }

    fun release() {
        toneGen?.release()
        toneGen = null
    }

    private fun initIfNeeded() {
        if (toneGen == null) {
            init()
        }
    }

    private fun adjustVolume(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = (maxVolume * 0.7).toInt() // 70% of max volume

        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < targetVolume) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                targetVolume,
                AudioManager.FLAG_SHOW_UI
            )
        }
    }
}