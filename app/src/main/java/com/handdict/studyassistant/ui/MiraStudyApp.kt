package com.handdict.studyassistant.ui

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.handdict.studyassistant.R
import com.handdict.studyassistant.data.DictionaryEntry
import com.handdict.studyassistant.data.DictionaryRepository
import com.handdict.studyassistant.data.DEFAULT_DICTIONARY_DAILY_LIMIT
import com.handdict.studyassistant.data.DeviceWeatherRepository
import com.handdict.studyassistant.data.DailyWeather
import com.handdict.studyassistant.data.FocusRecord
import com.handdict.studyassistant.data.FOCUS_SUBJECT_MAX_MINUTES
import com.handdict.studyassistant.data.Lesson
import com.handdict.studyassistant.data.LocatedWeather
import com.handdict.studyassistant.data.LocalStore
import com.handdict.studyassistant.data.TimerSnapshot
import com.handdict.studyassistant.data.WeatherInfo
import com.handdict.studyassistant.data.WeatherCache
import com.handdict.studyassistant.data.WeatherCity
import com.handdict.studyassistant.data.WeatherReport
import com.handdict.studyassistant.data.defaultHomeworkSubjects
import com.handdict.studyassistant.data.maximumExpectedMinutes
import com.handdict.studyassistant.data.WeatherRepository
import com.handdict.studyassistant.timer.TimerAlarmScheduler
import com.handdict.studyassistant.focus.TimePin
import com.handdict.studyassistant.ui.theme.MiraBlue
import com.handdict.studyassistant.ui.theme.MiraGreen
import com.handdict.studyassistant.ui.theme.MiraNavy
import com.handdict.studyassistant.ui.theme.MiraOrange
import com.handdict.studyassistant.ui.theme.MiraSky
import com.handdict.studyassistant.voice.WakeWordDetector
import com.handdict.studyassistant.voice.WakeChimePlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class AppScreen(val label: String, val symbol: String) {
    HOME("首页", "⌂"), TIMER("作业计时", "◷"), DICTIONARY("查字典", "▤"),
    BORROWING("借位小能手", "−"), SCHEDULE("课程表", "▦"), WEATHER("天气", "☁"), SETTINGS("设置", "⚙"),
}

private enum class NavigationStyle(val key: String, val label: String, val description: String) {
    FRESH("fresh", "清新卡片", "柔和蓝色卡片，清楚又耐看"),
    COLORFUL("colorful", "彩色文具", "每个功能都有自己的活力颜色"),
    JOURNAL("journal", "自然手账", "奶油纸张与绿色自然气息"),
    MINIMAL("minimal", "极简蓝白", "更安静、专注的学习界面"),
    STATIONERY("stationery", "活力文具", "彩色线条图标，蓝色选中卡片"),
}

private fun navigationStyleFor(key: String) = NavigationStyle.entries.firstOrNull { it.key == key } ?: NavigationStyle.FRESH

private fun screenAccent(screen: AppScreen): Color = when (screen) {
    AppScreen.HOME -> Color(0xFF3D83EA)
    AppScreen.TIMER -> Color(0xFFFFA928)
    AppScreen.DICTIONARY -> Color(0xFF35B77C)
    AppScreen.BORROWING -> Color(0xFF2677ED)
    AppScreen.SCHEDULE -> Color(0xFFFF7468)
    AppScreen.WEATHER -> Color(0xFF4AA8E8)
    AppScreen.SETTINGS -> Color(0xFF7B72E9)
}

private fun screenBackgroundResource(screen: AppScreen): Int = when (screen) {
    AppScreen.HOME -> R.drawable.mira_bg_home_v2
    AppScreen.TIMER -> R.drawable.mira_bg_timer_v2
    AppScreen.DICTIONARY -> R.drawable.mira_bg_dictionary
    AppScreen.BORROWING -> R.drawable.mira_bg_home_v2
    AppScreen.SCHEDULE -> R.drawable.mira_bg_schedule
    AppScreen.WEATHER -> R.drawable.mira_bg_weather
    AppScreen.SETTINGS -> R.drawable.mira_bg_settings
}

private fun navigationIconResource(style: NavigationStyle, screen: AppScreen): Int = when (style) {
    NavigationStyle.FRESH -> when (screen) {
        AppScreen.HOME -> R.drawable.mira_nav_fresh_home
        AppScreen.TIMER -> R.drawable.mira_nav_fresh_timer
        AppScreen.DICTIONARY -> R.drawable.mira_nav_fresh_dictionary
        AppScreen.BORROWING -> R.drawable.mira_schedule_math
        AppScreen.SCHEDULE -> R.drawable.mira_nav_fresh_schedule
        AppScreen.WEATHER -> R.drawable.mira_nav_fresh_weather
        AppScreen.SETTINGS -> R.drawable.mira_nav_fresh_settings
    }
    NavigationStyle.COLORFUL -> when (screen) {
        AppScreen.HOME -> R.drawable.mira_nav_colorful_home
        AppScreen.TIMER -> R.drawable.mira_nav_colorful_timer
        AppScreen.DICTIONARY -> R.drawable.mira_nav_colorful_dictionary
        AppScreen.BORROWING -> R.drawable.mira_schedule_math
        AppScreen.SCHEDULE -> R.drawable.mira_nav_colorful_schedule
        AppScreen.WEATHER -> R.drawable.mira_nav_colorful_weather
        AppScreen.SETTINGS -> R.drawable.mira_nav_colorful_settings
    }
    NavigationStyle.JOURNAL -> when (screen) {
        AppScreen.HOME -> R.drawable.mira_nav_journal_home
        AppScreen.TIMER -> R.drawable.mira_nav_journal_timer
        AppScreen.DICTIONARY -> R.drawable.mira_nav_journal_dictionary
        AppScreen.BORROWING -> R.drawable.mira_schedule_math
        AppScreen.SCHEDULE -> R.drawable.mira_nav_journal_schedule
        AppScreen.WEATHER -> R.drawable.mira_nav_journal_weather
        AppScreen.SETTINGS -> R.drawable.mira_nav_journal_settings
    }
    NavigationStyle.MINIMAL -> when (screen) {
        AppScreen.HOME -> R.drawable.mira_nav_minimal_home
        AppScreen.TIMER -> R.drawable.mira_nav_minimal_timer
        AppScreen.DICTIONARY -> R.drawable.mira_nav_minimal_dictionary
        AppScreen.BORROWING -> R.drawable.mira_schedule_math
        AppScreen.SCHEDULE -> R.drawable.mira_nav_minimal_schedule
        AppScreen.WEATHER -> R.drawable.mira_nav_minimal_weather
        AppScreen.SETTINGS -> R.drawable.mira_nav_minimal_settings
    }
    NavigationStyle.STATIONERY -> when (screen) {
        AppScreen.HOME -> R.drawable.mira_nav_stationery_home
        AppScreen.TIMER -> R.drawable.mira_nav_stationery_timer
        AppScreen.DICTIONARY -> R.drawable.mira_nav_stationery_dictionary
        AppScreen.BORROWING -> R.drawable.mira_schedule_math
        AppScreen.SCHEDULE -> R.drawable.mira_nav_stationery_schedule
        AppScreen.WEATHER -> R.drawable.mira_nav_stationery_weather
        AppScreen.SETTINGS -> R.drawable.mira_nav_stationery_settings
    }
}

private fun timerCurrentSectionIcon(style: NavigationStyle): Int = when (style) {
    NavigationStyle.FRESH -> R.drawable.mira_timer_section_current_fresh
    NavigationStyle.COLORFUL -> R.drawable.mira_timer_section_current_colorful
    NavigationStyle.JOURNAL -> R.drawable.mira_timer_section_current_journal
    NavigationStyle.MINIMAL -> R.drawable.mira_timer_section_current_minimal
    NavigationStyle.STATIONERY -> R.drawable.mira_timer_section_current_stationery
}

private fun timerWeeklySectionIcon(style: NavigationStyle): Int = when (style) {
    NavigationStyle.FRESH -> R.drawable.mira_timer_section_weekly_fresh
    NavigationStyle.COLORFUL -> R.drawable.mira_timer_section_weekly_colorful
    NavigationStyle.JOURNAL -> R.drawable.mira_timer_section_weekly_journal
    NavigationStyle.MINIMAL -> R.drawable.mira_timer_section_weekly_minimal
    NavigationStyle.STATIONERY -> R.drawable.mira_timer_section_weekly_stationery
}

private fun subjectAccent(subject: String): Color = when (subject) {
    "语文" -> Color(0xFFFF766B)
    "数学" -> Color(0xFF438FF4)
    "英语" -> Color(0xFF45B978)
    "科学" -> Color(0xFF9A70E8)
    "体育" -> Color(0xFFFFA528)
    "美术" -> Color(0xFFE65DAA)
    else -> listOf(Color(0xFF25A9C5), Color(0xFFF08A55), Color(0xFF6977D7))[subject.hashCode().mod(3)]
}

private val screenItems = AppScreen.entries
private val focusScreenItems = listOf(AppScreen.HOME, AppScreen.TIMER, AppScreen.DICTIONARY, AppScreen.BORROWING)
private val cardShape = RoundedCornerShape(24.dp)

private data class ParentGateRequest(val title: String, val action: () -> Unit)

private fun enterSystemFocusMode(activity: Activity) {
    val insets = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
    insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    insets.hide(WindowInsetsCompat.Type.systemBars())
    val activityManager = activity.getSystemService(ActivityManager::class.java)
    if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
        activity.startLockTask()
    }
}

private fun leaveSystemFocusMode(activity: Activity) {
    val activityManager = activity.getSystemService(ActivityManager::class.java)
    if (activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
        runCatching { activity.stopLockTask() }
    }
    WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        .show(WindowInsetsCompat.Type.systemBars())
}

private fun openSystemHomeSettingsAfterFocusExit(activity: Activity) {
    // stopLockTask() needs a brief moment to release the system UI before another app can open.
    activity.window.decorView.postDelayed({
        Toast.makeText(activity, "请选择“小米系统桌面”并设为默认", Toast.LENGTH_LONG).show()
        runCatching {
            activity.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }.recoverCatching {
            activity.startActivity(Intent(Settings.ACTION_SETTINGS))
        }.onFailure {
            Toast.makeText(activity, "系统设置未能打开，请从顶部控制中心进入设置", Toast.LENGTH_LONG).show()
        }
    }, 350L)
}
private val miraDisplayFont = FontFamily(Font(R.font.zcool_kuaile_regular))
private val miraHandFont = FontFamily(Font(R.font.ma_shan_zheng_regular))
private val miraKaiFont = FontFamily(Font(R.font.lxgw_wenkai_regular))

internal fun formatTimer(totalSeconds: Int): String = String.format(
    Locale.ROOT,
    "%02d:%02d",
    totalSeconds.coerceIn(0, 99 * 60 + 59) / 60,
    totalSeconds.coerceIn(0, 99 * 60 + 59) % 60,
)

private fun screenIcon(screen: AppScreen): ImageVector = when (screen) {
    AppScreen.HOME -> Icons.Rounded.Home
    AppScreen.TIMER -> Icons.Rounded.AccessTime
    AppScreen.DICTIONARY -> Icons.AutoMirrored.Rounded.MenuBook
    AppScreen.BORROWING -> Icons.Rounded.Edit
    AppScreen.SCHEDULE -> Icons.Rounded.CalendarMonth
    AppScreen.WEATHER -> Icons.Rounded.WbSunny
    AppScreen.SETTINGS -> Icons.Rounded.Settings
}

