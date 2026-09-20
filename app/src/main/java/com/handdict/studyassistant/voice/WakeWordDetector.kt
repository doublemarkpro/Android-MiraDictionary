package com.handdict.studyassistant.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.KeywordSpotter
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.max

/** Foreground-only, fully local wake-word detector for “你好小智”. */
class WakeWordDetector(
    context: Context,
    private val onWakeWord: (String) -> Unit,
    private val onStateChanged: (State) -> Unit = {},
) {
    enum class State { IDLE, LOADING, LISTENING, ERROR }

    private val applicationContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val running = AtomicBoolean(false)
    private val lock = Any()

    @Volatile private var worker: Thread? = null
    @Volatile private var audioRecord: AudioRecord? = null
    private var keywordSpotter: KeywordSpotter? = null

    fun start() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            publishState(State.ERROR)
            return
        }
        synchronized(lock) {
            if (running.get() || worker?.isAlive == true) return
            running.set(true)
            worker = thread(name = "MiraWakeWord", isDaemon = true) { listenLoop() }
        }
    }

    fun stop() {
        running.set(false)
        runCatching { audioRecord?.stop() }
    }

    fun release() {
        stop()
        val oldWorker = worker
        thread(name = "MiraWakeWordRelease", isDaemon = true) {
            runCatching { oldWorker?.join(1500) }
            synchronized(lock) {
                runCatching { keywordSpotter?.release() }
                keywordSpotter = null
            }
        }
    }

    private fun listenLoop() {
        var stream: OnlineStream? = null
        var detectedKeyword: String? = null
        var failed = false
        try {
            publishState(State.LOADING)
            val spotter = synchronized(lock) {
                keywordSpotter ?: createKeywordSpotter().also { keywordSpotter = it }
            }
            if (!running.get()) return
            stream = spotter.createStream()
            val minimumBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
            check(minimumBytes > 0) { "无法创建麦克风缓冲区" }
            val bufferBytes = max(minimumBytes, SAMPLE_RATE / 5 * 2)
            check(
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
            ) { "麦克风权限已被关闭" }
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE,
                CHANNEL,
                ENCODING,
                bufferBytes,
            )
            check(recorder.state == AudioRecord.STATE_INITIALIZED) { "麦克风初始化失败" }
            audioRecord = recorder
            recorder.startRecording()
            check(recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) { "麦克风无法开始录音" }
            publishState(State.LISTENING)

            val pcm = ShortArray(bufferBytes / 2)
            while (running.get()) {
                val count = recorder.read(pcm, 0, pcm.size)
                if (count <= 0) continue
                val samples = FloatArray(count) { index -> pcm[index] / 32768f }
                stream.acceptWaveform(samples, SAMPLE_RATE)
                while (spotter.isReady(stream)) spotter.decode(stream)
                val keyword = spotter.getResult(stream).keyword
                if (keyword.isNotBlank()) {
                    detectedKeyword = keyword
                    running.set(false)
                }
            }
        } catch (error: Throwable) {
            failed = true
            Log.e(TAG, "Wake-word detector stopped unexpectedly", error)
            if (running.getAndSet(false)) publishState(State.ERROR)
        } finally {
            val recorder = audioRecord
            audioRecord = null
            runCatching { if (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) recorder.stop() }
            runCatching { recorder?.release() }
            runCatching { stream?.release() }
            worker = null
            if (detectedKeyword == null && !failed) publishState(State.IDLE)
        }
        detectedKeyword?.let { keyword ->
            publishState(State.IDLE)
            mainHandler.post { onWakeWord(keyword) }
        }
    }

    private fun createKeywordSpotter(): KeywordSpotter {
        val base = "kws/"
        val config = KeywordSpotterConfig(
            featConfig = FeatureConfig(sampleRate = SAMPLE_RATE, featureDim = 80, dither = 0f),
            modelConfig = OnlineModelConfig(
                transducer = OnlineTransducerModelConfig(
                    encoder = base + "encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx",
                    decoder = base + "decoder-epoch-12-avg-2-chunk-16-left-64.onnx",
                    joiner = base + "joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx",
                ),
                tokens = base + "tokens.txt",
                numThreads = 1,
                debug = false,
                provider = "cpu",
            ),
            maxActivePaths = 4,
            keywordsFile = base + "keywords.txt",
            keywordsScore = 3.0f,
            keywordsThreshold = 0.1f,
            numTrailingBlanks = 1,
        )
        return KeywordSpotter(applicationContext.assets, config)
    }

    private fun publishState(state: State) {
        mainHandler.post { onStateChanged(state) }
    }

    private companion object {
        const val SAMPLE_RATE = 16_000
        const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val TAG = "MiraWakeWord"
    }
}
