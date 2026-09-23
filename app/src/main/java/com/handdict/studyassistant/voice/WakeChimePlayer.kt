package com.handdict.studyassistant.voice

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** A short, offline two-note chime confirming that the wake word was heard. */
class WakeChimePlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null

    @Synchronized
    fun play() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            val samples = CHIME_PCM
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(samples.size)
                .build()
            try {
                track.write(samples, 0, samples.size)
                track.setVolume(0.58f)
                track.play()
                delay(CHIME_DURATION_MILLIS + 80L)
            } finally {
                runCatching { track.stop() }
                track.release()
            }
        }
    }

    fun release() {
        playbackJob?.cancel()
        scope.cancel()
    }

    private companion object {
        const val SAMPLE_RATE = 24_000
        const val CHIME_DURATION_MILLIS = 440

        val CHIME_PCM: ByteArray by lazy {
            val sampleCount = SAMPLE_RATE * CHIME_DURATION_MILLIS / 1_000
            ByteArray(sampleCount * 2).also { bytes ->
                repeat(sampleCount) { index ->
                    val time = index.toDouble() / SAMPLE_RATE
                    val first = bell(time, start = 0.0, duration = 0.27, frequency = 880.0)
                    val second = bell(time, start = 0.105, duration = 0.32, frequency = 1_318.51)
                    val value = ((first + second) * 0.36).coerceIn(-1.0, 1.0)
                    val pcm = (value * Short.MAX_VALUE).toInt()
                    bytes[index * 2] = (pcm and 0xFF).toByte()
                    bytes[index * 2 + 1] = ((pcm ushr 8) and 0xFF).toByte()
                }
            }
        }

        fun bell(time: Double, start: Double, duration: Double, frequency: Double): Double {
            val local = time - start
            if (local !in 0.0..duration) return 0.0
            val attack = (local / 0.012).coerceIn(0.0, 1.0)
            val decay = exp(-5.2 * local / duration)
            val phase = 2.0 * PI * frequency * local
            return attack * decay * (sin(phase) + 0.24 * sin(phase * 2.01) + 0.08 * sin(phase * 3.02))
        }
    }
}