@Composable
fun MiraStudyApp() {
    val context = LocalContext.current
    val store = remember { LocalStore(context.applicationContext) }
    var selected by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    // 不使用 rememberSaveable，避免系统恢复旧界面状态后绕过“启动即专注”。
    var focusModeActive by remember { mutableStateOf(true) }
    var parentGateRequest by remember { mutableStateOf<ParentGateRequest?>(null) }
    var pendingDictionaryQuery by rememberSaveable { mutableStateOf("") }
    var navigationStyleKey by rememberSaveable { mutableStateOf(store.loadNavigationStyle()) }
    var voiceWakeEnabled by rememberSaveable { mutableStateOf(store.loadVoiceWakeEnabled()) }
    var wakeState by remember { mutableStateOf(WakeWordDetector.State.IDLE) }
    var appInForeground by remember { mutableStateOf(false) }
    var manualVoiceActive by remember { mutableStateOf(false) }
    var showWakeVoiceOverlay by remember { mutableStateOf(false) }
    var wakeListening by remember { mutableStateOf(false) }
    var wakeTranscript by remember { mutableStateOf("") }
    var wakeError by remember { mutableStateOf<String?>(null) }
    var wakeLevel by remember { mutableFloatStateOf(0f) }
    var wakeTriggerEpoch by remember { mutableIntStateOf(0) }
    val navigationStyle = navigationStyleFor(navigationStyleKey)
    LaunchedEffect(Unit) { store.saveFocusModeActive(true) }
    val wakeChimePlayer = remember { WakeChimePlayer() }
    val wakeDetector = remember {
        WakeWordDetector(
            context = context.applicationContext,
            onWakeWord = {
                wakeChimePlayer.play()
                showWakeVoiceOverlay = true
                wakeTranscript = "你好小智"
                wakeError = null
                wakeTriggerEpoch++
            },
            onStateChanged = { wakeState = it },
        )
    }
    val wakeSpeechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    DisposableEffect(wakeSpeechRecognizer) {
        wakeSpeechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { wakeListening = true; wakeError = null }
            override fun onBeginningOfSpeech() { wakeListening = true }
            override fun onRmsChanged(rmsdB: Float) { wakeLevel = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f) }
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { wakeListening = false }
            override fun onError(error: Int) {
                wakeListening = false
                wakeError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "没有听清，请再说一次完整问题"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有听到问题，请再试一次"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "语音服务暂时不可用"
                    else -> "识别没有完成，请再试一次"
                }
            }
            override fun onResults(results: Bundle?) {
                wakeListening = false
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (spoken.isBlank()) {
                    wakeError = "没有听清，请再说一次完整问题"
                    return
                }
                wakeTranscript = spoken
                pendingDictionaryQuery = extractDictionaryQuery(spoken)
                selected = AppScreen.DICTIONARY
                showWakeVoiceOverlay = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                    if (it.isNotBlank()) wakeTranscript = it
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose { wakeSpeechRecognizer?.destroy() }
    }
    fun startWakeCommandRecognition() {
        wakeTranscript = ""
        wakeError = null
        showWakeVoiceOverlay = true
        if (wakeSpeechRecognizer == null) {
            wakeListening = false
            wakeError = "设备暂不支持语音识别"
            return
        }
        wakeListening = true
        runCatching {
            wakeSpeechRecognizer.startListening(dictionarySpeechIntent().putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true))
        }.onFailure {
            wakeListening = false
            wakeError = "语音识别暂时无法启动"
        }
    }
    val voiceWakePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        voiceWakeEnabled = granted
        store.saveVoiceWakeEnabled(granted)
        if (!granted) Toast.makeText(context, "需要麦克风权限才能使用“你好小智”", Toast.LENGTH_SHORT).show()
    }
    fun setVoiceWakeEnabled(enabled: Boolean) {
        if (!enabled) {
            voiceWakeEnabled = false
            store.saveVoiceWakeEnabled(false)
            wakeDetector.stop()
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            voiceWakeEnabled = true
            store.saveVoiceWakeEnabled(true)
        } else {
            voiceWakePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    DisposableEffect(context, wakeDetector) {
        val lifecycleOwner = context as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> appInForeground = true
                Lifecycle.Event.ON_STOP -> appInForeground = false
                else -> Unit
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
            wakeDetector.release()
            wakeChimePlayer.release()
        }
    }
    LaunchedEffect(voiceWakeEnabled, appInForeground, manualVoiceActive, showWakeVoiceOverlay) {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (voiceWakeEnabled && appInForeground && !manualVoiceActive && !showWakeVoiceOverlay && hasPermission) {
            delay(650)
            wakeDetector.start()
        } else {
            wakeDetector.stop()
        }
    }
    LaunchedEffect(wakeTriggerEpoch) {
        if (wakeTriggerEpoch > 0) {
            wakeDetector.stop()
            // 等待短提示音播放完成，避免提示音被随后的语音识别误收进去。
            delay(480)
            startWakeCommandRecognition()
        }
    }
    val wakeStatus = when {
        !voiceWakeEnabled -> "已关闭"
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED -> "需要麦克风权限"
        showWakeVoiceOverlay -> "正在识别你的问题"
        wakeState == WakeWordDetector.State.LOADING -> "正在加载本地唤醒模型"
        wakeState == WakeWordDetector.State.LISTENING -> "正在等待“你好小智”"
        wakeState == WakeWordDetector.State.ERROR -> "启动失败，请关闭后重试"
        else -> "准备中"
    }
    val availableScreens = if (focusModeActive) focusScreenItems else screenItems

    fun activateFocusMode() {
        focusModeActive = true
        store.saveFocusModeActive(true)
    }

    fun requestParentApproval(title: String, action: () -> Unit) {
        parentGateRequest = ParentGateRequest(title, action)
    }

    fun deactivateFocusMode() {
        requestParentApproval("是否退出专注模式？") {
            focusModeActive = false
            store.saveFocusModeActive(false)
            (context as? Activity)?.let { activity ->
                leaveSystemFocusMode(activity)
                openSystemHomeSettingsAfterFocusExit(activity)
            }
            selected = AppScreen.HOME
        }
    }

    LaunchedEffect(focusModeActive) {
        if (focusModeActive && selected !in focusScreenItems) selected = AppScreen.HOME
    }
    LaunchedEffect(focusModeActive, appInForeground) {
        val activity = context as? Activity ?: return@LaunchedEffect
        if (focusModeActive && appInForeground) {
            // 自动启动时需等待 Activity 完成恢复，否则部分系统会忽略 startLockTask。
            delay(500)
            runCatching { enterSystemFocusMode(activity) }.onFailure {
                Toast.makeText(context, "无法启动屏幕固定，请在系统设置中开启该功能", Toast.LENGTH_LONG).show()
            }
        } else if (!focusModeActive) {
            leaveSystemFocusMode(activity)
        }
    }
    BackHandler(enabled = focusModeActive) {
        Toast.makeText(context, "专注模式中，请由家长退出", Toast.LENGTH_SHORT).show()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(screenBackgroundResource(selected)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            if (maxWidth >= 720.dp) {
                Row(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                    TabletNavigation(
                        selected = selected,
                        style = navigationStyle,
                        items = availableScreens,
                        onSelected = { selected = it },
                    )
                    Column(Modifier.fillMaxSize()) {
                        when (selected) {
                            AppScreen.HOME -> HomeHeroHeader(
                                focusModeActive = focusModeActive,
                                onActivateFocusMode = ::activateFocusMode,
                                onDeactivateFocusMode = ::deactivateFocusMode,
                            )
                            AppScreen.TIMER -> TimerHeroHeader(navigationStyle)
                            AppScreen.DICTIONARY -> DictionaryHeroHeader(navigationStyle)
                            AppScreen.SCHEDULE -> ScheduleHeroHeader(navigationStyle)
                            AppScreen.WEATHER -> WeatherHeroHeader(navigationStyle)
                            else -> AppHeader(selected, navigationStyle)
                        }
                        ScreenContent(
                            screen = selected,
                            onNavigate = { selected = it },
                            onOpenDictionary = { query ->
                                pendingDictionaryQuery = query
                                selected = AppScreen.DICTIONARY
                            },
                            dictionaryQuery = pendingDictionaryQuery,
                            navigationStyle = navigationStyle,
                            onNavigationStyleChanged = { style ->
                                navigationStyleKey = style.key
                                store.saveNavigationStyle(style.key)
                            },
                            voiceWakeEnabled = voiceWakeEnabled,
                            voiceWakeStatus = wakeStatus,
                            onVoiceWakeEnabledChanged = ::setVoiceWakeEnabled,
                            onManualVoiceSessionChanged = { manualVoiceActive = it },
                            focusModeActive = focusModeActive,
                            onRequestParentApproval = ::requestParentApproval,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                    when (selected) {
                        AppScreen.HOME -> HomeHeroHeader(
                            focusModeActive = focusModeActive,
                            onActivateFocusMode = ::activateFocusMode,
                            onDeactivateFocusMode = ::deactivateFocusMode,
                        )
                        AppScreen.TIMER -> TimerHeroHeader(navigationStyle)
                        AppScreen.DICTIONARY -> DictionaryHeroHeader(navigationStyle)
                        AppScreen.SCHEDULE -> ScheduleHeroHeader(navigationStyle)
                        AppScreen.WEATHER -> WeatherHeroHeader(navigationStyle)
                        else -> AppHeader(selected, navigationStyle)
                    }
                    ScreenContent(
                        screen = selected,
                        onNavigate = { selected = it },
                        onOpenDictionary = { query ->
                            pendingDictionaryQuery = query
                            selected = AppScreen.DICTIONARY
                        },
                        dictionaryQuery = pendingDictionaryQuery,
                        navigationStyle = navigationStyle,
                        onNavigationStyleChanged = { style ->
                            navigationStyleKey = style.key
                            store.saveNavigationStyle(style.key)
                        },
                        voiceWakeEnabled = voiceWakeEnabled,
                        voiceWakeStatus = wakeStatus,
                        onVoiceWakeEnabledChanged = ::setVoiceWakeEnabled,
                        onManualVoiceSessionChanged = { manualVoiceActive = it },
                        focusModeActive = focusModeActive,
                        onRequestParentApproval = ::requestParentApproval,
                        modifier = Modifier.weight(1f),
                    )
                    CompactNavigation(selected = selected, items = availableScreens, onSelected = { selected = it })
                }
            }
            if (showWakeVoiceOverlay) {
                Box(Modifier.fillMaxSize().background(Color(0xFFF7FBFF).copy(alpha = 0.72f)))
                HomeVoiceOverlay(
                    listening = wakeListening,
                    transcript = wakeTranscript,
                    error = wakeError,
                    level = wakeLevel,
                    onRetry = { startWakeCommandRecognition() },
                    onCancel = {
                        wakeSpeechRecognizer?.cancel()
                        wakeListening = false
                        showWakeVoiceOverlay = false
                    },
                    onComplete = {
                        if (wakeTranscript.isNotBlank()) {
                            pendingDictionaryQuery = extractDictionaryQuery(wakeTranscript)
                            selected = AppScreen.DICTIONARY
                        }
                        showWakeVoiceOverlay = false
                    },
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            parentGateRequest?.let { request ->
                TimePinDialog(
                    title = request.title,
                    message = "请输入家长 PIN 继续。",
                    onDismiss = { parentGateRequest = null },
                    onSubmit = { pin ->
                        if (TimePin.verify(pin)) {
                            parentGateRequest = null
                            request.action()
                            true
                        } else {
                            false
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TimePinDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Boolean,
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(220)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(4); error = null },
                    modifier = Modifier.focusRequester(focusRequester),
                    label = { Text("家长 PIN") },
                    singleLine = true,
                    isError = error != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    error = when {
                        pin.length != 4 -> "请输入 4 位家长 PIN"
                        !onSubmit(pin) -> "PIN 不正确"
                        else -> null
                    }
                },
            ) { Text("确认") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun LiveDateTimeText(
    modifier: Modifier = Modifier,
    includeYear: Boolean = false,
    fontSize: Int = 13,
    color: Color = Color(0xFF516B8D),
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            val current = System.currentTimeMillis()
            nowMillis = current
            delay((1_000L - current % 1_000L).coerceAtLeast(50L))
        }
    }
    val value = remember(nowMillis, includeYear) {
        val pattern = if (includeYear) "yyyy年M月d日 EEEE  HH:mm:ss" else "M月d日 EEEE  HH:mm:ss"
        Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern(pattern, Locale.CHINA))
    }
    Text(value, modifier = modifier, color = color, fontSize = fontSize.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun PageTitleBlock(
    screen: AppScreen,
    navigationStyle: NavigationStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigationStyleIcon(navigationStyle, screen, Modifier.size(50.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.width(if (screen == AppScreen.BORROWING) 260.dp else 190.dp)) {
            Text(
                screen.label,
                color = Color(0xFF082B63),
                fontSize = if (screen == AppScreen.BORROWING) 34.sp else 42.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
            Box(Modifier.width(86.dp).height(6.dp).background(Color(0xFFFFC832), CircleShape))
            LiveDateTimeText(fontSize = 12, color = Color(0xFF516B8D))
        }
    }
}

@Composable
private fun AppHeader(selected: AppScreen, navigationStyle: NavigationStyle) {
    Box(Modifier.fillMaxWidth().height(112.dp).clipToBounds()) {
        PageTitleBlock(
            screen = selected,
            navigationStyle = navigationStyle,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 30.dp),
        )
        Surface(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 30.dp),
            color = Color(0xFFFFF3C9),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                "⭐ 为今天的进步加油 · 认真一点点，成长一大步",
                Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                color = Color(0xFF80601B),
            )
        }
    }
}

@Composable
private fun HomeHeroHeader(
    focusModeActive: Boolean,
    onActivateFocusMode: () -> Unit,
    onDeactivateFocusMode: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp),
    ) {
        Column(
            Modifier.align(Alignment.CenterStart)
                .combinedClickable(
                    onClick = { if (!focusModeActive) onActivateFocusMode() },
                    onLongClick = { if (focusModeActive) onDeactivateFocusMode() },
                )
                .padding(start = 30.dp, end = 24.dp, top = 10.dp, bottom = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFC437), modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "Mira 学习助手",
                    color = Color(0xFF123B78),
                    fontSize = 43.sp,
                    fontFamily = miraDisplayFont,
                    fontWeight = FontWeight.Black,
                )
            }
            Box(Modifier.padding(start = 56.dp, top = 2.dp).width(360.dp).height(5.dp).background(Color(0xFFFFCA3A), CircleShape))
            Row(Modifier.padding(start = 56.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (focusModeActive) "专注模式中 · 长按标题退出" else "好好学习，遇见更棒的自己！ · 点击进入专注",
                    color = Color(0xFF496991),
                    fontSize = 18.sp,
                    fontFamily = miraHandFont,
                )
                Spacer(Modifier.width(13.dp))
                Icon(Icons.Rounded.SentimentSatisfiedAlt, null, tint = Color(0xFF6C7E9D), modifier = Modifier.size(29.dp))
            }
        }
        Column(Modifier.align(Alignment.Center).padding(start = 260.dp, top = 26.dp)) {
            LiveDateTimeText(includeYear = true, fontSize = 18, color = Color(0xFF173C72))
            Text("今天也是闪闪发光的一天！", color = Color(0xFF5A7194), fontFamily = miraHandFont, fontSize = 17.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun TimerHeroHeader(navigationStyle: NavigationStyle) {
    Box(
        Modifier.fillMaxWidth().height(112.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFFC7ECFF).copy(alpha = 0.52f), Color(0xFFB9E5FB).copy(alpha = 0.38f), Color(0xFFDDF5FF).copy(alpha = 0.48f))))
            .clipToBounds(),
    ) {
        Box(Modifier.align(Alignment.CenterEnd).width(390.dp).height(112.dp).clipToBounds()) {
            Image(
                painterResource(R.drawable.mira_timer_header_decor_v3),
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center).width(360.dp).height(180.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                "⭐ 加油！\n你可以的！",
                modifier = Modifier.align(Alignment.Center).offset(x = 32.dp, y = 5.dp).rotate(-4f),
                color = Color(0xFF29476E),
                fontFamily = miraHandFont,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
        PageTitleBlock(
            screen = AppScreen.TIMER,
            navigationStyle = navigationStyle,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 30.dp),
        )
        Column(Modifier.align(Alignment.CenterStart).padding(start = 310.dp)) {
            Text("专注当下，", color = Color(0xFF496987), fontSize = 18.sp, fontFamily = miraHandFont)
            Text("每天都有进步！  ☺", color = Color(0xFF496987), fontSize = 18.sp, fontFamily = miraHandFont)
        }
    }
}

@Composable
private fun DictionaryHeroHeader(navigationStyle: NavigationStyle) {
    Box(
        Modifier.fillMaxWidth().height(112.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFFD6F1FF).copy(alpha = 0.50f), Color(0xFFF8FCFF).copy(alpha = 0.28f), Color(0xFFDFF5FF).copy(alpha = 0.45f))))
            .clipToBounds(),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cloud = Color.White.copy(alpha = 0.72f)
            drawCircle(cloud, size.height * 0.34f, Offset(size.width * 0.28f, size.height * 0.64f))
            drawCircle(cloud, size.height * 0.24f, Offset(size.width * 0.34f, size.height * 0.54f))
            drawCircle(cloud.copy(alpha = 0.55f), size.height * 0.21f, Offset(size.width * 0.72f, size.height * 0.34f))
        }
        PageTitleBlock(
            screen = AppScreen.DICTIONARY,
            navigationStyle = navigationStyle,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 30.dp),
        )
        Column(Modifier.align(Alignment.CenterEnd).padding(end = 34.dp), horizontalAlignment = Alignment.End) {
            Text("汉字有力量", color = Color(0xFF496991), fontFamily = miraHandFont, fontSize = 18.sp)
            Text("让世界更美好！  ✈", color = Color(0xFF496991), fontFamily = miraHandFont, fontSize = 18.sp)
        }
    }
}

@Composable
private fun ScheduleHeroHeader(navigationStyle: NavigationStyle) {
    Box(
        Modifier.fillMaxWidth().height(112.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFFD8F1FF).copy(alpha = 0.50f), Color(0xFFF7FCFF).copy(alpha = 0.28f), Color(0xFFDDF3FF).copy(alpha = 0.45f))))
            .clipToBounds(),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cloud = Color.White.copy(alpha = 0.68f)
            drawCircle(cloud, size.height * 0.34f, Offset(size.width * 0.28f, size.height * 0.67f))
            drawCircle(cloud, size.height * 0.25f, Offset(size.width * 0.34f, size.height * 0.54f))
            val route = Path().apply {
                moveTo(size.width * 0.60f, size.height * 0.52f)
                quadraticTo(size.width * 0.68f, size.height * 0.18f, size.width * 0.76f, size.height * 0.52f)
                quadraticTo(size.width * 0.83f, size.height * 0.82f, size.width * 0.91f, size.height * 0.37f)
            }
            drawPath(route, Color(0xFF90B5DA), style = Stroke(3f, cap = StrokeCap.Round))
        }
        PageTitleBlock(
            screen = AppScreen.SCHEDULE,
            navigationStyle = navigationStyle,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 30.dp),
        )
        Text(
            "每一节课\n都是成长的机会！",
            modifier = Modifier.align(Alignment.Center).offset(x = 90.dp),
            color = Color(0xFF6485AA),
            fontFamily = miraHandFont,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
        Text("✈  ★", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 36.dp), color = Color(0xFF4776A7), fontSize = 34.sp)
    }
}

@Composable
private fun WeatherHeroHeader(navigationStyle: NavigationStyle) {
    Box(
        Modifier.fillMaxWidth().height(112.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFFDDF4FF).copy(alpha = 0.48f), Color(0xFFF8FCFF).copy(alpha = 0.28f), Color(0xFFE7F8E6).copy(alpha = 0.46f))))
            .clipToBounds(),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val hill = Path().apply {
                moveTo(size.width * 0.78f, size.height)
                quadraticTo(size.width * 0.86f, size.height * 0.35f, size.width * 0.92f, size.height)
                quadraticTo(size.width * 0.96f, size.height * 0.52f, size.width, size.height * 0.82f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(hill, Color(0xFFB7E6A5))
            drawCircle(Color(0xFFFFCC35), size.height * 0.08f, Offset(size.width * 0.13f, size.height * 0.34f))
            repeat(3) { index ->
                drawLine(
                    Color(0xFFFFC327),
                    Offset(size.width * (0.16f + index * 0.012f), size.height * (0.18f + index * 0.12f)),
                    Offset(size.width * (0.17f + index * 0.012f), size.height * (0.11f + index * 0.12f)),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round,
                )
            }
        }
        PageTitleBlock(
            screen = AppScreen.WEATHER,
            navigationStyle = navigationStyle,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 30.dp),
        )
        Column(Modifier.align(Alignment.CenterEnd).padding(end = 54.dp), horizontalAlignment = Alignment.End) {
            Text("每天都是", color = Color(0xFF486A92), fontFamily = miraHandFont, fontSize = 17.sp)
            Text("更棒的一天！  ★", color = Color(0xFF486A92), fontFamily = miraHandFont, fontSize = 18.sp)
        }
    }
}

