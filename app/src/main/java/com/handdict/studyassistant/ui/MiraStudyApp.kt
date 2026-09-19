package com.handdict.studyassistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.handdict.studyassistant.data.DictionaryEntry
import com.handdict.studyassistant.data.DictionaryRepository
import com.handdict.studyassistant.data.FocusRecord
import com.handdict.studyassistant.data.Lesson
import com.handdict.studyassistant.data.LocalStore
import com.handdict.studyassistant.data.TimerSnapshot
import com.handdict.studyassistant.data.WeatherInfo
import com.handdict.studyassistant.data.WeatherRepository
import com.handdict.studyassistant.data.weatherCities
import com.handdict.studyassistant.timer.TimerAlarmScheduler
import com.handdict.studyassistant.ui.theme.MiraBlue
import com.handdict.studyassistant.ui.theme.MiraGreen
import com.handdict.studyassistant.ui.theme.MiraNavy
import com.handdict.studyassistant.ui.theme.MiraOrange
import com.handdict.studyassistant.ui.theme.MiraSky
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private enum class AppScreen(val label: String, val symbol: String) {
    HOME("首页", "⌂"), TIMER("作业计时", "◷"), DICTIONARY("查字典", "▤"),
    SCHEDULE("课程表", "▦"), WEATHER("天气", "☁"),
}

private val screenItems = AppScreen.entries
private val cardShape = RoundedCornerShape(24.dp)

@Composable
fun MiraStudyApp() {
    var selected by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        ) {
            if (maxWidth >= 720.dp) {
                Row(Modifier.fillMaxSize()) {
                    TabletNavigation(selected = selected, onSelected = { selected = it })
                    Column(Modifier.fillMaxSize()) {
                        AppHeader(selected)
                        ScreenContent(selected, onNavigate = { selected = it }, Modifier.weight(1f))
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    AppHeader(selected)
                    ScreenContent(selected, onNavigate = { selected = it }, Modifier.weight(1f))
                    CompactNavigation(selected = selected, onSelected = { selected = it })
                }
            }
        }
    }
}

@Composable
private fun AppHeader(selected: AppScreen) {
    val date = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(74.dp).padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (selected == AppScreen.HOME) "Mira 学习助手" else selected.label, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.width(20.dp))
        Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text("为今天的进步加油  ·  认真一点点，成长一大步", color = MiraGreen)
    }
}

