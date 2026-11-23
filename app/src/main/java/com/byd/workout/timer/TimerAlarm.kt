package com.byd.workout.timer

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.delay

object TimerAlarm {
    suspend fun play(context: Context) {
        // Vibrate [0, 200, 100, 200]
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 200, 100, 200)
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }

        // Play 2 beeps (880 Hz is handled by ToneGenerator.TONE_PROP_BEEP), 150ms duration, 250ms gap
        val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        delay(400) // 150ms beep + 250ms silence
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        delay(150)
        toneGenerator.release()
    }
}