@Composable
private fun TabletNavigation(
    selected: AppScreen,
    style: NavigationStyle,
    items: List<AppScreen>,
    onSelected: (AppScreen) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .fillMaxHeight()
            .padding(horizontal = 11.dp, vertical = 15.dp),
    ) {
        items.forEach { item ->
            val isSelected = item == selected
            val accent = if (style == NavigationStyle.COLORFUL) screenAccent(item) else when (style) {
                NavigationStyle.JOURNAL -> MiraGreen
                else -> Color(0xFF3E8FF3)
            }
            val selectedBackground = when (style) {
                NavigationStyle.FRESH -> Color.White.copy(alpha = 0.74f)
                NavigationStyle.COLORFUL -> accent.copy(alpha = 0.16f)
                NavigationStyle.JOURNAL -> Color(0xFFFFE9A8).copy(alpha = 0.68f)
                NavigationStyle.MINIMAL -> Color.White.copy(alpha = 0.78f)
                NavigationStyle.STATIONERY -> Color(0xFF438FF4).copy(alpha = 0.16f)
            }
            val contentColor = if (style == NavigationStyle.JOURNAL) Color(0xFF43604C) else Color(0xFF29476E)
            Surface(
                onClick = { onSelected(item) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(if (style == NavigationStyle.JOURNAL) 7.dp else 13.dp),
                color = if (isSelected) selectedBackground else Color.Transparent,
                shadowElevation = if (isSelected && style == NavigationStyle.MINIMAL) 1.dp else 0.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(30.dp), contentAlignment = Alignment.Center) {
                        NavigationStyleIcon(style, item, Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        item.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) contentColor else if (style == NavigationStyle.JOURNAL) Color(0xFF43604C) else Color(0xFF29476E),
                        fontFamily = if (style == NavigationStyle.JOURNAL) miraHandFont else FontFamily.Default,
                        fontSize = 14.sp,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.height(13.dp))
        }
    }
}

@Composable
private fun NavigationStyleIcon(
    style: NavigationStyle,
    item: AppScreen,
    modifier: Modifier = Modifier.size(28.dp),
) {
    Image(
        painter = painterResource(navigationIconResource(style, item)),
        contentDescription = item.label,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun NavigationClockIcon(circleColor: Color, handColor: Color) {
    Canvas(Modifier.size(24.dp)) {
        drawCircle(circleColor)
        drawLine(handColor, center, Offset(center.x, size.height * 0.27f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(handColor, center, Offset(size.width * 0.72f, size.height * 0.62f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    }
}

@Composable
private fun CompactNavigation(selected: AppScreen, items: List<AppScreen>, onSelected: (AppScreen) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEach { item ->
            TextButton(onClick = { onSelected(item) }) {
                Text("${item.symbol} ${item.label}", color = if (item == selected) MiraBlue else MiraNavy)
            }
        }
    }
}

@Composable
private fun ScreenContent(
    screen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onOpenDictionary: (String) -> Unit,
    dictionaryQuery: String,
    navigationStyle: NavigationStyle,
    onNavigationStyleChanged: (NavigationStyle) -> Unit,
    voiceWakeEnabled: Boolean,
    voiceWakeStatus: String,
    onVoiceWakeEnabledChanged: (Boolean) -> Unit,
    onManualVoiceSessionChanged: (Boolean) -> Unit,
    focusModeActive: Boolean,
    onRequestParentApproval: (String, () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().padding(start = 22.dp, end = 24.dp, bottom = 22.dp)) {
        when (screen) {
            AppScreen.HOME -> HomeScreen(
                onNavigate = onNavigate,
                onOpenDictionary = onOpenDictionary,
                onVoiceSessionChanged = onManualVoiceSessionChanged,
                navigationStyle = navigationStyle,
                focusModeActive = focusModeActive,
                onRequestParentApproval = onRequestParentApproval,
            )
            AppScreen.TIMER -> TimerScreen(
                navigationStyle = navigationStyle,
                onOpenSettings = { onNavigate(AppScreen.SETTINGS) },
                focusModeActive = focusModeActive,
                onRequestParentApproval = onRequestParentApproval,
            )
            AppScreen.DICTIONARY -> DictionaryScreen(initialQuery = dictionaryQuery, onVoiceSessionChanged = onManualVoiceSessionChanged)
            AppScreen.BORROWING -> BorrowingTrainerScreen()
            AppScreen.SCHEDULE -> ScheduleScreen()
            AppScreen.WEATHER -> WeatherScreen()
            AppScreen.SETTINGS -> SettingsScreen(
                navigationStyle = navigationStyle,
                onNavigationStyleChanged = onNavigationStyleChanged,
                voiceWakeEnabled = voiceWakeEnabled,
                voiceWakeStatus = voiceWakeStatus,
                onVoiceWakeEnabledChanged = onVoiceWakeEnabledChanged,
                onRequestParentApproval = onRequestParentApproval,
            )
        }
    }
}

@Composable
private fun MiraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEFC).copy(alpha = 0.93f))
    val elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = cardShape, colors = colors, elevation = elevation) {
            content()
        }
    } else {
        Card(modifier = modifier, shape = cardShape, colors = colors, elevation = elevation) { content() }
    }
}