@Composable
private fun TabletNavigation(selected: AppScreen, onSelected: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier.width(154.dp).fillMaxHeight().background(Color(0xFFFCFDFE)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("M", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp,
            modifier = Modifier.background(MiraBlue, CircleShape).padding(horizontal = 15.dp, vertical = 8.dp))
        Spacer(Modifier.height(28.dp))
        screenItems.forEach { item ->
            val isSelected = item == selected
            Surface(
                onClick = { onSelected(item) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) MiraSky else Color.Transparent,
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 13.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(item.symbol, fontSize = 25.sp, color = if (isSelected) MiraBlue else MiraNavy)
                    Text(item.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MiraBlue else MiraNavy)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("学而时习之", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("不亦说乎", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CompactNavigation(selected: AppScreen, onSelected: (AppScreen) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        screenItems.forEach { item ->
            TextButton(onClick = { onSelected(item) }) {
                Text("${item.symbol} ${item.label}", color = if (item == selected) MiraBlue else MiraNavy)
            }
        }
    }
}

@Composable
private fun ScreenContent(screen: AppScreen, onNavigate: (AppScreen) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(start = 22.dp, end = 24.dp, bottom = 22.dp)) {
        when (screen) {
            AppScreen.HOME -> HomeScreen(onNavigate)
            AppScreen.TIMER -> TimerScreen()
            AppScreen.DICTIONARY -> DictionaryScreen()
            AppScreen.SCHEDULE -> ScheduleScreen()
            AppScreen.WEATHER -> WeatherScreen()
        }
    }
}

@Composable
private fun MiraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
private fun HomeScreen(onNavigate: (AppScreen) -> Unit) {
    val context = LocalContext.current
    val todayIndex = LocalDate.now().dayOfWeek.value - 1
    val todayLessons = remember {
        if (todayIndex in 0..4) LocalStore(context).loadLessons().filter { it.day == todayIndex }.sortedBy { it.start }
        else emptyList()
    }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(0.9f), onClick = { onNavigate(AppScreen.TIMER) }) {
                Column(
                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFEAF6FF), Color.White))).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(Modifier.fillMaxWidth()) { SectionTitle("◷", "作业计时") }
                    Spacer(Modifier.weight(1f))
                    Text("语文", style = MaterialTheme.typography.headlineLarge)
                    Text("35:00", style = MaterialTheme.typography.displayLarge, color = MiraNavy)
                    Text("点击开始专注", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { onNavigate(AppScreen.TIMER) }, modifier = Modifier.fillMaxWidth(0.65f)) { Text("开始计时") }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(1.1f), onClick = { onNavigate(AppScreen.SCHEDULE) }) {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("▦", "课程表", MiraOrange)
                        Spacer(Modifier.weight(1f)); Text(if (todayIndex in 0..4) "今日课程  ›" else "周末休息  ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(14.dp))
                    if (todayLessons.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (todayIndex in 0..4) "今天还没有安排课程" else "周末没有课程，好好休息吧", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        todayLessons.take(4).forEachIndexed { index, lesson -> LessonRow(index + 1, lesson) }
                    }
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(0.95f), onClick = { onNavigate(AppScreen.DICTIONARY) }) {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    SectionTitle("▤", "查字典", MiraGreen)
                    Spacer(Modifier.height(14.dp))
                    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF5F8FB), modifier = Modifier.fillMaxWidth()) {
                        Text("⌕  输入汉字或拼音，快速查询", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("规", fontSize = 76.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(26.dp))
                        Column { Text("guī", style = MaterialTheme.typography.headlineLarge); Text("部首 见   ·   8画") }
                    }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(1.05f), onClick = { onNavigate(AppScreen.WEATHER) }) {
                Column(
                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFDDF2FF), Color(0xFFF7FCFF)))).padding(24.dp),
                ) {
                    Row(Modifier.fillMaxWidth()) { SectionTitle("☁", "天气") }
                    Spacer(Modifier.weight(1f))
                    Text("上海", style = MaterialTheme.typography.headlineMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("24°C", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.weight(1f)); Text("🌤️", fontSize = 68.sp)
                    }
                    Text("多云  ·  空气清新", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text("进入天气页面可选择城市并刷新实时数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TimerScreen() {
    val context = LocalContext.current
    val store = remember { LocalStore(context) }
    val snapshot = remember { store.loadTimer() }
    val records = remember { mutableStateListOf<FocusRecord>().also { it.addAll(store.loadFocusRecords()) } }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) Toast.makeText(context, "未开启通知权限，计时仍可使用，但后台结束时不会弹出提醒", Toast.LENGTH_LONG).show()
    }
    var taskName by rememberSaveable { mutableStateOf(snapshot.taskName) }
    var durationMinutes by rememberSaveable { mutableIntStateOf(snapshot.durationMinutes) }
    var remainingSeconds by rememberSaveable {
        mutableIntStateOf(if (snapshot.running) max(0, ((snapshot.endAtMillis - System.currentTimeMillis()) / 1000).toInt()) else snapshot.remainingSeconds)
    }
    var running by rememberSaveable { mutableStateOf(snapshot.running && remainingSeconds > 0) }
    var endAtMillis by rememberSaveable { mutableLongStateOf(if (running) snapshot.endAtMillis else 0L) }

    LaunchedEffect(running, endAtMillis) {
        while (running) {
            remainingSeconds = max(0, ((endAtMillis - System.currentTimeMillis()) / 1000).toInt())
            if (remainingSeconds == 0) {
                val completedAt = endAtMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
                running = false
                store.addFocusRecord(FocusRecord(completedAt, taskName.ifBlank { "专注作业" }, durationMinutes, completedAt))
                records.clear(); records.addAll(store.loadFocusRecords())
                store.saveTimer(TimerSnapshot(taskName, durationMinutes, 0, false, 0L))
                break
            }
            delay(500)
        }
    }

    fun saveCurrent() = store.saveTimer(TimerSnapshot(taskName, durationMinutes, remainingSeconds, running, endAtMillis))

    MiraCard(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize().padding(34.dp), horizontalArrangement = Arrangement.spacedBy(42.dp)) {
            Column(Modifier.weight(0.8f)) {
                SectionTitle("◷", "作业计时")
                Spacer(Modifier.height(28.dp))
                OutlinedTextField(taskName, { taskName = it }, label = { Text("本次作业") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(20.dp))
                Text("选择时长", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(25, 35, 45, 60).forEach { minutes ->
                        FilterChip(selected = durationMinutes == minutes, onClick = {
                            durationMinutes = minutes; if (!running) remainingSeconds = minutes * 60
                        }, label = { Text("${minutes}分钟") })
                    }
                }
                Spacer(Modifier.height(26.dp))
                Text("建议每完成一轮，起身活动和眺望远处。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (records.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("最近完成", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { store.clearFocusRecords(); records.clear() }) { Text("清空") }
                    }
                    Spacer(Modifier.height(8.dp))
                    records.take(3).forEach { record ->
                        val time = Instant.ofEpochMilli(record.completedAtMillis).atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("M月d日 HH:mm"))
                        Text("✓ ${record.taskName} · ${record.durationMinutes}分钟 · $time", color = MiraGreen, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
            Column(
                Modifier.weight(1.2f).fillMaxHeight().background(Color(0xFFEAF6FF), cardShape).padding(34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(taskName.ifBlank { "专注作业" }, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                Text(String.format(Locale.ROOT, "%02d:%02d", minutes, seconds), style = MaterialTheme.typography.displayLarge, fontSize = 82.sp)
                Spacer(Modifier.height(18.dp))
                LinearProgressIndicator(
                    progress = { if (durationMinutes == 0) 0f else remainingSeconds / (durationMinutes * 60f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                )
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        if (running) {
                            remainingSeconds = max(0, ((endAtMillis - System.currentTimeMillis()) / 1000).toInt())
                            running = false; endAtMillis = 0L
                            TimerAlarmScheduler.cancel(context); saveCurrent()
                        } else {
                            if (remainingSeconds <= 0) remainingSeconds = durationMinutes * 60
                            endAtMillis = System.currentTimeMillis() + remainingSeconds * 1000L
                            running = true
                            TimerAlarmScheduler.schedule(context, taskName.ifBlank { "专注作业" }, durationMinutes, endAtMillis)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            saveCurrent()
                        }
                    }, modifier = Modifier.width(150.dp)) { Text(if (running) "暂停" else "开始") }
                    FilledTonalButton(onClick = {
                        running = false; remainingSeconds = durationMinutes * 60; endAtMillis = 0L
                        TimerAlarmScheduler.cancel(context); saveCurrent()
                    }, modifier = Modifier.width(150.dp)) { Text("重置") }
                    OutlinedButton(onClick = {
                        val completedAt = System.currentTimeMillis()
                        running = false; remainingSeconds = 0; endAtMillis = 0L
                        TimerAlarmScheduler.cancel(context)
                        store.addFocusRecord(FocusRecord(completedAt, taskName.ifBlank { "专注作业" }, durationMinutes, completedAt))
                        records.clear(); records.addAll(store.loadFocusRecords())
                        saveCurrent()
                    }, modifier = Modifier.width(150.dp)) { Text("完成") }
                }
            }
        }
    }
}

@Composable
private fun DictionaryScreen() {
    val context = LocalContext.current
    val repository = remember { DictionaryRepository(context.applicationContext) }
    var query by rememberSaveable { mutableStateOf("") }
    var entry by remember { mutableStateOf<DictionaryEntry?>(null) }
    var results by remember { mutableStateOf<List<DictionaryEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(query) {
        delay(120)
        loading = true
        val found = withContext(Dispatchers.IO) { repository.search(query) }
        results = found
        entry = found.firstOrNull()
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("输入汉字、拼音或词语") },
            leadingIcon = { Text("⌕", fontSize = 26.sp) },
            trailingIcon = {
                Text(if (loading) "查询中" else "${results.size} 条", color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        if (!loading && results.size > 1) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                results.forEach { result ->
                    FilterChip(
                        selected = entry?.word == result.word,
                        onClick = { entry = result },
                        label = { Text("${result.word}  ${result.pinyin.replace(" / ", " · ")}") },
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        val selectedEntry = entry
        if (!loading && selectedEntry == null) {
            MiraCard(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("3500 常用字库暂未找到“$query”") }
            }
        } else if (selectedEntry != null) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiraCard(Modifier.weight(0.92f).fillMaxHeight()) { CharacterPanel(selectedEntry, onSelect = { query = it }) }
                Column(Modifier.weight(1.08f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MiraCard(Modifier.fillMaxWidth().weight(1.15f)) { MeaningPanel(selectedEntry) }
                    MiraCard(Modifier.fillMaxWidth().weight(0.85f)) { WordsPanel(selectedEntry, onSelect = { query = it }) }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("正在打开离线字典…") }
        }
    }
}

@Composable
private fun CharacterPanel(entry: DictionaryEntry, onSelect: (String) -> Unit) {
    val speak = rememberSpeaker()
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth()) { SectionTitle("▤", "汉字详情", MiraGreen) }
        Spacer(Modifier.height(14.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFFFFAF0), modifier = Modifier.size(260.dp)) {
            StrokeOrderView(entry.word, entry.strokePaths)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(entry.pinyin, style = MaterialTheme.typography.headlineLarge)
            FilledTonalButton(onClick = { speak(entry.word) }) { Text("🔊 朗读") }
        }
        Spacer(Modifier.height(6.dp))
        Text("部首 ${entry.radical}   ·   ${entry.strokes}画   ·   ${entry.structure}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        if (entry.related.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                entry.related.take(4).forEach { RelatedTile(it, Modifier.weight(1f), onSelect) }
            }
        }
    }
}

@Composable
private fun StrokeOrderView(character: String, strokePaths: List<String>) {
    val parsedPaths = remember(strokePaths) {
        strokePaths.mapNotNull { data -> runCatching { PathParser().parsePathString(data).toPath() }.getOrNull() }
    }
    var visibleStrokes by remember(character, parsedPaths) { mutableIntStateOf(parsedPaths.size) }
    var playing by remember(character) { mutableStateOf(false) }

    LaunchedEffect(playing, parsedPaths) {
        if (playing) {
            visibleStrokes = 0
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
            Text(character, fontSize = 142.sp, fontWeight = FontWeight.Medium, color = MiraNavy, modifier = Modifier.align(Alignment.Center))
        }
        FilledTonalButton(
            onClick = { playing = true },
            enabled = parsedPaths.isNotEmpty() && !playing,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
        ) { Text(if (playing) "播放中" else "▶ 笔顺") }
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
    Column(Modifier.fillMaxSize().padding(26.dp).verticalScroll(rememberScrollState())) {
        SectionTitle("◉", "基本释义")
        Spacer(Modifier.height(18.dp))
        entry.meanings.forEachIndexed { index, meaning ->
            Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.Top) {
                Text("${index + 1}", color = Color.White, textAlign = TextAlign.Center,
                    modifier = Modifier.size(34.dp).background(MiraGreen, CircleShape).padding(top = 6.dp))
                Spacer(Modifier.width(14.dp)); Text(meaning, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun WordsPanel(entry: DictionaryEntry, onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(26.dp)) {
        SectionTitle("⌁", "组词", MiraGreen)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            entry.phrases.forEach { phrase ->
                Surface(onClick = { onSelect(phrase.take(1)) }, shape = RoundedCornerShape(14.dp), color = Color(0xFFFFF1CF)) {
                    Text(phrase, Modifier.padding(horizontal = 20.dp, vertical = 14.dp), color = Color(0xFF8A4B08), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RelatedTile(text: String, modifier: Modifier = Modifier, onClick: (String) -> Unit) {
    Surface(onClick = { onClick(text) }, modifier = modifier, shape = RoundedCornerShape(12.dp), color = MiraSky) {
        Text(text, Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, fontSize = 22.sp)
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
    var editingLesson by remember { mutableStateOf<Lesson?>(null) }
    var deletingLesson by remember { mutableStateOf<Lesson?>(null) }
    val weekdays = listOf("周一", "周二", "周三", "周四", "周五")

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                weekdays.forEachIndexed { index, day -> FilterChip(selected = selectedDay == index, onClick = { selectedDay = index }, label = { Text(day) }) }
            }
            Button(onClick = { showAdd = true }) { Text("＋ 添加课程") }
        }
        Spacer(Modifier.height(14.dp))
        MiraCard(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState())) {
                SectionTitle("▦", "${weekdays[selectedDay]}课程", MiraOrange)
                Spacer(Modifier.height(18.dp))
                val dayLessons = lessons.filter { it.day == selectedDay }.sortedBy { it.start }
                if (dayLessons.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { Text("今天还没有课程，可以点击右上角添加。") }
                }
                dayLessons.forEachIndexed { index, lesson ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { LessonRow(index + 1, lesson) }
                        TextButton(onClick = { editingLesson = lesson }) { Text("编辑") }
                        TextButton(onClick = { deletingLesson = lesson }) { Text("删除") }
                    }
                }
            }
        }
    }

    if (showAdd) {
        LessonDialog(day = selectedDay, initial = null, onDismiss = { showAdd = false }, onSave = { lesson ->
            val conflict = lessons.any { it.day == lesson.day && lesson.start < it.end && lesson.end > it.start }
            if (conflict) Toast.makeText(context, "这个时间段已经有课程了", Toast.LENGTH_SHORT).show()
            else { lessons.add(lesson); store.saveLessons(lessons); showAdd = false }
        })
    }
    editingLesson?.let { original ->
        LessonDialog(day = original.day, initial = original, onDismiss = { editingLesson = null }, onSave = { updated ->
            val conflict = lessons.any { it != original && it.day == updated.day && updated.start < it.end && updated.end > it.start }
            if (conflict) Toast.makeText(context, "这个时间段已经有课程了", Toast.LENGTH_SHORT).show()
            else {
                val index = lessons.indexOf(original)
                if (index >= 0) lessons[index] = updated
                store.saveLessons(lessons)
                editingLesson = null
            }
        })
    }
    deletingLesson?.let { lesson ->
        AlertDialog(
            onDismissRequest = { deletingLesson = null },
            title = { Text("删除课程") },
            text = { Text("确定删除“${lesson.name}（${lesson.start} - ${lesson.end}）”吗？") },
            confirmButton = {
                TextButton(onClick = {
                    lessons.remove(lesson); store.saveLessons(lessons); deletingLesson = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingLesson = null }) { Text("取消") } },
        )
    }
}

@Composable
private fun LessonRow(index: Int, lesson: Lesson) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(index.toString(), color = Color.White, textAlign = TextAlign.Center,
            modifier = Modifier.size(34.dp).background(listOf(MiraBlue, MiraGreen, Color(0xFFF0AC38), Color(0xFF8B66CE))[index % 4], CircleShape).padding(top = 6.dp))
        Spacer(Modifier.width(15.dp))
        Text(lesson.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text("${lesson.start} - ${lesson.end}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LessonDialog(day: Int, initial: Lesson?, onDismiss: () -> Unit, onSave: (Lesson) -> Unit) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var start by remember(initial) { mutableStateOf(initial?.start ?: "08:00") }
    var end by remember(initial) { mutableStateOf(initial?.end ?: "08:45") }
    val timePattern = remember { Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$") }
    val timeFormatValid = timePattern.matches(start) && timePattern.matches(end)
    val timeOrderValid = timeFormatValid && start < end
    val canSave = name.isNotBlank() && timeOrderValid
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "添加课程" else "编辑课程") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("课程名称") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(start, { start = it }, label = { Text("开始") }, isError = start.isNotEmpty() && !timePattern.matches(start), modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(end, { end = it }, label = { Text("结束") }, isError = end.isNotEmpty() && !timePattern.matches(end), modifier = Modifier.weight(1f), singleLine = true)
                }
                Text(
                    if (timeFormatValid && !timeOrderValid) "结束时间必须晚于开始时间" else "时间格式示例：08:00",
                    color = if (timeFormatValid && !timeOrderValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(enabled = canSave, onClick = { onSave(Lesson(day, name.trim(), start, end)) }) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun WeatherScreen() {
    var cityIndex by rememberSaveable { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    var info by remember { mutableStateOf<WeatherInfo?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val city = weatherCities[cityIndex]

    LaunchedEffect(cityIndex, refresh) {
        loading = true; error = null
        runCatching { withContext(Dispatchers.IO) { WeatherRepository.load(city) } }
            .onSuccess { info = it }
            .onFailure { error = it.message ?: "无法连接天气服务" }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                weatherCities.forEachIndexed { index, item -> FilterChip(selected = cityIndex == index, onClick = { cityIndex = index }, label = { Text(item.name) }) }
            }
            OutlinedButton(onClick = { refresh++ }) { Text("刷新") }
        }
        Spacer(Modifier.height(14.dp))
        MiraCard(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFDDF2FF), Color.White))).padding(40.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    SectionTitle("☁", "实时天气")
                    Spacer(Modifier.height(30.dp))
                    Text(city.name, style = MaterialTheme.typography.headlineLarge)
                    when {
                        loading -> Text("正在获取天气…", style = MaterialTheme.typography.titleLarge)
                        error != null -> {
                            Text("暂时无法获取", style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(8.dp)); Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                        }
                        info != null -> {
                            Text("${info!!.temperature.toInt()}°C", style = MaterialTheme.typography.displayLarge, fontSize = 88.sp)
                            Text(info!!.description, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
                Column(Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(info?.symbol ?: "🌤️", fontSize = 118.sp)
                    Spacer(Modifier.height(18.dp))
                    Surface(shape = cardShape, color = Color.White.copy(alpha = 0.82f)) {
                        Column(Modifier.padding(26.dp).widthIn(min = 260.dp)) {
                            WeatherMetric("相对湿度", info?.let { "${it.humidity}%" } ?: "--")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            WeatherMetric("风速", info?.let { "${it.windSpeed} km/h" } ?: "--")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            WeatherMetric("更新时间", "刚刚")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetric(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.weight(1f)); Text(value, fontWeight = FontWeight.Bold) }
}