@Composable
private fun SectionTitle(symbol: String, title: String, color: Color = MiraBlue) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(symbol, fontSize = 27.sp, color = color)
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun HomeScreen(
    onNavigate: (AppScreen) -> Unit,
    onOpenDictionary: (String) -> Unit,
    onVoiceSessionChanged: (Boolean) -> Unit,
    navigationStyle: NavigationStyle,
    focusModeActive: Boolean,
    onRequestParentApproval: (String, () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val todayIndex = LocalDate.now().dayOfWeek.value - 1
    val store = remember { LocalStore(context) }
    val isWeekend = todayIndex !in 0..4
    val previewDay = if (isWeekend) 0 else todayIndex
    val todayLessons = remember(previewDay) {
        store.loadLessons().filter { it.day == previewDay }.sortedBy { it.period }
    }
    val homeSnapshot = remember { store.loadTimer() }
    val focusRecords = remember { store.loadFocusRecords() }
    val weekStart = remember { LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val completedWeekDays = remember(focusRecords, weekStart) {
        focusRecords.map { Instant.ofEpochMilli(it.completedAtMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
            .filter { !it.isBefore(weekStart) && !it.isAfter(weekStart.plusDays(4)) }
            .map { it.dayOfWeek.value }
            .toSet()
    }

    var selectedSubject by rememberSaveable { mutableStateOf(homeSnapshot.subject.ifBlank { "语文" }) }
    var expectedMinutes by rememberSaveable {
        mutableIntStateOf(
            if (homeSnapshot.running || homeSnapshot.elapsedSeconds > 0) homeSnapshot.expectedMinutes
            else store.loadExpectedMinutes(homeSnapshot.subject.ifBlank { "语文" }),
        )
    }
    val restoredHomeElapsed = homeSnapshot.elapsedSeconds + if (homeSnapshot.running && homeSnapshot.startedAtMillis > 0L) {
        ((System.currentTimeMillis() - homeSnapshot.startedAtMillis) / 1000).toInt().coerceAtLeast(0)
    } else 0
    var homeElapsedSeconds by rememberSaveable { mutableIntStateOf(restoredHomeElapsed) }
    var homeSessionBaseSeconds by rememberSaveable { mutableIntStateOf(homeSnapshot.elapsedSeconds) }
    var homeTimerRunning by rememberSaveable { mutableStateOf(homeSnapshot.running) }
    var homeStartedAtMillis by rememberSaveable {
        mutableLongStateOf(if (homeSnapshot.running) homeSnapshot.startedAtMillis else 0L)
    }
    val homeNotificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) Toast.makeText(context, "未开启通知权限，计时仍会继续", Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(homeTimerRunning, homeStartedAtMillis, homeSessionBaseSeconds, focusModeActive) {
        while (homeTimerRunning) {
            homeElapsedSeconds = homeSessionBaseSeconds +
                ((System.currentTimeMillis() - homeStartedAtMillis) / 1000).toInt().coerceAtLeast(0)
            if (focusModeActive && homeElapsedSeconds >= FOCUS_SUBJECT_MAX_MINUTES * 60) {
                val completedAt = System.currentTimeMillis()
                val completedSubject = selectedSubject
                store.addFocusRecord(
                    FocusRecord(
                        completedAt,
                        "${completedSubject}作业",
                        completedSubject,
                        FOCUS_SUBJECT_MAX_MINUTES,
                        completedAt,
                    ),
                )
                homeTimerRunning = false
                homeElapsedSeconds = 0
                homeSessionBaseSeconds = 0
                homeStartedAtMillis = 0L
                TimerAlarmScheduler.cancel(context)
                val nextIndex = (defaultHomeworkSubjects.indexOf(completedSubject) + 1) % defaultHomeworkSubjects.size
                selectedSubject = defaultHomeworkSubjects[nextIndex]
                expectedMinutes = store.loadExpectedMinutes(selectedSubject).coerceAtMost(FOCUS_SUBJECT_MAX_MINUTES)
                store.saveTimer(TimerSnapshot("${selectedSubject}作业", selectedSubject, expectedMinutes, 0, false, 0L))
                Toast.makeText(
                    context,
                    "$completedSubject 已达到 60 分钟，已自动结束并切换到 $selectedSubject",
                    Toast.LENGTH_LONG,
                ).show()
                break
            }
            delay(500)
        }
    }
    fun saveHomeTimer() {
        store.saveTimer(
            TimerSnapshot(
                taskName = "${selectedSubject}作业",
                subject = selectedSubject,
                expectedMinutes = expectedMinutes,
                elapsedSeconds = homeElapsedSeconds,
                running = homeTimerRunning,
                startedAtMillis = homeStartedAtMillis,
            ),
        )
    }
    fun selectHomeSubject(subject: String) {
        if (homeTimerRunning) return
        val subjectExpectedMinutes = store.loadExpectedMinutes(subject)
        selectedSubject = subject
        expectedMinutes = subjectExpectedMinutes
        store.saveTimer(
            store.loadTimer().copy(
                taskName = "${subject}作业",
                subject = subject,
                expectedMinutes = subjectExpectedMinutes,
            ),
        )
    }
    fun toggleHomeTimer() {
        if (homeTimerRunning) {
            homeElapsedSeconds = homeSessionBaseSeconds +
                ((System.currentTimeMillis() - homeStartedAtMillis) / 1000).toInt().coerceAtLeast(0)
            homeSessionBaseSeconds = homeElapsedSeconds
            homeTimerRunning = false
            homeStartedAtMillis = 0L
            TimerAlarmScheduler.cancel(context)
            saveHomeTimer()
        } else {
            homeSessionBaseSeconds = homeElapsedSeconds
            homeStartedAtMillis = System.currentTimeMillis()
            homeTimerRunning = true
            val secondsUntilExpected = expectedMinutes * 60 - homeElapsedSeconds
            if (secondsUntilExpected > 0) {
                TimerAlarmScheduler.schedule(
                    context,
                    "${selectedSubject}作业",
                    expectedMinutes,
                    System.currentTimeMillis() + secondsUntilExpected * 1000L,
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) homeNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            saveHomeTimer()
        }
    }

    var homeQuery by rememberSaveable { mutableStateOf("") }

    var showVoiceOverlay by remember { mutableStateOf(false) }
    var voiceListening by remember { mutableStateOf(false) }
    var voiceTranscript by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var voiceLevel by remember { mutableFloatStateOf(0f) }
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { voiceListening = true; voiceError = null }
            override fun onBeginningOfSpeech() { voiceListening = true }
            override fun onRmsChanged(rmsdB: Float) { voiceLevel = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f) }
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { voiceListening = false }
            override fun onError(error: Int) {
                voiceListening = false
                onVoiceSessionChanged(false)
                voiceError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "没有听清，再说一次吧"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有听到声音，再试一次吧"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "语音服务暂时不可用"
                    else -> "识别没有完成，请再试一次"
                }
            }
            override fun onResults(results: Bundle?) {
                voiceListening = false
                onVoiceSessionChanged(false)
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (spoken.isNotBlank()) {
                    voiceTranscript = spoken
                    val query = extractDictionaryQuery(spoken)
                    homeQuery = query
                    voiceError = null
                    showVoiceOverlay = false
                    onOpenDictionary(query)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                    if (it.isNotBlank()) voiceTranscript = it
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose {
            onVoiceSessionChanged(false)
            speechRecognizer?.destroy()
        }
    }
    fun startHomeVoiceSearch() {
        onVoiceSessionChanged(true)
        showVoiceOverlay = true
        voiceTranscript = ""
        voiceError = null
        if (speechRecognizer == null) {
            onVoiceSessionChanged(false)
            voiceError = "设备暂不支持语音识别"
            return
        }
        voiceListening = true
        runCatching {
            speechRecognizer.startListening(dictionarySpeechIntent().putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true))
        }.onFailure {
            voiceListening = false
            onVoiceSessionChanged(false)
            voiceError = "语音识别暂时无法启动"
        }
    }
    val homeMicrophonePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startHomeVoiceSearch() else {
            showVoiceOverlay = true
            voiceError = "需要麦克风权限才能语音查字"
        }
    }
    fun requestHomeVoiceSearch() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startHomeVoiceSearch()
        } else {
            homeMicrophonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    var permissionEpoch by remember { mutableIntStateOf(0) }
    var weatherRefreshRequest by remember { mutableIntStateOf(0) }
    val initialWeatherCache = remember { store.loadWeatherCache() }
    var locatedWeather by remember { mutableStateOf(initialWeatherCache?.weather) }
    var homeTodayForecast by remember { mutableStateOf<DailyWeather?>(null) }
    var weatherLoading by remember { mutableStateOf(false) }
    var weatherError by remember { mutableStateOf<String?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissionEpoch++
    }
    val hasLocationPermission = remember(permissionEpoch) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    var requestedLocation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission && !requestedLocation) {
            requestedLocation = true
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }
    LaunchedEffect(permissionEpoch, hasLocationPermission, weatherRefreshRequest) {
        if (hasLocationPermission) {
            val cache = store.loadWeatherCache()
            val refreshMillis = store.loadWeatherRefreshMinutes() * 60_000L
            val isFresh = cache != null && System.currentTimeMillis() - cache.updatedAtMillis < refreshMillis
            if (isFresh && weatherRefreshRequest == 0) locatedWeather = cache.weather
            weatherLoading = true
            weatherError = null
            runCatching {
                withContext(Dispatchers.IO) {
                    val city = DeviceWeatherRepository.locate(context.applicationContext)
                    city to WeatherRepository.loadReport(city)
                }
            }
                .onSuccess { (city, report) ->
                    val weather = LocatedWeather(city.name, report.current)
                    locatedWeather = weather
                    homeTodayForecast = report.daily.firstOrNull()
                    store.saveWeatherCache(WeatherCache(weather, System.currentTimeMillis()))
                }
                .onFailure {
                    if (locatedWeather == null) weatherError = it.message ?: "天气获取失败"
                }
            weatherLoading = false
        }
    }

    Box(Modifier.fillMaxSize()) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiraCard(Modifier.weight(0.92f).fillMaxHeight()) {
            Box(
                Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF7DD), Color(0xFFFFFDF7)))),
            ) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        NavigationStyleIcon(navigationStyle, AppScreen.TIMER, Modifier.size(58.dp).offset(y = (-5).dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("作业计时", color = MiraNavy, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            Text("专注学习，高效完成作业", color = Color(0xFF67738A), fontSize = 14.sp)
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Surface(
                            modifier = Modifier.size(205.dp),
                            shape = CircleShape,
                            color = Color(0xFFFFFEFA),
                            border = androidx.compose.foundation.BorderStroke(12.dp, Color(0xFFFFEDB5)),
                        ) {
                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                if (homeTimerRunning) {
                                    Text("正在专注", color = Color(0xFF62708A), fontSize = 18.sp)
                                }
                                Text(formatTimer(homeElapsedSeconds), color = Color(0xFF122F62), fontSize = 50.sp, fontWeight = FontWeight.Black)
                                Text("预计 $expectedMinutes 分钟", color = Color(0xFF62708A), fontSize = 16.sp)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            listOf("语文", "数学", "英语").forEachIndexed { index, item ->
                                HomeSubjectButton(
                                    subject = item,
                                    selected = selectedSubject == item,
                                    color = listOf(Color(0xFFFF8B7E), Color(0xFF5B9CF1), Color(0xFF55BE7C))[index],
                                    onClick = { selectHomeSubject(item) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (homeTimerRunning && focusModeActive) {
                                    onRequestParentApproval("暂停计时") { toggleHomeTimer() }
                                } else {
                                    toggleHomeTimer()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (homeTimerRunning) Color(0xFFE85F59) else Color(0xFFFFB91D),
                            ),
                        ) {
                            Icon(if (homeTimerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, modifier = Modifier.size(30.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (homeTimerRunning) "暂停计时" else "开始计时", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Column(Modifier.weight(1.05f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(0.78f)) {
                Column(
                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFE7FAF1), Color(0xFFF8FFFB))))
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NavigationStyleIcon(navigationStyle, AppScreen.DICTIONARY, Modifier.size(58.dp).offset(y = (-7).dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("查字典", color = MiraNavy, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            Text("认识汉字，探索更大的世界", color = Color(0xFF60748B), fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = homeQuery,
                            onValueChange = { homeQuery = it },
                            modifier = Modifier.weight(1f).height(56.dp),
                            placeholder = { Text("请输入要查询的汉字", color = Color(0xFF8B97AB), fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Color(0xFF8090A8), modifier = Modifier.size(22.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(28.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF38BC79),
                                unfocusedBorderColor = Color.Transparent,
                            ),
                        )
                        Spacer(Modifier.width(9.dp))
                        Surface(onClick = { requestHomeVoiceSearch() }, color = Color(0xFF36B979), shape = CircleShape) {
                            Icon(Icons.Rounded.Mic, "语音查字", tint = Color.White, modifier = Modifier.padding(12.dp).size(24.dp))
                        }
                    }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(0.80f)) {
                DailySentencePanel()
            }
            MiraCard(Modifier.fillMaxWidth().weight(0.42f)) {
                Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StudyProgressStar(completed = true, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("本周学习进度", color = MiraNavy, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Text("点亮星星，见证成长！", color = Color(0xFF8090A8), fontSize = 10.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("周一", "周二", "周三", "周四", "周五").forEachIndexed { index, label ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                StudyProgressStar(
                                    completed = index + 1 in completedWeekDays,
                                    modifier = Modifier.size(30.dp),
                                )
                                Text(label, color = Color(0xFF68758C), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        Column(Modifier.weight(1.08f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MiraCard(
                Modifier.fillMaxWidth().weight(1.5f),
                onClick = if (focusModeActive) null else ({ onNavigate(AppScreen.SCHEDULE) }),
            ) {
                Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF1F8FF), Color.White))).padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NavigationStyleIcon(navigationStyle, AppScreen.SCHEDULE, Modifier.size(58.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("课程表", color = MiraNavy, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            Text(if (isWeekend) "周一课程，提前做好准备" else "好好上课，收获新知识", color = Color(0xFF60748B), fontSize = 14.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (focusModeActive) "专注时仅预览" else "完整课表",
                                color = if (focusModeActive) Color(0xFF8292A7) else Color(0xFF458FE6),
                                fontSize = 13.sp,
                            )
                            if (!focusModeActive) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Color(0xFF458FE6), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Column(
                            Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("上午", color = Color(0xFF5F7693), fontSize = 12.sp, fontWeight = FontWeight.Black)
                            (1..4).forEach { period ->
                                HomeLessonRow(
                                    period,
                                    todayLessons.firstOrNull { it.period == period },
                                    Modifier.weight(1f),
                                )
                            }
                        }
                        Column(
                            Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("下午", color = Color(0xFF5F7693), fontSize = 12.sp, fontWeight = FontWeight.Black)
                            (5..7).forEach { period ->
                                HomeLessonRow(
                                    period,
                                    todayLessons.firstOrNull { it.period == period },
                                    Modifier.weight(1f),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(0.5f)) {
                Box(
                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFBFE6FF), Color(0xFFEAF8FF)))),
                ) {
                    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 11.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painterResource(weatherIconResource(locatedWeather?.info?.weatherCode ?: 1)),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.width(7.dp))
                            Text("天气", color = MiraNavy, fontSize = 17.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.weight(1f))
                            Text("今日温度", color = Color(0xFF66809E), fontSize = 10.sp)
                        }
                        Spacer(Modifier.height(2.dp))
                        when {
                            !hasLocationPermission -> {
                                Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = Color(0xFF4E94E8), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("开启定位后显示实时天气", color = Color(0xFF536B89), fontSize = 11.sp)
                                    Spacer(Modifier.weight(1f))
                                    TextButton(onClick = {
                                        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                                    }) { Text("开启定位", fontSize = 12.sp) }
                                }
                            }
                            weatherLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("正在获取当前位置天气…", color = Color(0xFF536B89)) }
                            locatedWeather != null -> {
                                val weather = locatedWeather!!
                                Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(weather.placeName, color = MiraNavy, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                            Text("  ·  ${weather.info.description}", color = Color(0xFF536B89), fontSize = 11.sp)
                                        }
                                        Text(
                                            "湿度 ${weather.info.humidity}%  ·  风速 ${weather.info.windSpeed.toInt()} km/h",
                                            color = Color(0xFF526C8D),
                                            fontSize = 10.sp,
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("${weather.info.temperature.toInt()}℃", color = Color(0xFF102E61), fontSize = 31.sp, fontWeight = FontWeight.Black)
                                        Spacer(Modifier.width(7.dp))
                                        Text(
                                            homeTodayForecast?.let { "${it.minTemperature.toInt()}℃ ~ ${it.maxTemperature.toInt()}℃" } ?: "--℃ ~ --℃",
                                            color = Color(0xFF315F8C),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 3.dp),
                                        )
                                    }
                                }
                            }
                            else -> {
                                Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Text(weatherError ?: "暂时无法获取天气", color = Color(0xFF536B89), fontSize = 11.sp)
                                    Spacer(Modifier.weight(1f))
                                    TextButton(onClick = { weatherRefreshRequest++ }) { Text("重新获取", fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        if (showVoiceOverlay) {
            Box(Modifier.fillMaxSize().background(Color(0xFFF7FBFF).copy(alpha = 0.64f)))
            HomeVoiceOverlay(
                listening = voiceListening,
                transcript = voiceTranscript,
                error = voiceError,
                level = voiceLevel,
                onRetry = { startHomeVoiceSearch() },
                onCancel = {
                    speechRecognizer?.cancel()
                    onVoiceSessionChanged(false)
                    showVoiceOverlay = false
                    voiceListening = false
                },
                onComplete = {
                    onVoiceSessionChanged(false)
                    if (voiceTranscript.isNotBlank()) {
                        val query = extractDictionaryQuery(voiceTranscript)
                        homeQuery = query
                        onOpenDictionary(query)
                    }
                    showVoiceOverlay = false
                },
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun HomeVoiceOverlay(
    listening: Boolean,
    transcript: String,
    error: String?,
    level: Float,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "voicePulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300), repeatMode = RepeatMode.Restart),
        label = "voicePulseRadius",
    )
    Surface(
        modifier = modifier.width(370.dp),
        color = Color(0xFFF2FFF9),
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 14.dp,
    ) {
        Column(Modifier.padding(horizontal = 30.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (listening) "正在听…" else if (error != null) "再试一次吧" else "听到了！", color = MiraNavy, fontSize = 29.sp, fontWeight = FontWeight.Black)
            Text("说出你想查的汉字", color = Color(0xFF61748E), fontSize = 16.sp, modifier = Modifier.padding(top = 3.dp))
            Box(Modifier.size(154.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val energy = 0.15f + level * 0.25f
                    val base = size.minDimension / 2f
                    drawCircle(Color(0xFF68D8A5).copy(alpha = (0.15f * (1f - pulse)).coerceAtLeast(0.03f)), radius = base * (0.64f + pulse * 0.34f))
                    drawCircle(Color(0xFF68D8A5).copy(alpha = 0.13f + energy), radius = base * 0.68f)
                    drawCircle(Color(0xFF43C887).copy(alpha = 0.24f), radius = base * 0.52f)
                }
                Surface(color = Color(0xFF38BC79), shape = CircleShape, shadowElevation = 6.dp, modifier = Modifier.size(78.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Mic, null, tint = Color.White, modifier = Modifier.size(42.dp))
                    }
                }
            }
            Surface(color = Color.White, shape = RoundedCornerShape(24.dp), shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Mic, null, tint = Color(0xFF37BE7C), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        error ?: transcript.ifBlank { if (listening) "请开始说话…" else "等待识别结果…" },
                        color = if (error != null) MaterialTheme.colorScheme.error else MiraNavy,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp)) { Text("取消") }
                Button(
                    onClick = if (error != null) onRetry else onComplete,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DBE7D)),
                ) { Text(if (error != null) "重试" else "完成") }
            }
        }
    }
}

@Composable
private fun HomeSubjectButton(
    subject: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        color = if (selected) color else color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(19.dp),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(subject, color = if (selected) Color.White else color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun StudyProgressStar(completed: Boolean, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        if (completed) {
            Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFC52E), modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Rounded.StarBorder, null, tint = Color(0xFFAEBBCD), modifier = Modifier.fillMaxSize())
        }
    }
}

private data class DailySentence(
    val text: String,
    val source: String,
    val keywords: List<String>,
)

// Curated, child-friendly public-domain lines. Corpus reference:
// https://github.com/chinese-poetry/chinese-poetry (MIT).
private val dailySentences = listOf(
    DailySentence("读书破万卷，下笔如有神。", "唐 · 杜甫", listOf("勤学", "积累")),
    DailySentence("欲穷千里目，更上一层楼。", "唐 · 王之涣《登鹳雀楼》", listOf("进取", "成长")),
    DailySentence("纸上得来终觉浅，绝知此事要躬行。", "宋 · 陆游《冬夜读书示子聿》", listOf("实践", "求知")),
    DailySentence("学而时习之，不亦说乎？", "《论语 · 学而》", listOf("学习", "复习")),
    DailySentence("不积跬步，无以至千里。", "《荀子 · 劝学》", listOf("坚持", "积累")),
    DailySentence("少壮不努力，老大徒伤悲。", "汉乐府《长歌行》", listOf("勤奋", "惜时")),
    DailySentence("会当凌绝顶，一览众山小。", "唐 · 杜甫《望岳》", listOf("勇气", "志向")),
    DailySentence("山重水复疑无路，柳暗花明又一村。", "宋 · 陆游《游山西村》", listOf("希望", "坚持")),
    DailySentence("海内存知己，天涯若比邻。", "唐 · 王勃《送杜少府之任蜀州》", listOf("友谊", "豁达")),
    DailySentence("千里之行，始于足下。", "《道德经》", listOf("行动", "坚持")),
)

@Composable
private fun DailySentencePanel() {
    val sentence = dailySentences[(LocalDate.now().dayOfYear - 1) % dailySentences.size]
    val displayText = remember(sentence.text) {
        val separator = sentence.text.indexOf('，')
        if (sentence.text.length >= 14 && separator in 4..(sentence.text.lastIndex - 4)) {
            sentence.text.replaceFirst("，", "，\n")
        } else sentence.text
    }
    Box(
        Modifier.fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFFFFFDF4), Color(0xFFF0FBF4))))
            .padding(horizontal = 20.dp, vertical = 15.dp),
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.mira_home_daily_quote_v1),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.width(9.dp))
                Text("每日好句", color = Color(0xFF315A73), fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                Surface(color = Color(0xFFFFE7A8), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        sentence.keywords.joinToString(" · "),
                        Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = Color(0xFF9B6710),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "“$displayText”",
                    color = MiraNavy,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 29.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
            Text(sentence.source, color = Color(0xFF65758A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeLessonRow(period: Int, lesson: Lesson?, modifier: Modifier = Modifier) {
    val colors = listOf(Color(0xFFFFE8E5), Color(0xFFE5F1FF), Color(0xFFE8F7E5), Color(0xFFFFF2CE))
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (lesson != null) colors[(period - 1).mod(colors.size)] else Color(0xFFF2F5F9),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("第${period}节", color = if (lesson != null) MiraNavy else Color(0xFF8C99AC), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(48.dp))
            if (lesson != null) {
                Image(
                    painterResource(scheduleLessonIcon(lesson.name)),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(Modifier.width(7.dp))
            }
            Text(
                lesson?.name ?: "暂无课程",
                color = if (lesson != null) MiraNavy else Color(0xFF9AA6B8),
                fontSize = 13.sp,
                fontWeight = if (lesson != null) FontWeight.Black else FontWeight.Normal,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TimerScreen(
    navigationStyle: NavigationStyle,
    onOpenSettings: () -> Unit,
    focusModeActive: Boolean,
    onRequestParentApproval: (String, () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val store = remember { LocalStore(context) }
    val snapshot = remember { store.loadTimer() }
    val records = remember { mutableStateListOf<FocusRecord>().also { it.addAll(store.loadFocusRecords()) } }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) Toast.makeText(context, "未开启通知权限，计时仍可使用，但后台结束时不会弹出提醒", Toast.LENGTH_LONG).show()
    }
    val configuredSubjects = remember(focusModeActive) {
        if (focusModeActive) defaultHomeworkSubjects else store.loadHomeworkSubjects()
    }
    var subject by rememberSaveable {
        mutableStateOf(snapshot.subject.takeIf { it in configuredSubjects } ?: configuredSubjects.first())
    }
    var expectedMinutes by rememberSaveable {
        mutableIntStateOf(
            (if (snapshot.running || snapshot.elapsedSeconds > 0) snapshot.expectedMinutes
            else store.loadExpectedMinutes(snapshot.subject.takeIf { it in configuredSubjects } ?: configuredSubjects.first()))
                .coerceAtMost(if (focusModeActive) FOCUS_SUBJECT_MAX_MINUTES else 99),
        )
    }
    val restoredElapsed = snapshot.elapsedSeconds + if (snapshot.running && snapshot.startedAtMillis > 0L) {
        ((System.currentTimeMillis() - snapshot.startedAtMillis) / 1000).toInt().coerceAtLeast(0)
    } else 0
    var elapsedSeconds by rememberSaveable { mutableIntStateOf(restoredElapsed) }
    var sessionBaseSeconds by rememberSaveable { mutableIntStateOf(snapshot.elapsedSeconds) }
    var running by rememberSaveable { mutableStateOf(snapshot.running) }
    var startedAtMillis by rememberSaveable {
        mutableLongStateOf(if (snapshot.running) snapshot.startedAtMillis else 0L)
    }

    fun saveCurrent() = store.saveTimer(
        TimerSnapshot("${subject}作业", subject, expectedMinutes, elapsedSeconds, running, startedAtMillis)
    )

    fun toggleTimer() {
        if (running) {
            elapsedSeconds = sessionBaseSeconds + ((System.currentTimeMillis() - startedAtMillis) / 1000).toInt().coerceAtLeast(0)
            sessionBaseSeconds = elapsedSeconds
            running = false
            startedAtMillis = 0L
            TimerAlarmScheduler.cancel(context)
            saveCurrent()
        } else {
            sessionBaseSeconds = elapsedSeconds
            startedAtMillis = System.currentTimeMillis()
            running = true
            val secondsUntilExpected = expectedMinutes * 60 - elapsedSeconds
            if (secondsUntilExpected > 0) {
                TimerAlarmScheduler.schedule(
                    context, "${subject}作业", expectedMinutes,
                    System.currentTimeMillis() + secondsUntilExpected * 1000L,
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            saveCurrent()
        }
    }

    fun resetTimer() {
        running = false
        elapsedSeconds = 0
        sessionBaseSeconds = 0
        startedAtMillis = 0L
        TimerAlarmScheduler.cancel(context)
        saveCurrent()
    }

    fun completeTimer(autoAdvance: Boolean = false) {
        val completedAt = System.currentTimeMillis()
        if (running) {
            elapsedSeconds = sessionBaseSeconds + ((completedAt - startedAtMillis) / 1000).toInt().coerceAtLeast(0)
        }
        if (focusModeActive) elapsedSeconds = elapsedSeconds.coerceAtMost(FOCUS_SUBJECT_MAX_MINUTES * 60)
        val actualMinutes = max(1, (elapsedSeconds + 59) / 60)
            .coerceAtMost(if (focusModeActive) FOCUS_SUBJECT_MAX_MINUTES else Int.MAX_VALUE)
        val completedSubject = subject
        running = false
        startedAtMillis = 0L
        TimerAlarmScheduler.cancel(context)
        store.addFocusRecord(FocusRecord(completedAt, "${completedSubject}作业", completedSubject, actualMinutes, completedAt))
        records.clear()
        records.addAll(store.loadFocusRecords())
        elapsedSeconds = 0
        sessionBaseSeconds = 0
        if (autoAdvance && focusModeActive) {
            val nextIndex = (configuredSubjects.indexOf(completedSubject) + 1) % configuredSubjects.size
            subject = configuredSubjects[nextIndex]
            expectedMinutes = store.loadExpectedMinutes(subject).coerceAtMost(FOCUS_SUBJECT_MAX_MINUTES)
        }
        saveCurrent()
        if (autoAdvance) {
            Toast.makeText(context, "$completedSubject 已达到 60 分钟，已自动结束并切换到 $subject", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(focusModeActive) {
        if (focusModeActive) {
            if (subject !in configuredSubjects) subject = configuredSubjects.first()
            expectedMinutes = store.loadExpectedMinutes(subject).coerceAtMost(FOCUS_SUBJECT_MAX_MINUTES)
            saveCurrent()
        }
    }

    LaunchedEffect(running, startedAtMillis, sessionBaseSeconds, focusModeActive) {
        while (running) {
            elapsedSeconds = sessionBaseSeconds +
                ((System.currentTimeMillis() - startedAtMillis) / 1000).toInt().coerceAtLeast(0)
            if (focusModeActive && elapsedSeconds >= FOCUS_SUBJECT_MAX_MINUTES * 60) {
                elapsedSeconds = FOCUS_SUBJECT_MAX_MINUTES * 60
                completeTimer(autoAdvance = true)
                break
            }
            delay(500)
        }
    }

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.weight(0.93f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(0.47f)) {
                Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 17.dp)) {
                    TimerSectionTitle(timerCurrentSectionIcon(navigationStyle), "科目选择")
                    Spacer(Modifier.weight(1f))
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        configuredSubjects.forEach { item ->
                            TimerSubjectCard(
                                subject = item,
                                selected = subject == item,
                                enabled = !running,
                                onClick = {
                                    val subjectExpectedMinutes = store.loadExpectedMinutes(item)
                                        .coerceAtMost(if (focusModeActive) FOCUS_SUBJECT_MAX_MINUTES else 99)
                                    subject = item
                                    expectedMinutes = subjectExpectedMinutes
                                    store.saveTimer(
                                        TimerSnapshot(
                                            "${item}作业",
                                            item,
                                            subjectExpectedMinutes,
                                            elapsedSeconds,
                                            running,
                                            startedAtMillis,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    HorizontalDivider(color = Color(0xFFE8EDF3))
                    Spacer(Modifier.weight(1f))
                    if (focusModeActive) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFEEE9),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3B7A8)),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Star, null, tint = Color(0xFFE06A4E))
                                Spacer(Modifier.width(9.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("专注模式已开启", color = MiraNavy, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    Text("仅开放首页、计时和字典 · 每科最多 60 分钟", color = Color(0xFF7C665F), fontSize = 11.sp)
                                }
                                Text("长按首页标题退出", color = Color(0xFFE06A4E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Surface(
                            onClick = onOpenSettings,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF2F7FF),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text(
                                "设置预计时间 · 点首页标题进入专注模式",
                                Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
                                color = MiraNavy,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(0.53f)) {
                TimerWeeklyChart(records = records, navigationStyle = navigationStyle)
            }
        }

        Column(Modifier.weight(1.18f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(0.62f)) {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color.White, Color(0xFFFBFDFF))))) {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Box(
                            Modifier.size(220.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(Modifier.fillMaxSize().aspectRatio(1f)) {
                                val strokeWidth = size.minDimension * 0.075f
                                val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                val inset = strokeWidth / 2f + 4f
                                val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                                drawArc(
                                    color = Color(0xFFE4EAF2), startAngle = -90f, sweepAngle = 360f,
                                    useCenter = false, topLeft = Offset(inset, inset), size = arcSize, style = stroke,
                                )
                                val progress = min(1f, elapsedSeconds / (expectedMinutes * 60f))
                                if (progress > 0f) {
                                    drawArc(
                                        color = subjectAccent(subject), startAngle = -90f, sweepAngle = 360f * progress,
                                        useCenter = false, topLeft = Offset(inset, inset), size = arcSize, style = stroke,
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    formatTimer(elapsedSeconds),
                                    color = Color(0xFF082C63),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 52.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.sp,
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                )
                                Text(if (running) "正在专注" else "从零开始", color = Color(0xFF71809A), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    if (running && focusModeActive) {
                                        onRequestParentApproval("暂停计时") { toggleTimer() }
                                    } else {
                                        toggleTimer()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (running) Color(0xFFE85F59) else Color(0xFFFFAF08),
                                ),
                            ) {
                                Icon(if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (running) "暂停" else "开始", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (focusModeActive) onRequestParentApproval("重置计时") { resetTimer() }
                                    else resetTimer()
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6682A6)),
                            ) {
                                Icon(Icons.Rounded.Refresh, null)
                                Spacer(Modifier.width(5.dp))
                                Text("重置", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { completeTimer() },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF159565)),
                            ) {
                                Icon(Icons.Rounded.Check, null)
                                Spacer(Modifier.width(4.dp))
                                Text("完成", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().weight(0.38f)) {
                TimerMotivationBanner()
            }
        }
    }
}

@Composable
private fun TimerSubjectCard(
    subject: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val accent = subjectAccent(subject)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.width(116.dp).height(58.dp),
        color = if (selected) accent else accent.copy(alpha = 0.10f),
        shape = RoundedCornerShape(17.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accent.copy(alpha = if (selected) 1f else 0.45f)),
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(scheduleLessonIcon(subject)),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                subject,
                color = if (selected) Color.White else accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun TimerWeeklyChart(records: List<FocusRecord>, navigationStyle: NavigationStyle) {
    val context = LocalContext.current
    val configuredSubjects = remember { LocalStore(context).loadHomeworkSubjects() }
    var bySubject by rememberSaveable { mutableStateOf(false) }
    val weekStart = remember { LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val weekRecords = records.filter { record ->
        val date = Instant.ofEpochMilli(record.completedAtMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        !date.isBefore(weekStart) && !date.isAfter(weekStart.plusDays(6))
    }
    val rows = if (bySubject) {
        (configuredSubjects + weekRecords.map { it.subject }).distinct().map { item ->
            item to weekRecords.filter { it.subject == item }.sumOf { it.durationMinutes }
        }
    } else {
        val names = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        names.mapIndexed { index, name ->
            val date = weekStart.plusDays(index.toLong())
            name to weekRecords.filter {
                Instant.ofEpochMilli(it.completedAtMillis).atZone(ZoneId.systemDefault()).toLocalDate() == date
            }.sumOf { it.durationMinutes }
        }
    }
    val maximum = max(1, rows.maxOfOrNull { it.second } ?: 1)
    val axisMaximum = timerChartAxisMaximum(maximum)
    val ticks = listOf(axisMaximum, axisMaximum * 3 / 4, axisMaximum / 2, axisMaximum / 4, 0)
    val colors = listOf(
        Color(0xFF5A98F5), Color(0xFF64CEB0), Color(0xFFFFD852), Color(0xFFFF8175),
        Color(0xFF9B72E8), Color(0xFF45ADEB), Color(0xFF8BD15B),
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 17.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TimerSectionTitle(timerWeeklySectionIcon(navigationStyle), "本周作业时长")
            Spacer(Modifier.weight(1f))
            if (axisMaximum > 120) {
                Text(
                    "按最高 $axisMaximum 分钟显示",
                    color = Color(0xFF6C7B91),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Surface(color = Color(0xFFF0F3F8), shape = RoundedCornerShape(22.dp)) {
                Row(Modifier.padding(3.dp)) {
                    listOf(false to "按天", true to "按学科").forEach { (mode, label) ->
                        Surface(
                            onClick = { bySubject = mode },
                            color = if (bySubject == mode) Color(0xFF438FF4) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Text(
                                label,
                                Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                color = if (bySubject == mode) Color.White else Color(0xFF526A88),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.width(38.dp).fillMaxHeight()) {
                Text("分钟", color = Color(0xFF637590), fontSize = 10.sp, modifier = Modifier.height(16.dp))
                Column(Modifier.height(124.dp).fillMaxWidth(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
                    ticks.forEach { tick -> Text(tick.toString(), color = Color(0xFF60718B), fontSize = 10.sp) }
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().height(124.dp)) {
                    Canvas(Modifier.fillMaxSize()) {
                        repeat(5) { index ->
                            val y = size.height * index / 4f
                            drawLine(Color(0xFFDDE5EE), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.2f)
                        }
                        drawLine(Color(0xFFBFCAD8), Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 1.4f)
                    }
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                        rows.forEachIndexed { index, (_, minutes) ->
                            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(Modifier.weight(1f))
                                Text(if (minutes == 0) "" else minutes.toString(), color = Color(0xFF41587A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    Modifier.width(27.dp).height(
                                        if (minutes == 0) 3.dp else (96f * minutes / axisMaximum.toFloat()).coerceAtLeast(8f).dp,
                                    ).background(colors[index % colors.size], RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp)),
                                )
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 5.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    rows.forEach { (label, _) ->
                        Text(label, modifier = Modifier.weight(1f), color = Color(0xFF526A88), fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerSectionTitle(iconRes: Int, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.width(9.dp))
        Text(title, color = MiraNavy, fontSize = 21.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TimerMotivationBanner() {
    Box(Modifier.fillMaxSize()) {
        Surface(
            color = Color(0xFFFFF6D6).copy(alpha = 0.78f),
            shape = RoundedCornerShape(46.dp),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp, end = 235.dp),
        ) {
            Column(Modifier.padding(horizontal = 25.dp, vertical = 14.dp)) {
                Text("认真写作业，", color = Color(0xFF17385F), fontFamily = miraHandFont, fontSize = 21.sp)
                Text("就是在遇见更棒的自己！", color = Color(0xFF17385F), fontFamily = miraHandFont, fontSize = 21.sp)
                Text("        〰〰  ⭐", color = Color(0xFFFFC928), fontSize = 17.sp)
            }
        }
        Image(
            painterResource(R.drawable.mira_timer_student_desk),
            contentDescription = "认真写作业的小学生",
            modifier = Modifier.align(Alignment.BottomEnd).width(300.dp).fillMaxHeight(),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun SettingsScreen(
    navigationStyle: NavigationStyle,
    onNavigationStyleChanged: (NavigationStyle) -> Unit,
    voiceWakeEnabled: Boolean,
    voiceWakeStatus: String,
    onVoiceWakeEnabledChanged: (Boolean) -> Unit,
    onRequestParentApproval: (String, () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val store = remember { LocalStore(context) }
    val configuredSubjects = remember { mutableStateListOf<String>().also { it.addAll(store.loadHomeworkSubjects()) } }
    var expectedSubject by rememberSaveable { mutableStateOf(configuredSubjects.first()) }
    var expectedMinutes by rememberSaveable { mutableIntStateOf(store.loadExpectedMinutes(expectedSubject)) }
    var weatherRefreshHours by rememberSaveable {
        mutableIntStateOf(((store.loadWeatherRefreshMinutes() + 59) / 60).coerceIn(1, 12))
    }
    var newSubject by rememberSaveable { mutableStateOf("") }
    var dictionaryDailyLimit by rememberSaveable { mutableIntStateOf(store.loadDictionaryDailyLimit()) }
    var showDictionaryLimitEditor by remember { mutableStateOf(false) }
    var dictionaryLimitDraft by remember { mutableIntStateOf(dictionaryDailyLimit) }
    val expectedMaximum = maximumExpectedMinutes(expectedSubject)

    fun updateExpected(minutes: Int) {
        expectedMinutes = minutes.coerceIn(10, expectedMaximum)
        store.saveExpectedMinutes(expectedSubject, expectedMinutes)
        val timer = store.loadTimer()
        if (timer.subject == expectedSubject && !timer.running && timer.elapsedSeconds == 0) {
            store.saveTimer(timer.copy(expectedMinutes = expectedMinutes))
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().weight(1.08f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MiraCard(Modifier.weight(1.05f).fillMaxHeight()) {
                Column(
                    Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFFBF2), Color.White))).padding(22.dp),
                ) {
                    SettingsCardTitle(R.drawable.mira_settings_group_timer, "计时设置", "拖动圆环，为每个学科单独设置")
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            Text("选择学科", color = MiraNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(5.dp))
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                            ) {
                                configuredSubjects.forEach { item ->
                                    SettingsSubjectChip(
                                        subject = item,
                                        selected = expectedSubject == item,
                                        onClick = {
                                            expectedSubject = item
                                            expectedMinutes = store.loadExpectedMinutes(item)
                                        },
                                    )
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Surface(color = Color(0xFFFFF4D7), shape = RoundedCornerShape(15.dp)) {
                                Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp)) {
                                    Text("${expectedSubject}预计完成时间", color = Color(0xFF7B5721), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    Text(
                                        "范围 10–$expectedMaximum 分钟；专注模式下最多 60 分钟。",
                                        color = Color(0xFF826F54),
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        CircularValueSlider(
                            value = expectedMinutes,
                            valueRange = 10..expectedMaximum,
                            onValueChange = ::updateExpected,
                            unit = "分钟",
                            color = subjectAccent(expectedSubject),
                            modifier = Modifier.size(158.dp),
                        )
                    }
                }
            }

            MiraCard(Modifier.weight(1f).fillMaxHeight()) {
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    SettingsCardTitle(R.drawable.mira_settings_group_style, "菜单栏风格", "点击后立即预览并自动保存")
                    Spacer(Modifier.height(8.dp))
                    NavigationStyle.entries.chunked(2).forEach { rowStyles ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowStyles.forEach { style ->
                                Surface(
                                    onClick = { onNavigationStyleChanged(style) },
                                    modifier = Modifier.weight(1f).padding(vertical = 3.dp),
                                    color = if (navigationStyle == style) Color(0xFFE7F0FF) else Color(0xFFF6F8FB),
                                    shape = RoundedCornerShape(15.dp),
                                    border = if (navigationStyle == style) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF77A9F6)) else null,
                                ) {
                                    Row(Modifier.padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                        NavigationStylePreview(style)
                                        Spacer(Modifier.width(7.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(style.label, color = MiraNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(style.description.substringBefore('，'), color = Color(0xFF718097), fontSize = 10.sp, maxLines = 1)
                                        }
                                        if (navigationStyle == style) Text("✓", color = MiraBlue, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                            if (rowStyles.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth().weight(0.92f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MiraCard(Modifier.weight(1.05f).fillMaxHeight()) {
                Column(
                    Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF2FAFF), Color.White))).padding(20.dp),
                ) {
                    SettingsCardTitle(
                        iconRes = R.drawable.mira_settings_group_common,
                        title = "常用设置",
                        subtitle = "天气、查字次数与语音唤醒",
                        action = {
                            Surface(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                                    }.onFailure {
                                        Toast.makeText(context, "请在系统默认应用中更换桌面", Toast.LENGTH_LONG).show()
                                    }
                                },
                                color = Color(0xFF7B72E9),
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    "更换系统桌面",
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        },
                    )
                    Spacer(Modifier.height(7.dp))
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("天气刷新", color = MiraNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            CircularValueSlider(
                                value = weatherRefreshHours,
                                valueRange = 1..12,
                                onValueChange = { hours ->
                                    weatherRefreshHours = hours
                                    store.saveWeatherRefreshMinutes(hours * 60)
                                },
                                unit = "小时",
                                color = Color(0xFF49A5E8),
                                modifier = Modifier.size(112.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            onClick = {
                                onRequestParentApproval("修改今日查字次数") {
                                    dictionaryLimitDraft = dictionaryDailyLimit
                                    showDictionaryLimitEditor = true
                                }
                            },
                            modifier = Modifier.width(145.dp).fillMaxHeight(),
                            color = Color(0xFFFFF4D7),
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Column(
                                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text("今日查字上限", color = Color(0xFF7B5721), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Text(dictionaryDailyLimit.toString(), color = Color(0xFFE18B25), fontSize = 34.sp, fontWeight = FontWeight.Black)
                                Text(
                                    "已用 ${store.loadDictionaryLookupWords().size} 次 · 点击修改",
                                    color = Color(0xFF826F54),
                                    fontSize = 9.sp,
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFEAF8F1),
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = Color.White, shape = CircleShape, modifier = Modifier.size(36.dp)) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Rounded.Mic, null, tint = Color(0xFF239D70), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "你好小智",
                                        color = MiraNavy,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                Spacer(Modifier.height(7.dp))
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "语音唤醒 · $voiceWakeStatus",
                                        color = if (voiceWakeEnabled) MiraGreen else Color(0xFF718097),
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Switch(
                                        checked = voiceWakeEnabled,
                                        onCheckedChange = onVoiceWakeEnabledChanged,
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MiraGreen),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            MiraCard(Modifier.weight(1f).fillMaxHeight()) {
                Box(Modifier.fillMaxSize().background(Color(0xFFF2FBF6))) {
                    Image(
                        painterResource(R.drawable.mira_green_hills),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(72.dp).alpha(0.34f),
                        contentScale = ContentScale.FillBounds,
                    )
                    Column(Modifier.fillMaxSize().padding(20.dp)) {
                        SettingsCardTitle(R.drawable.mira_settings_group_subjects, "计时学科", "默认保留语文、数学、英语")
                        Spacer(Modifier.height(7.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            configuredSubjects.forEach { item ->
                                val removable = item !in defaultHomeworkSubjects
                                SettingsSubjectChip(
                                    subject = item,
                                    removable = removable,
                                    onRemove = if (!removable) null else {
                                        {
                                            configuredSubjects.remove(item)
                                            store.saveHomeworkSubjects(configuredSubjects)
                                            if (expectedSubject == item) {
                                                expectedSubject = configuredSubjects.first()
                                                expectedMinutes = store.loadExpectedMinutes(expectedSubject)
                                            }
                                        }
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newSubject,
                                onValueChange = { newSubject = it.take(8) },
                                modifier = Modifier.weight(1f).height(49.dp),
                                placeholder = { Text("添加学科，如：科学", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                            )
                            Button(
                                onClick = {
                                    val value = newSubject.trim()
                                    when {
                                        value.isBlank() -> Toast.makeText(context, "请输入学科名称", Toast.LENGTH_SHORT).show()
                                        value in configuredSubjects -> Toast.makeText(context, "这个学科已经有了", Toast.LENGTH_SHORT).show()
                                        configuredSubjects.size >= 9 -> Toast.makeText(context, "最多添加 9 个学科", Toast.LENGTH_SHORT).show()
                                        else -> {
                                            configuredSubjects.add(value)
                                            store.saveHomeworkSubjects(configuredSubjects)
                                            newSubject = ""
                                        }
                                    }
                                },
                                modifier = Modifier.height(47.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MiraGreen),
                            ) { Text("＋ 添加") }
                        }
                        Spacer(Modifier.weight(1f))
                        Text("学习记录与字典数据均保存在本机", color = MiraGreen, fontSize = 11.sp)
                    }
                }
            }
        }
    }
    if (showDictionaryLimitEditor) {
        AlertDialog(
            onDismissRequest = { showDictionaryLimitEditor = false },
            title = { Text("设置今日查字次数") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "沿圆环拖动设置次数，仅在今天有效；明天自动恢复为 $DEFAULT_DICTIONARY_DAILY_LIMIT 次。",
                        textAlign = TextAlign.Center,
                    )
                    CircularValueSlider(
                        value = dictionaryLimitDraft,
                        valueRange = 1..200,
                        onValueChange = { dictionaryLimitDraft = it },
                        unit = "次",
                        color = Color(0xFFE9A12D),
                        modifier = Modifier.size(210.dp),
                    )
                    Text("今日已查询 ${store.loadDictionaryLookupWords().size} 个不同词条", color = Color(0xFF718097), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val limit = dictionaryLimitDraft.coerceIn(1, 200)
                    dictionaryDailyLimit = limit
                    store.saveDictionaryDailyLimit(limit)
                    showDictionaryLimitEditor = false
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showDictionaryLimitEditor = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun SettingsCardTitle(
    iconRes: Int,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(iconRes), null, Modifier.size(48.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = MiraNavy, fontSize = 20.sp, fontWeight = FontWeight.Black)
                if (action != null) {
                    Spacer(Modifier.width(9.dp))
                    action()
                }
            }
            Text(subtitle, color = Color(0xFF718097), fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingsSubjectChip(
    subject: String,
    selected: Boolean = false,
    removable: Boolean = false,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
) {
    val accent = subjectAccent(subject)
    val chipContent: @Composable () -> Unit = {
        Row(
            Modifier.padding(start = 9.dp, end = if (removable) 5.dp else 11.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painterResource(scheduleLessonIcon(subject)), null, Modifier.size(27.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(6.dp))
            Text(
                subject,
                color = if (selected) Color.White else accent,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
            )
            if (removable && onRemove != null) {
                Spacer(Modifier.width(5.dp))
                Surface(onClick = onRemove, color = accent, shape = CircleShape, modifier = Modifier.size(19.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("×", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            color = if (selected) accent else accent.copy(alpha = 0.11f),
            shape = RoundedCornerShape(17.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, accent.copy(alpha = if (selected) 1f else 0.42f)),
            content = chipContent,
        )
    } else {
        Surface(
            color = accent.copy(alpha = 0.11f),
            shape = RoundedCornerShape(17.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, accent.copy(alpha = 0.42f)),
            content = chipContent,
        )
    }
}

@Composable
private fun CircularValueSlider(
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val safeValue = value.coerceIn(valueRange.first, valueRange.last)
    val rangeSize = (valueRange.last - valueRange.first).coerceAtLeast(1)
    val progress = (safeValue - valueRange.first) / rangeSize.toFloat()
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier.fillMaxSize().pointerInput(valueRange) {
                fun valueAt(position: Offset): Int {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    var degrees = Math.toDegrees(
                        atan2((position.y - center.y).toDouble(), (position.x - center.x).toDouble()),
                    ).toFloat() + 90f
                    if (degrees < 0f) degrees += 360f
                    return (valueRange.first + degrees / 360f * rangeSize).roundToInt()
                        .coerceIn(valueRange.first, valueRange.last)
                }
                detectDragGestures(
                    onDragStart = { position -> onValueChange(valueAt(position)) },
                    onDrag = { change, _ -> onValueChange(valueAt(change.position)) },
                )
            },
        ) {
            val strokeWidth = size.minDimension * 0.095f
            val inset = strokeWidth / 2f + 3f
            val diameter = size.minDimension - inset * 2f
            val stroke = Stroke(strokeWidth, cap = StrokeCap.Round)
            drawArc(
                color = Color(0xFFE1E8F0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(diameter, diameter),
                style = stroke,
            )
            if (progress > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(diameter, diameter),
                    style = stroke,
                )
            }
            val angle = Math.toRadians((progress * 360f - 90f).toDouble())
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val thumb = Offset(
                center.x + cos(angle).toFloat() * radius,
                center.y + sin(angle).toFloat() * radius,
            )
            drawCircle(Color.White, radius = strokeWidth * 0.43f, center = thumb)
            drawCircle(color, radius = strokeWidth * 0.29f, center = thumb)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(safeValue.toString(), color = MiraNavy, fontSize = 29.sp, fontWeight = FontWeight.Black)
            Text(unit, color = Color(0xFF718097), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NavigationStylePreview(style: NavigationStyle) {
    NavigationStyleIcon(style, AppScreen.HOME, Modifier.size(43.dp))
}

@Composable
private fun DictionaryScreen(
    initialQuery: String = "",
    onVoiceSessionChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { DictionaryRepository(context.applicationContext) }
    val store = remember { LocalStore(context.applicationContext) }
    var query by rememberSaveable { mutableStateOf("") }
    var entry by remember { mutableStateOf<DictionaryEntry?>(null) }
    var results by remember { mutableStateOf<List<DictionaryEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showVoiceOverlay by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var voiceTranscript by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var voiceLevel by remember { mutableFloatStateOf(0f) }
    var showMeaningDetails by rememberSaveable { mutableStateOf(false) }
    var dictionaryLimit by remember { mutableIntStateOf(store.loadDictionaryDailyLimit()) }
    var lookupWords by remember { mutableStateOf(store.loadDictionaryLookupWords()) }
    var quotaExceeded by remember { mutableStateOf(false) }
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) query = initialQuery
    }
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening = true; voiceError = null }
            override fun onBeginningOfSpeech() { listening = true }
            override fun onRmsChanged(rmsdB: Float) { voiceLevel = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f) }
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { listening = false }
            override fun onError(error: Int) {
                listening = false
                onVoiceSessionChanged(false)
                voiceError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "没有听清，再说一次吧"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有听到声音，再试一次吧"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "语音服务暂时不可用"
                    else -> "识别没有完成，请再试一次"
                }
            }
            override fun onResults(results: Bundle?) {
                listening = false
                onVoiceSessionChanged(false)
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (spoken.isNotBlank()) {
                    voiceTranscript = spoken
                    query = extractDictionaryQuery(spoken)
                    voiceError = null
                    showVoiceOverlay = false
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                    if (it.isNotBlank()) voiceTranscript = it
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose {
            onVoiceSessionChanged(false)
            speechRecognizer?.destroy()
        }
    }
    fun startDictionaryVoiceSearch() {
        onVoiceSessionChanged(true)
        showVoiceOverlay = true
        voiceTranscript = ""
        voiceError = null
        if (speechRecognizer == null) {
            onVoiceSessionChanged(false)
            voiceError = "设备暂不支持语音识别"
            return
        }
        listening = true
        runCatching {
            speechRecognizer.startListening(dictionarySpeechIntent().putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true))
        }.onFailure {
            listening = false
            onVoiceSessionChanged(false)
            voiceError = "语音识别暂时无法启动"
        }
    }
    val microphonePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            startDictionaryVoiceSearch()
        } else {
            showVoiceOverlay = true
            voiceError = "需要麦克风权限才能语音查字"
        }
    }
    fun requestDictionaryVoiceSearch() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startDictionaryVoiceSearch()
        } else {
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun selectDictionaryEntry(candidate: DictionaryEntry): Boolean {
        dictionaryLimit = store.loadDictionaryDailyLimit()
        if (query.isBlank() || store.recordDictionaryLookup(candidate.word)) {
            lookupWords = store.loadDictionaryLookupWords()
            quotaExceeded = false
            entry = candidate
            return true
        }
        quotaExceeded = true
        entry = null
        Toast.makeText(context, "今天的查字次数已用完", Toast.LENGTH_SHORT).show()
        return false
    }

    LaunchedEffect(query) {
        delay(600)
        loading = true
        val found = withContext(Dispatchers.IO) { repository.search(query) }
        results = found
        val first = found.firstOrNull()
        if (first == null) {
            entry = null
            quotaExceeded = false
        } else {
            selectDictionaryEntry(first)
        }
        loading = false
    }

    val selectedEntry = entry
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(58.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("输入汉字、拼音或词语（如 ba4）", color = Color(0xFF8796AE), fontSize = 16.sp) },
                leadingIcon = {
                    Image(painterResource(R.drawable.mira_dict_search), "搜索", Modifier.size(28.dp))
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        Surface(onClick = { query = "" }, color = Color.Transparent, shape = CircleShape) {
                            Image(painterResource(R.drawable.mira_dict_clear), "清空", Modifier.padding(3.dp).size(27.dp))
                        }
                    }
                },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(23.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF92D3FF),
                    unfocusedBorderColor = Color(0xFFC5E6FA),
                ),
                textStyle = MaterialTheme.typography.titleMedium.copy(color = MiraNavy),
                singleLine = true,
            )
            Surface(
                color = if (lookupWords.size >= dictionaryLimit) Color(0xFFFFECE8) else Color(0xFFE9F8EF),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(
                    "今日 ${lookupWords.size}/$dictionaryLimit",
                    Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    color = if (lookupWords.size >= dictionaryLimit) Color(0xFFC4553E) else Color(0xFF168458),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
            Surface(
                onClick = { requestDictionaryVoiceSearch() },
                enabled = !listening,
                modifier = Modifier.width(168.dp).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFF258DF3),
                shadowElevation = 5.dp,
            ) {
                Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Image(painterResource(R.drawable.mira_dict_voice), null, Modifier.size(33.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (listening) "正在听…" else "语音查字", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        }
        if (!loading && results.size > 1) {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().height(62.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                results.forEach { result ->
                    val chosen = selectedEntry?.word == result.word
                    Surface(
                        onClick = { selectDictionaryEntry(result) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (chosen) Color(0xFFD9F2FF) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (chosen) Color(0xFF4AA8F5) else Color(0xFFDDE8F2)),
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(result.word, fontFamily = miraKaiFont, fontSize = 28.sp, color = MiraNavy)
                            Spacer(Modifier.width(7.dp))
                            Text(candidatePinyinLabel(result.pinyin, query), color = Color(0xFF5F7593), fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            Spacer(Modifier.height(10.dp))
        }
        if (!loading && quotaExceeded) {
            MiraCard(Modifier.fillMaxSize()) {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("今天已经查询了 $dictionaryLimit 个生字", color = MiraNavy, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(10.dp))
                    Text("明天会自动恢复到 20 次；家长也可以在设置中修改今天的次数。", color = Color(0xFF6A7A91), fontSize = 16.sp)
                }
            }
        } else if (!loading && selectedEntry == null) {
            MiraCard(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("内置字典暂未找到“$query”", color = Color(0xFF6A7A91), fontSize = 20.sp)
                }
            }
        } else if (selectedEntry != null) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MiraCard(Modifier.weight(1.9f).fillMaxHeight()) {
                    CharacterPanel(selectedEntry)
                }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    MiraCard(Modifier.fillMaxWidth().weight(1.14f), onClick = { showMeaningDetails = true }) {
                        MeaningPanel(selectedEntry)
                    }
                    MiraCard(Modifier.fillMaxWidth().weight(0.86f)) {
                        WordsPanel(selectedEntry, onSelect = { query = it })
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("正在打开离线字典…") }
        }
    }
        if (showVoiceOverlay) {
            Box(Modifier.fillMaxSize().background(Color(0xFFF7FBFF).copy(alpha = 0.64f)))
            HomeVoiceOverlay(
                listening = listening,
                transcript = voiceTranscript,
                error = voiceError,
                level = voiceLevel,
                onRetry = { startDictionaryVoiceSearch() },
                onCancel = {
                    speechRecognizer?.cancel()
                    onVoiceSessionChanged(false)
                    showVoiceOverlay = false
                    listening = false
                },
                onComplete = {
                    onVoiceSessionChanged(false)
                    if (voiceTranscript.isNotBlank()) query = extractDictionaryQuery(voiceTranscript)
                    showVoiceOverlay = false
                },
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
    if (showMeaningDetails && selectedEntry != null) {
        MeaningDetailOverlay(selectedEntry, onDismiss = { showMeaningDetails = false })
    }
}

private fun dictionarySpeechIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
    putExtra(RecognizerIntent.EXTRA_PROMPT, "请说：智能的智怎么写")
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
}

internal fun extractDictionaryQuery(spoken: String): String {
    val text = spoken.replace(Regex("[，。！？、,.!?\\s]"), "")
    val character = "[\\u3400-\\u9FFF]"
    val patterns = listOf(
        Regex("的($character)(?:字)?(?:怎么写|如何写|怎么读|是什么意思|什么含义)?"),
        Regex("($character)(?:字)?(?:怎么写|如何写|怎么读|是什么意思|什么含义)"),
        Regex("(?:查|查询|搜索|找)(?:一下)?($character)"),
    )
    patterns.forEach { pattern -> pattern.find(text)?.groupValues?.getOrNull(1)?.let { return it } }
    return text
        .replace(Regex("^(请帮我|帮我|请|查一下|查询|搜索|查)"), "")
        .replace(Regex("(怎么写|如何写|怎么读|是什么意思|什么含义)$"), "")
        .take(12)
        .ifBlank { spoken.trim() }
}

private fun candidatePinyinLabel(pinyin: String, query: String): String {
    val numbered = Regex("^([a-zA-ZüÜvV]+)([1-5])$").matchEntire(query.trim())
        ?: return pinyin.substringBefore(" / ")
    val expected = normalizePinyinLabel(numbered.groupValues[1])
    val tone = numbered.groupValues[2].toInt()
    return pinyin.split(Regex("[\\s/,;·・]+"))
        .firstOrNull { normalizePinyinLabel(it) == expected && pinyinToneLabel(it) == tone }
        ?: pinyin.substringBefore(" / ")
}

private fun normalizePinyinLabel(value: String): String = Normalizer
    .normalize(value.replace('ɡ', 'g').lowercase(), Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "")
    .replace('ü', 'v')

private fun pinyinToneLabel(value: String): Int = when {
    value.any { it in "āēīōūǖ" } -> 1
    value.any { it in "áéíóúǘ" } -> 2
    value.any { it in "ǎěǐǒǔǚ" } -> 3
    value.any { it in "àèìòùǜ" } -> 4
    else -> 5
}

@Composable
private fun CharacterPanel(entry: DictionaryEntry) {
    val speak = rememberSpeaker()
    var replaySignal by remember(entry.word) { mutableIntStateOf(0) }
    Row(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(Modifier.width(306.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFFBF1),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD9C8A9)),
                modifier = Modifier.size(306.dp),
            ) {
                StrokeOrderView(entry.word, entry.strokePaths, replaySignal)
            }
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(
                onClick = { replaySignal++ },
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE4F5FF)),
            ) {
                Image(painterResource(R.drawable.mira_dict_replay), null, Modifier.size(25.dp))
                Spacer(Modifier.width(7.dp))
                Text("重播笔顺", color = Color(0xFF174477), fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.pinyin.substringBefore(" / "), color = Color(0xFF082B63), fontSize = 43.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(12.dp))
                Surface(onClick = { speak(entry.word) }, color = Color.Transparent, shape = CircleShape) {
                    Image(painterResource(R.drawable.mira_dict_speaker), "朗读", Modifier.padding(3.dp).size(49.dp))
                }
            }
            Text(
                "部首 ${entry.radical}  ·  ${entry.strokes}画  ·  ${entry.structure}",
                color = Color(0xFF183B70),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            )
            Spacer(Modifier.height(12.dp))
            StrokeSequencePanel(entry, Modifier.fillMaxWidth().weight(1f))
        }
    }
}

@Composable
private fun StrokeSequencePanel(entry: DictionaryEntry, modifier: Modifier = Modifier) {
    val parsedPaths = remember(entry.strokePaths) {
        entry.strokePaths.mapNotNull { data -> runCatching { PathParser().parsePathString(data).toPath() }.getOrNull() }
    }
    Surface(modifier = modifier, color = Color(0xFFF3F9FD), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxSize().padding(14.dp)) {
            Text("笔顺", color = Color(0xFF163D74), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (parsedPaths.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("暂无笔顺数据", color = Color(0xFF8493A7))
                }
            } else {
                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                    val gap = 7.dp
                    val columns = if (maxWidth >= 330.dp) 6 else 4
                    val cellSize = ((maxWidth - gap * (columns - 1)) / columns).coerceAtMost(54.dp)
                    val rows = parsedPaths.indices.chunked(columns)
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        rows.forEachIndexed { rowIndex, rowSteps ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                                rowSteps.forEach { step ->
                                    StrokeStepCell(parsedPaths, step, Modifier.size(cellSize))
                                }
                                repeat(columns - rowSteps.size) { Spacer(Modifier.size(cellSize)) }
                            }
                            if (rowIndex < rows.lastIndex) Spacer(Modifier.height(gap))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrokeStepCell(paths: List<Path>, lastVisibleIndex: Int, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(7.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD5E1EB))) {
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            val scale = size.minDimension / 1024f
            withTransform({
                translate(left = (size.width - 1024f * scale) / 2f, top = 900f * scale)
                scale(scaleX = scale, scaleY = -scale, pivot = Offset.Zero)
            }) {
                paths.take(lastVisibleIndex + 1).forEachIndexed { index, path ->
                    drawPath(path, if (index == lastVisibleIndex) Color(0xFFE84B43) else Color(0xFF17243A))
                }
            }
        }
    }
}

@Composable
private fun StrokeOrderView(character: String, strokePaths: List<String>, replaySignal: Int) {
    val parsedPaths = remember(strokePaths) {
        strokePaths.mapNotNull { data -> runCatching { PathParser().parsePathString(data).toPath() }.getOrNull() }
    }
    var visibleStrokes by remember(character, parsedPaths) { mutableIntStateOf(0) }
    var playing by remember(character) { mutableStateOf(false) }

    LaunchedEffect(character, parsedPaths, replaySignal) {
        if (parsedPaths.isNotEmpty()) {
            playing = true
            visibleStrokes = 0
            delay(180)
            parsedPaths.indices.forEach { index ->
                delay(320)
                visibleStrokes = index + 1
            }
            playing = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize().padding(14.dp)) {
            val gridColor = Color(0xFFE8DCC6)
            drawLine(gridColor, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.5f)
            drawLine(gridColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.5f)
            drawLine(gridColor, Offset(0f, 0f), Offset(size.width, size.height), 1f)
            drawLine(gridColor, Offset(size.width, 0f), Offset(0f, size.height), 1f)
            if (parsedPaths.isEmpty()) {
                return@Canvas
            }
            val scale = size.minDimension / 1024f
            withTransform({
                translate(top = 900f * scale)
                scale(scaleX = scale, scaleY = -scale, pivot = Offset.Zero)
            }) {
                parsedPaths.forEachIndexed { index, path ->
                    val color = when {
                        index < visibleStrokes - 1 -> MiraNavy
                        index == visibleStrokes - 1 && playing -> Color(0xFFE85B52)
                        index < visibleStrokes -> MiraNavy
                        else -> Color(0xFFDDE2E8)
                    }
                    drawPath(path, color)
                }
            }
        }
        if (parsedPaths.isEmpty()) {
            Text(character, fontSize = 142.sp, fontFamily = miraKaiFont, color = MiraNavy, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun rememberSpeaker(): (String) -> Unit {
    val context = LocalContext.current
    val engineState = remember { mutableStateOf<TextToSpeech?>(null) }
    val readyState = remember { mutableStateOf(false) }
    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageStatus = engineState.value?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                readyState.value = languageStatus != TextToSpeech.LANG_MISSING_DATA &&
                    languageStatus != TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                readyState.value = false
            }
        }
        engineState.value = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            engineState.value = null
            readyState.value = false
        }
    }
    return remember(context) {
        { text: String ->
            val engine = engineState.value
            if (!readyState.value || engine == null) {
                Toast.makeText(context, "朗读引擎正在准备，请稍后再试", Toast.LENGTH_SHORT).show()
            } else if (engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mira-dictionary") == TextToSpeech.ERROR) {
                Toast.makeText(context, "系统朗读暂不可用", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun MeaningPanel(entry: DictionaryEntry) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.mira_dict_meaning), null, Modifier.size(47.dp))
            Spacer(Modifier.width(9.dp))
            Text("基本释义", color = Color(0xFF0B326A), fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            entry.meanings.joinToString(separator = "\n") { it.trim() },
            color = Color(0xFF28394F),
            fontSize = 17.sp,
            lineHeight = 25.sp,
            maxLines = 8,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "点击卡片查看全部释义",
            color = Color(0xFF6EA5C9),
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.align(Alignment.End).padding(bottom = 3.dp),
        )
    }
}

@Composable
private fun MeaningDetailOverlay(entry: DictionaryEntry, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.width(660.dp).height(360.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFFFFEFC),
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8EAF6)),
        ) {
            Column(Modifier.fillMaxSize().padding(26.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.mira_dict_meaning), null, Modifier.size(45.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(entry.word, color = Color(0xFF102650), fontFamily = miraKaiFont, fontSize = 52.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(entry.pinyin, color = Color(0xFF337FC7), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(18.dp))
                    Text("全部释义", color = MiraNavy, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    Surface(onClick = onDismiss, color = Color(0xFFE7F5FF), shape = CircleShape) {
                        Text("×", Modifier.padding(horizontal = 14.dp, vertical = 6.dp), color = Color(0xFF4F6F91), fontSize = 25.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFE3EDF4))
                Spacer(Modifier.height(14.dp))
                Text(
                    entry.meanings.joinToString(separator = "\n") { it.trim() },
                    color = Color(0xFF24364D),
                    fontSize = 20.sp,
                    lineHeight = 32.sp,
                    modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun WordsPanel(entry: DictionaryEntry, onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.mira_dict_words), null, Modifier.size(47.dp))
            Spacer(Modifier.width(9.dp))
            Text("组词", color = Color(0xFF0B326A), fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(13.dp))
        entry.phrases.take(6).chunked(3).forEachIndexed { index, phrases ->
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                phrases.forEach { phrase ->
                    Surface(onClick = { onSelect(phrase.take(1)) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp), color = Color(0xFFDFF3FF)) {
                        Box(Modifier.fillMaxSize().padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                            Text(phrase, color = Color(0xFF173B6D), fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                }
                repeat(3 - phrases.size) { Spacer(Modifier.weight(1f)) }
            }
            if (index == 0) Spacer(Modifier.height(9.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreen() {
    val context = LocalContext.current
    val store = remember { LocalStore(context) }
    val lessons = remember { mutableStateListOf<Lesson>().also { it.addAll(store.loadLessons()) } }
    var selectedDay by rememberSaveable { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var addPeriod by remember { mutableIntStateOf(1) }
    var editingLesson by remember { mutableStateOf<Lesson?>(null) }
    var deletingLesson by remember { mutableStateOf<Lesson?>(null) }
    var showBulkEditor by remember { mutableStateOf(false) }
    var hasPendingChanges by rememberSaveable { mutableStateOf(false) }
    val weekdays = listOf("周一", "周二", "周三", "周四", "周五")
    val dayLessons = lessons.filter { it.day == selectedDay }.sortedBy { it.period }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            weekdays.forEachIndexed { index, day ->
                val selected = selectedDay == index
                Surface(
                    onClick = { selectedDay = index },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    color = if (selected) Color(0xFFFFCC45) else Color(0xFFF4F9FE),
                    shape = RoundedCornerShape(17.dp),
                    shadowElevation = if (selected) 4.dp else 1.dp,
                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(day, color = Color(0xFF173B70), fontSize = 19.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            Surface(
                onClick = { showBulkEditor = true },
                modifier = Modifier.width(180.dp).fillMaxHeight(),
                color = Color(0xFF4DBA6C),
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 4.dp,
            ) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("▦", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(7.dp))
                    Text("批量排课", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                }
            }
            Surface(
                onClick = {
                    store.saveLessons(lessons)
                    hasPendingChanges = false
                    Toast.makeText(context, "课表已保存", Toast.LENGTH_SHORT).show()
                },
                enabled = hasPendingChanges,
                modifier = Modifier.width(180.dp).fillMaxHeight(),
                color = if (hasPendingChanges) Color(0xFF2D86ED) else Color(0xFFD9E2EC),
                shape = RoundedCornerShape(18.dp),
                shadowElevation = if (hasPendingChanges) 4.dp else 0.dp,
            ) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(23.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(if (hasPendingChanges) "保存课表" else "已保存", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MiraCard(Modifier.weight(1.85f).fillMaxHeight()) {
                Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFFFFF3CE), shape = RoundedCornerShape(12.dp)) {
                            Text("${weekdays[selectedDay]}课程", Modifier.padding(horizontal = 18.dp, vertical = 8.dp), color = MiraNavy, fontSize = 23.sp, fontWeight = FontWeight.Black)
                        }
                        Text("  ！", color = Color(0xFFFF745B), fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Text("加油！新的一天从这里开始！ ☺", color = Color(0xFF6583A8), fontFamily = miraHandFont, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    (1..7).forEach { period ->
                        val lesson = dayLessons.firstOrNull { it.period == period }
                        ScheduleLessonRow(
                            period = period,
                            lesson = lesson,
                            onEdit = {
                                if (lesson != null) editingLesson = lesson
                                else {
                                    addPeriod = period
                                    showAdd = true
                                }
                            },
                        )
                    }
                }
            }
            Column(Modifier.weight(0.85f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MiraCard(Modifier.fillMaxWidth().weight(1f)) {
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF8D9), Color(0xFFFFFEF7))))) {
                        Column(Modifier.fillMaxSize().padding(20.dp)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("今天", color = MiraNavy, fontSize = 27.sp, fontWeight = FontWeight.Black)
                                Text(" ${dayLessons.size} ", color = Color(0xFFFF861F), fontSize = 52.sp, fontWeight = FontWeight.Black)
                                Text("节课", color = MiraNavy, fontSize = 27.sp, fontWeight = FontWeight.Black)
                            }
                            Text("好好上课\n收获更棒的自己！", color = Color(0xFF6583A8), fontFamily = miraHandFont, fontSize = 16.sp, lineHeight = 22.sp)
                            Spacer(Modifier.weight(1f))
                        }
                        Image(
                            painterResource(R.drawable.mira_schedule_backpack),
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth(0.82f).height(190.dp).padding(end = 6.dp, bottom = 6.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Text("★", modifier = Modifier.align(Alignment.BottomStart).padding(start = 22.dp, bottom = 18.dp), color = Color(0xFFFFBF27), fontSize = 34.sp)
                    }
                }
                MiraCard(Modifier.fillMaxWidth().weight(0.78f)) {
                    Box(Modifier.fillMaxSize().background(Color(0xFFFFFEFB))) {
                        Canvas(Modifier.fillMaxSize()) {
                            var y = 52.dp.toPx()
                            while (y < size.height) {
                                drawLine(Color(0xFFE7EEF4), Offset(0f, y), Offset(size.width, y), 1f)
                                y += 34.dp.toPx()
                            }
                        }
                        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 17.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", fontSize = 23.sp)
                                Spacer(Modifier.width(7.dp))
                                Text("小提醒", color = MiraNavy, fontSize = 19.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.weight(1f))
                                Text("★", color = Color(0xFFFFBE26), fontSize = 29.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            listOf("带好课本和文具", "上课认真听讲", "做一个棒棒的自己！").forEach {
                                Text("✅  $it", Modifier.padding(vertical = 6.dp), color = Color(0xFF29476E), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(Modifier.weight(1f))
                            Text("加油！ ♡", color = Color(0xFF6B89AC), fontFamily = miraHandFont, fontSize = 20.sp, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        LessonDialog(day = selectedDay, initial = null, initialPeriod = addPeriod, onDismiss = { showAdd = false }, onSave = { lesson ->
            val conflict = lessons.any { it.day == lesson.day && it.period == lesson.period }
            if (conflict) Toast.makeText(context, "第 ${lesson.period} 节已经有课程了", Toast.LENGTH_SHORT).show()
            else {
                lessons.add(lesson)
                hasPendingChanges = true
                showAdd = false
            }
        })
    }
    if (showBulkEditor) {
        BulkScheduleEditor(
            initialLessons = lessons,
            onDismiss = { showBulkEditor = false },
            onSave = { updated ->
                lessons.clear()
                lessons.addAll(updated.sortedWith(compareBy<Lesson> { it.day }.thenBy { it.period }))
                store.saveLessons(lessons)
                hasPendingChanges = false
                showBulkEditor = false
                Toast.makeText(context, "整周课表已保存", Toast.LENGTH_SHORT).show()
            },
        )
    }
    editingLesson?.let { original ->
        LessonDialog(day = original.day, initial = original, initialPeriod = original.period, onDismiss = { editingLesson = null }, onDelete = {
            editingLesson = null
            deletingLesson = original
        }, onSave = { updated ->
            val conflict = lessons.any { it != original && it.day == updated.day && it.period == updated.period }
            if (conflict) Toast.makeText(context, "第 ${updated.period} 节已经有课程了", Toast.LENGTH_SHORT).show()
            else {
                val index = lessons.indexOf(original)
                if (index >= 0) lessons[index] = updated
                hasPendingChanges = true
                editingLesson = null
            }
        })
    }
    deletingLesson?.let { lesson ->
        AlertDialog(
            onDismissRequest = { deletingLesson = null },
            title = { Text("删除课程") },
            text = { Text("确定删除“第 ${lesson.period} 节 ${lesson.name}”吗？") },
            confirmButton = {
                TextButton(onClick = {
                    lessons.remove(lesson)
                    hasPendingChanges = true
                    deletingLesson = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingLesson = null }) { Text("取消") } },
        )
    }
}

@Composable
private fun BulkScheduleEditor(
    initialLessons: List<Lesson>,
    onDismiss: () -> Unit,
    onSave: (List<Lesson>) -> Unit,
) {
    val weekdays = listOf("周一", "周二", "周三", "周四", "周五")
    val commonSubjects = listOf(
        "语文", "数学", "英语", "科学", "体育", "美术", "音乐",
        "信息技术", "道法", "劳动", "班会", "社团", "选修", "竖笛",
    )
    val subjects = remember(initialLessons) {
        (commonSubjects + initialLessons.map { it.name }).map(String::trim).filter(String::isNotBlank).distinct()
    }
    val draft = remember(initialLessons) {
        mutableStateListOf<Lesson>().also { it.addAll(initialLessons.filter { lesson -> lesson.day in 0..4 }) }
    }
    var selectedSubject by rememberSaveable { mutableStateOf(subjects.first()) }
    var eraseMode by rememberSaveable { mutableStateOf(false) }

    fun paintSlot(day: Int, period: Int) {
        val existingIndex = draft.indexOfFirst { it.day == day && it.period == period }
        if (eraseMode) {
            if (existingIndex >= 0) draft.removeAt(existingIndex)
        } else {
            val lesson = Lesson(day, selectedSubject, period)
            when {
                existingIndex < 0 -> draft.add(lesson)
                draft[existingIndex].name != selectedSubject -> draft[existingIndex] = lesson
            }
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.91f),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFFFFEFC),
            shadowElevation = 16.dp,
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("批量排课", color = MiraNavy, fontSize = 26.sp, fontWeight = FontWeight.Black)
                        Text("选择科目后，点击格子或按住拖过多个格子即可连续填写。", color = Color(0xFF657A96), fontSize = 13.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("已安排 ${draft.size} 节", color = MiraGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = { eraseMode = true },
                        color = if (eraseMode) Color(0xFFE76C61) else Color(0xFFFFECE9),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE76C61)),
                    ) {
                        Text(
                            "橡皮擦",
                            Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            color = if (eraseMode) Color.White else Color(0xFFC94F45),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    subjects.forEach { subject ->
                        val selected = !eraseMode && selectedSubject == subject
                        val accent = subjectAccent(subject)
                        Surface(
                            onClick = {
                                selectedSubject = subject
                                eraseMode = false
                            },
                            color = if (selected) accent else accent.copy(alpha = 0.11f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, accent.copy(alpha = if (selected) 1f else 0.42f)),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(painterResource(scheduleLessonIcon(subject)), null, Modifier.size(25.dp), contentScale = ContentScale.Fit)
                                Spacer(Modifier.width(6.dp))
                                Text(subject, color = if (selected) Color.White else accent, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.fillMaxWidth().weight(1f)
                        .pointerInput(selectedSubject, eraseMode) {
                            fun paintAt(position: Offset) {
                                val columnWidth = size.width / 6f
                                val rowHeight = size.height / 8f
                                val day = (position.x / columnWidth).toInt() - 1
                                val period = (position.y / rowHeight).toInt()
                                if (day in 0..4 && period in 1..7) paintSlot(day, period)
                            }
                            detectDragGestures(
                                onDragStart = ::paintAt,
                                onDrag = { change, _ ->
                                    change.consume()
                                    paintAt(change.position)
                                },
                            )
                        },
                ) {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BulkScheduleCell("节次", null, true, Modifier.weight(1f).fillMaxHeight())
                            weekdays.forEach { day ->
                                BulkScheduleCell(day, null, true, Modifier.weight(1f).fillMaxHeight())
                            }
                        }
                        (1..7).forEach { period ->
                            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BulkScheduleCell("第${period}节", null, true, Modifier.weight(1f).fillMaxHeight())
                                weekdays.indices.forEach { day ->
                                    val lesson = draft.firstOrNull { it.day == day && it.period == period }
                                    BulkScheduleCell(
                                        label = lesson?.name ?: "＋",
                                        lesson = lesson,
                                        header = false,
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                        onClick = { paintSlot(day, period) },
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { draft.clear() }) {
                        Text("清空草稿", color = MaterialTheme.colorScheme.error)
                    }
                    Text("只有点击“保存整周课表”后才会写入。", color = Color(0xFF718097), fontSize = 12.sp)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(draft.toList()) },
                        colors = ButtonDefaults.buttonColors(containerColor = MiraBlue),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Icon(Icons.Rounded.Check, null)
                        Spacer(Modifier.width(6.dp))
                        Text("保存整周课表", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkScheduleCell(
    label: String,
    lesson: Lesson?,
    header: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val accent = lesson?.let { subjectAccent(it.name) } ?: MiraBlue
    val content: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize().padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
            if (lesson != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Image(painterResource(scheduleLessonIcon(lesson.name)), null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
                    Spacer(Modifier.width(5.dp))
                    Text(label, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1)
                }
            } else {
                Text(
                    label,
                    color = if (header) MiraNavy else Color(0xFF9AA7B8),
                    fontSize = if (header) 14.sp else 18.sp,
                    fontWeight = if (header) FontWeight.Black else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
    val color = when {
        header -> Color(0xFFEAF2FC)
        lesson != null -> accent.copy(alpha = 0.12f)
        else -> Color(0xFFF5F7FA)
    }
    if (onClick == null) {
        Surface(modifier = modifier, color = color, shape = RoundedCornerShape(12.dp), content = content)
    } else {
        Surface(onClick = onClick, modifier = modifier, color = color, shape = RoundedCornerShape(12.dp), content = content)
    }
}

@Composable
private fun ScheduleLessonRow(period: Int, lesson: Lesson?, onEdit: () -> Unit) {
    val colors = listOf(Color(0xFFFFE2E4), Color(0xFFDDEEFF), Color(0xFFE2F5DD), Color(0xFFFFF0C4), Color(0xFFEDE4FF), Color(0xFFDDF6F1), Color(0xFFFFE8D6))
    val baseColor = colors[(period - 1).mod(colors.size)]
    Surface(
        modifier = Modifier.fillMaxWidth().height(58.dp).padding(vertical = 2.dp),
        color = if (lesson != null) baseColor else Color(0xFFF3F6F9),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = if (lesson != null) baseColor.copy(alpha = 0.78f) else Color(0xFFE8EDF2), modifier = Modifier.width(112.dp).fillMaxHeight()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("第${period}节", fontWeight = FontWeight.Black, color = if (lesson != null) MiraNavy else Color(0xFF8997A9), fontSize = 16.sp)
                }
            }
            Spacer(Modifier.width(18.dp))
            if (lesson != null) {
                Image(painterResource(scheduleLessonIcon(lesson.name)), null, Modifier.size(42.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(18.dp))
                Text(lesson.name, color = MiraNavy, fontSize = 21.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            } else {
                Text("暂无课程", color = Color(0xFF95A2B2), fontSize = 17.sp, modifier = Modifier.weight(1f))
            }
            Surface(onClick = onEdit, color = Color.White.copy(alpha = 0.76f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
                Image(painterResource(R.drawable.mira_schedule_edit), if (lesson != null) "编辑课程" else "添加课程", Modifier.padding(7.dp), contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.width(16.dp))
        }
    }
}

private fun scheduleLessonIcon(name: String): Int = when {
    name.contains("语文") -> R.drawable.mira_schedule_chinese
    name.contains("数学") -> R.drawable.mira_schedule_math
    name.contains("英语") -> R.drawable.mira_schedule_english
    name.contains("体育") -> R.drawable.mira_schedule_sports
    name.contains("美术") -> R.drawable.mira_schedule_art
    name.contains("音乐") -> R.drawable.mira_schedule_music
    name.contains("社团") || name.contains("班会") -> R.drawable.mira_schedule_club
    name.contains("选修") -> R.drawable.mira_schedule_elective
    name.contains("劳动") -> R.drawable.mira_schedule_labor
    name.contains("竖笛") -> R.drawable.mira_schedule_recorder
    name.contains("科学") -> R.drawable.mira_schedule_science
    name.contains("信息") || name.contains("电脑") || name.contains("计算机") -> R.drawable.mira_schedule_computer
    else -> R.drawable.mira_schedule_talent
}

@Composable
private fun LessonDialog(
    day: Int,
    initial: Lesson?,
    initialPeriod: Int,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (Lesson) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var period by remember(initial, initialPeriod) { mutableIntStateOf(initial?.period ?: initialPeriod) }
    val canSave = name.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "添加课程" else "编辑课程") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("课程名称") }, singleLine = true)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("语文", "数学", "英语", "体育", "美术", "音乐", "社团", "选修", "劳动", "竖笛").forEach { subject ->
                        Surface(onClick = { name = subject }, shape = RoundedCornerShape(14.dp), color = if (name == subject) Color(0xFFD9EEFF) else Color(0xFFF1F5F9)) {
                            Text(subject, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = MiraNavy, fontSize = 13.sp)
                        }
                    }
                }
                Text("选择节次", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..12).forEach { number ->
                        FilterChip(selected = period == number, onClick = { period = number }, label = { Text("第${number}节") })
                    }
                }
                Text("课程只按节次排列，不需要填写具体时间。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(enabled = canSave, onClick = { onSave(Lesson(day, name.trim(), period)) }) { Text("应用") } },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text("删除课程", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        },
    )
}

@Composable
private fun WeatherScreen() {
    val context = LocalContext.current
    val store = remember { LocalStore(context.applicationContext) }
    val manualCities = remember { mutableStateListOf<WeatherCity>().apply { addAll(store.loadManualWeatherCities()) } }
    var selectedManualCityName by rememberSaveable { mutableStateOf<String?>(null) }
    var locatedCityName by rememberSaveable { mutableStateOf(store.loadWeatherCache()?.weather?.placeName) }
    var refresh by remember { mutableIntStateOf(0) }
    var permissionEpoch by remember { mutableIntStateOf(0) }
    var report by remember { mutableStateOf<WeatherReport?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddCity by remember { mutableStateOf(false) }
    var cityInput by remember { mutableStateOf("") }
    var pendingCityName by remember { mutableStateOf<String?>(null) }
    var addingCity by remember { mutableStateOf(false) }
    var deletingCity by remember { mutableStateOf<WeatherCity?>(null) }
    val hasLocationPermission = permissionEpoch.let {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissionEpoch++
    }
    val selectedManualCity = manualCities.firstOrNull { it.name == selectedManualCityName }
    val displayCityName = selectedManualCity?.name ?: locatedCityName ?: "当前位置"

    LaunchedEffect(selectedManualCityName, refresh, permissionEpoch, manualCities.size) {
        loading = true
        error = null
        if (selectedManualCity == null && !hasLocationPermission) {
            loading = false
            error = "需要定位权限才能获取所在城市"
            return@LaunchedEffect
        }
        runCatching {
            withContext(Dispatchers.IO) {
                val city = selectedManualCity ?: DeviceWeatherRepository.locate(context)
                city to WeatherRepository.loadReport(city)
            }
        }.onSuccess { (city, weatherReport) ->
            if (selectedManualCity == null) {
                locatedCityName = city.name
                store.saveWeatherCache(WeatherCache(LocatedWeather(city.name, weatherReport.current), System.currentTimeMillis()))
            }
            report = weatherReport
        }
            .onFailure { error = it.message ?: "无法连接天气服务" }
        loading = false
    }

    LaunchedEffect(pendingCityName) {
        val requestedName = pendingCityName ?: return@LaunchedEffect
        addingCity = true
        runCatching {
            withContext(Dispatchers.IO) { DeviceWeatherRepository.geocodeCity(context, requestedName) }
        }.onSuccess { city ->
            if (city.name != locatedCityName && manualCities.none { it.name == city.name }) {
                manualCities += city
                store.saveManualWeatherCities(manualCities)
            }
            selectedManualCityName = if (city.name == locatedCityName) null else city.name
            showAddCity = false
            cityInput = ""
        }.onFailure {
            Toast.makeText(context, it.message ?: "添加城市失败", Toast.LENGTH_SHORT).show()
        }
        addingCity = false
        pendingCityName = null
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WeatherCityChip(
                    name = locatedCityName ?: "定位城市",
                    selected = selectedManualCityName == null,
                    location = true,
                    onClick = { selectedManualCityName = null },
                )
                manualCities.forEach { city ->
                    WeatherCityChip(
                        name = city.name,
                        selected = selectedManualCityName == city.name,
                        onClick = { selectedManualCityName = city.name },
                        onLongClick = { deletingCity = city },
                    )
                }
                Surface(
                    onClick = { showAddCity = true },
                    color = Color(0xFFE9F5FF),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB9DFFF)),
                ) {
                    Text("＋ 添加城市", Modifier.padding(horizontal = 17.dp, vertical = 10.dp), color = Color(0xFF2379D7), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Text("长按城市可删除", color = Color(0xFF6A7E98), fontSize = 11.sp)
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { refresh++ }, shape = RoundedCornerShape(22.dp)) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(20.dp))
                Spacer(Modifier.width(5.dp))
                Text("刷新")
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().height(268.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MiraCard(Modifier.weight(1.32f).fillMaxHeight()) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painterResource(R.drawable.mira_weather_qingdao_v1),
                        contentDescription = "青岛城市插画",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        Modifier.fillMaxHeight().width(250.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xCCDEF3FF), Color.Transparent))),
                    )
                    Column(Modifier.align(Alignment.TopStart).padding(start = 26.dp, top = 16.dp)) {
                        Text(displayCityName, color = MiraNavy, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        when {
                            loading -> Text("正在获取天气…", color = Color(0xFF466A91), fontSize = 18.sp)
                            error != null -> {
                                Text("暂时无法获取", color = MiraNavy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                                Text(error.orEmpty(), color = Color(0xFFB14242), fontSize = 13.sp, modifier = Modifier.widthIn(max = 210.dp))
                            }
                            report != null -> {
                                Text("${report!!.current.temperature.toInt()}℃", color = Color(0xFF082B63), fontSize = 54.sp, fontWeight = FontWeight.Black)
                                Text(report!!.current.description, color = MiraNavy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    report?.current?.let { current ->
                        Image(
                            painterResource(weatherIconResource(current.weatherCode)),
                            contentDescription = current.description,
                            modifier = Modifier.align(Alignment.TopEnd).padding(14.dp).size(96.dp),
                        )
                    }
                    Text(
                        "海风轻轻吹来，\n今天也要闪闪发光！",
                        modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp),
                        color = Color(0xFF315F8C),
                        fontFamily = miraHandFont,
                        fontSize = 16.sp,
                        textAlign = TextAlign.End,
                    )
                }
            }
            Column(Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WeatherMetricCard(
                    R.drawable.mira_weather_fog,
                    "相对湿度",
                    report?.current?.let { "${it.humidity}%" } ?: "--",
                    Color(0xFFE4F4FF),
                    Modifier.weight(1f),
                )
                WeatherMetricCard(
                    R.drawable.mira_weather_cloudy,
                    "风速",
                    report?.current?.let { "${it.windSpeed.toInt()} km/h" } ?: "--",
                    Color(0xFFECEBFF),
                    Modifier.weight(1f),
                )
                WeatherMetricCard(
                    R.drawable.mira_weather_partly_cloudy,
                    "体感温度",
                    report?.current?.let { "${it.apparentTemperature.toInt()}℃" } ?: "--",
                    Color(0xFFFFF0D9),
                    Modifier.weight(1f),
                )
                WeatherMetricCard(
                    R.drawable.mira_weather_rain,
                    "当前降水",
                    report?.current?.let { String.format(Locale.US, "%.1f mm", it.precipitation) } ?: "--",
                    Color(0xFFE5F8EF),
                    Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        MiraCard(Modifier.fillMaxWidth().weight(1f)) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("未来五天天气预报", color = MiraNavy, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    Text("一起期待更多好天气！ ★", color = Color(0xFF58779B), fontFamily = miraHandFont, fontSize = 14.sp)
                }
                Spacer(Modifier.height(5.dp))
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val forecasts = report?.daily.orEmpty()
                    if (forecasts.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (loading) "正在加载未来天气…" else "暂无预报数据", color = Color(0xFF6A7E98))
                        }
                    } else {
                        forecasts.forEach { daily -> WeatherForecastCard(daily, Modifier.weight(1f).fillMaxHeight()) }
                    }
                }
            }
        }
    }

    if (showAddCity) {
        AlertDialog(
            onDismissRequest = { if (!addingCity) showAddCity = false },
            title = { Text("添加城市") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("输入城市名称，例如：北京、杭州、成都。")
                    OutlinedTextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        label = { Text("城市名称") },
                        singleLine = true,
                        enabled = !addingCity,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = cityInput.isNotBlank() && !addingCity,
                    onClick = { pendingCityName = cityInput.trim() },
                ) { Text(if (addingCity) "正在查找…" else "添加") }
            },
            dismissButton = { TextButton(enabled = !addingCity, onClick = { showAddCity = false }) { Text("取消") } },
        )
    }

    deletingCity?.let { city ->
        AlertDialog(
            onDismissRequest = { deletingCity = null },
            title = { Text("删除城市") },
            text = { Text("确定从天气列表中删除“${city.name}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        manualCities.remove(city)
                        store.saveManualWeatherCities(manualCities)
                        if (selectedManualCityName == city.name) selectedManualCityName = null
                        deletingCity = null
                    },
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingCity = null }) { Text("取消") } },
        )
    }

    if (!hasLocationPermission && selectedManualCityName == null && !showAddCity) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("开启定位天气") },
            text = { Text("天气页默认只保留设备所在城市，需要定位权限才能自动识别。") },
            confirmButton = {
                TextButton(onClick = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                }) { Text("开启定位") }
            },
            dismissButton = { TextButton(onClick = { showAddCity = true }) { Text("手动添加城市") } },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeatherCityChip(
    name: String,
    selected: Boolean,
    location: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = if (selected) Color(0xFF3398F3) else Color(0xFFE9F4FF),
        shape = RoundedCornerShape(24.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFC7E8FF)) else null,
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (location) {
                Icon(Icons.Rounded.LocationOn, null, tint = if (selected) Color.White else Color(0xFF338CE2), modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(name, color = if (selected) Color.White else MiraNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun WeatherMetricCard(
    icon: Int,
    label: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxWidth(), color = backgroundColor, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(icon), null, Modifier.size(38.dp))
            Spacer(Modifier.width(11.dp))
            Column {
                Text(label, color = Color(0xFF47688E), fontSize = 12.sp)
                Text(value, color = MiraNavy, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun WeatherForecastCard(daily: DailyWeather, modifier: Modifier = Modifier) {
    val date = runCatching { LocalDate.parse(daily.date) }.getOrNull()
    val weekDay = when (date?.dayOfWeek) {
        DayOfWeek.MONDAY -> "周一"; DayOfWeek.TUESDAY -> "周二"; DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"; DayOfWeek.FRIDAY -> "周五"; DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"; else -> ""
    }
    Surface(modifier, color = Color(0xFFF0F8FF), shape = RoundedCornerShape(17.dp)) {
        Column(
            Modifier.fillMaxSize().padding(top = 5.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                date?.let { "${it.monthValue}月${it.dayOfMonth}日  $weekDay" } ?: daily.date,
                color = MiraNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
            Image(painterResource(weatherIconResource(daily.weatherCode)), daily.description, Modifier.size(36.dp))
            Text(daily.description, color = MiraNavy, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(
                "${daily.minTemperature.toInt()}℃ ~ ${daily.maxTemperature.toInt()}℃",
                color = MiraNavy,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
    }
}

private fun weatherIconResource(code: Int): Int = when (code) {
    0 -> R.drawable.mira_weather_sunny
    1, 2 -> R.drawable.mira_weather_partly_cloudy
    3 -> R.drawable.mira_weather_cloudy
    45, 48 -> R.drawable.mira_weather_fog
    71, 73, 75, 77, 85, 86 -> R.drawable.mira_weather_snow
    95, 96, 99 -> R.drawable.mira_weather_storm
    else -> R.drawable.mira_weather_rain
}
