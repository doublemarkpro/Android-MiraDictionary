package com.handdict.studyassistant.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.handdict.studyassistant.R
import com.handdict.studyassistant.data.DictionaryEntry
import com.handdict.studyassistant.data.DictionaryRepository
import com.handdict.studyassistant.data.DeviceWeatherRepository
import com.handdict.studyassistant.data.FocusRecord
import com.handdict.studyassistant.data.Lesson
import com.handdict.studyassistant.data.LocatedWeather
import com.handdict.studyassistant.data.LocalStore
import com.handdict.studyassistant.data.TimerSnapshot
import com.handdict.studyassistant.data.WeatherInfo
import com.handdict.studyassistant.data.WeatherCache
import com.handdict.studyassistant.data.WeatherRepository
import com.handdict.studyassistant.data.homeworkSubjects
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
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private enum class AppScreen(val label: String, val symbol: String) {
    HOME("首页", "⌂"), TIMER("作业计时", "◷"), DICTIONARY("查字典", "▤"),
    SCHEDULE("课程表", "▦"), WEATHER("天气", "☁"), SETTINGS("设置", "⚙"),
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
    AppScreen.SCHEDULE -> Color(0xFFFF7468)
    AppScreen.WEATHER -> Color(0xFF4AA8E8)
    AppScreen.SETTINGS -> Color(0xFF7B72E9)
}

private val screenItems = AppScreen.entries
private val cardShape = RoundedCornerShape(24.dp)
private val miraDisplayFont = FontFamily(Font(R.font.zcool_kuaile_regular))
private val miraHandFont = FontFamily(Font(R.font.ma_shan_zheng_regular))
private val miraKaiFont = FontFamily(Font(R.font.lxgw_wenkai_regular))

private fun formatTimer(totalSeconds: Int): String = String.format(
    Locale.ROOT,
    "%02d:%02d",
    totalSeconds.coerceAtLeast(0) / 60,
    totalSeconds.coerceAtLeast(0) % 60,
)

private fun screenIcon(screen: AppScreen): ImageVector = when (screen) {
    AppScreen.HOME -> Icons.Rounded.Home
    AppScreen.TIMER -> Icons.Rounded.AccessTime
    AppScreen.DICTIONARY -> Icons.AutoMirrored.Rounded.MenuBook
    AppScreen.SCHEDULE -> Icons.Rounded.CalendarMonth
    AppScreen.WEATHER -> Icons.Rounded.WbSunny
    AppScreen.SETTINGS -> Icons.Rounded.Settings
}

@Composable
fun MiraStudyApp() {
    val context = LocalContext.current
    val store = remember { LocalStore(context.applicationContext) }
    var selected by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var pendingDictionaryQuery by rememberSaveable { mutableStateOf("") }
    var navigationStyleKey by rememberSaveable { mutableStateOf(store.loadNavigationStyle()) }
    val navigationStyle = navigationStyleFor(navigationStyleKey)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFFF7FBFF), Color(0xFFFFFCF5))))
                .statusBarsPadding().navigationBarsPadding(),
        ) {
            if (maxWidth >= 720.dp) {
                Row(Modifier.fillMaxSize()) {
                    TabletNavigation(selected = selected, style = navigationStyle, onSelected = { selected = it })
                    Column(Modifier.fillMaxSize()) {
                        when (selected) {
                            AppScreen.HOME -> HomeHeroHeader()
                            AppScreen.TIMER -> TimerHeroHeader()
                            else -> AppHeader(selected)
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
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    when (selected) {
                        AppScreen.HOME -> HomeHeroHeader()
                        AppScreen.TIMER -> TimerHeroHeader()
                        else -> AppHeader(selected)
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
                        modifier = Modifier.weight(1f),
                    )
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
        modifier = Modifier.fillMaxWidth().height(82.dp).padding(horizontal = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(if (selected == AppScreen.HOME) "Mira 学习助手" else selected.label, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Box(Modifier.width(if (selected == AppScreen.HOME) 74.dp else 42.dp).height(4.dp).background(screenAccent(selected), CircleShape))
        }
        Spacer(Modifier.width(20.dp))
        Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Surface(color = Color(0xFFFFF3C9), shape = RoundedCornerShape(14.dp)) {
            Text("⭐ 为今天的进步加油 · 认真一点点，成长一大步", Modifier.padding(horizontal = 16.dp, vertical = 9.dp), color = Color(0xFF80601B))
        }
    }
}

@Composable
private fun HomeHeroHeader() {
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日  EEEE", Locale.CHINA))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFDFF2FF), Color(0xFFFDFCF7), Color(0xFFE4F6EB)),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cloud = Color.White.copy(alpha = 0.86f)
            drawCircle(cloud, radius = size.height * 0.62f, center = Offset(size.width * 0.23f, size.height * 0.56f))
            drawCircle(cloud, radius = size.height * 0.48f, center = Offset(size.width * 0.40f, size.height * 0.51f))
            drawCircle(Color.White.copy(alpha = 0.58f), radius = size.height * 0.42f, center = Offset(size.width * 0.59f, size.height * 0.25f))
            val underline = Path().apply {
                moveTo(size.width * 0.075f, size.height * 0.50f)
                quadraticTo(size.width * 0.24f, size.height * 0.56f, size.width * 0.41f, size.height * 0.48f)
            }
            drawPath(underline, Color(0xFFFFCA3A), style = Stroke(width = 6f, cap = StrokeCap.Round))
        }
        Image(
            painter = painterResource(R.drawable.mira_green_hills),
            contentDescription = null,
            modifier = Modifier.align(Alignment.BottomStart).width(150.dp).height(74.dp).alpha(0.66f),
            contentScale = ContentScale.Crop,
        )
        Column(Modifier.align(Alignment.CenterStart).padding(start = 30.dp, bottom = 1.dp)) {
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
            Row(Modifier.padding(start = 56.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("好好学习，遇见更棒的自己！", color = Color(0xFF496991), fontSize = 20.sp, fontFamily = miraHandFont)
                Spacer(Modifier.width(13.dp))
                Icon(Icons.Rounded.SentimentSatisfiedAlt, null, tint = Color(0xFF6C7E9D), modifier = Modifier.size(29.dp))
            }
        }
        Column(Modifier.align(Alignment.Center).padding(start = 260.dp, top = 26.dp)) {
            Text(today, color = Color(0xFF173C72), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("今天也是闪闪发光的一天！", color = Color(0xFF5A7194), fontFamily = miraHandFont, fontSize = 17.sp, modifier = Modifier.padding(top = 6.dp))
        }
        Image(
            painter = painterResource(R.drawable.mira_home_header_student),
            contentDescription = "开心学习的小学生",
            modifier = Modifier.align(Alignment.BottomEnd).width(310.dp).height(172.dp).padding(end = 12.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun TimerHeroHeader() {
    Box(
        Modifier.fillMaxWidth().height(112.dp).background(
            Brush.horizontalGradient(listOf(Color(0xFFCDEEFF), Color(0xFFBCE8FF), Color(0xFFE5F8FF))),
        ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.72f), size.height * 0.48f, Offset(size.width * 0.48f, size.height * 0.72f))
            drawCircle(Color.White.copy(alpha = 0.72f), size.height * 0.38f, Offset(size.width * 0.59f, size.height * 0.70f))
            val underline = Path().apply {
                moveTo(size.width * 0.18f, size.height * 0.72f)
                quadraticTo(size.width * 0.21f, size.height * 0.50f, size.width * 0.25f, size.height * 0.68f)
                quadraticTo(size.width * 0.27f, size.height * 0.77f, size.width * 0.30f, size.height * 0.61f)
            }
            drawPath(underline, Color(0xFFFFD939), style = Stroke(7f, cap = StrokeCap.Round))
        }
        Row(
            Modifier.align(Alignment.CenterStart).padding(start = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("✏️", fontSize = 39.sp)
            Spacer(Modifier.width(10.dp))
            Text("作业计时", color = Color(0xFF082B63), fontSize = 39.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(48.dp))
            Column {
                Text("专注当下，", color = Color(0xFF496987), fontSize = 18.sp, fontFamily = miraHandFont)
                Text("每天都有进步！  ☺", color = Color(0xFF496987), fontSize = 18.sp, fontFamily = miraHandFont)
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 178.dp).rotate(-7f),
            color = Color(0xFFFFF2B8),
            shape = RoundedCornerShape(4.dp),
            shadowElevation = 2.dp,
        ) {
            Text("⭐ 加油！\n   你可以的！", Modifier.padding(horizontal = 18.dp, vertical = 9.dp), color = Color(0xFF29476E), fontFamily = miraHandFont, fontSize = 17.sp)
        }
        Image(
            painterResource(R.drawable.mira_backpack_books),
            contentDescription = null,
            modifier = Modifier.align(Alignment.BottomEnd).width(180.dp).height(105.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun TabletNavigation(selected: AppScreen, style: NavigationStyle, onSelected: (AppScreen) -> Unit) {
    val panelColors = when (style) {
        NavigationStyle.FRESH -> listOf(Color(0xFFF2F9FF), Color(0xFFE8F5FF))
        NavigationStyle.COLORFUL -> listOf(Color.White, Color(0xFFFFF8EE))
        NavigationStyle.JOURNAL -> listOf(Color(0xFFFFFCF3), Color(0xFFF2F8E9))
        NavigationStyle.MINIMAL -> listOf(Color(0xFFF8FAFD), Color(0xFFF0F4F9))
        NavigationStyle.STATIONERY -> listOf(Color(0xFFFBFEFF), Color(0xFFEAF7FF))
    }
    Column(
        modifier = Modifier
            .width(132.dp)
            .fillMaxHeight()
            .background(Brush.verticalGradient(panelColors))
            .padding(horizontal = 11.dp, vertical = 15.dp),
    ) {
        screenItems.forEach { item ->
            val isSelected = item == selected
            val accent = if (style == NavigationStyle.COLORFUL) screenAccent(item) else when (style) {
                NavigationStyle.JOURNAL -> MiraGreen
                else -> Color(0xFF3E8FF3)
            }
            val selectedBackground = when (style) {
                NavigationStyle.FRESH -> accent
                NavigationStyle.COLORFUL -> accent.copy(alpha = 0.16f)
                NavigationStyle.JOURNAL -> Color(0xFFFFE9A8)
                NavigationStyle.MINIMAL -> Color.White
                NavigationStyle.STATIONERY -> Color(0xFF438FF4)
            }
            val contentColor = when {
                !isSelected -> when (style) {
                    NavigationStyle.JOURNAL -> Color(0xFF53715C)
                    NavigationStyle.MINIMAL -> Color(0xFF66758A)
                    NavigationStyle.STATIONERY -> Color(0xFF29476E)
                    else -> Color(0xFF647EA5)
                }
                style == NavigationStyle.FRESH || style == NavigationStyle.STATIONERY -> Color.White
                else -> accent
            }
            Surface(
                onClick = { onSelected(item) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(if (style == NavigationStyle.JOURNAL) 7.dp else 13.dp),
                color = if (isSelected) selectedBackground else Color.Transparent,
                shadowElevation = if (isSelected && (style == NavigationStyle.FRESH || style == NavigationStyle.MINIMAL)) 2.dp else 0.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(27.dp), contentAlignment = Alignment.Center) {
                        NavigationStyleIcon(style, item, isSelected, accent, contentColor)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) contentColor else if (style == NavigationStyle.JOURNAL) Color(0xFF43604C) else Color(0xFF29476E),
                        fontFamily = if (style == NavigationStyle.JOURNAL) miraHandFont else FontFamily.Default,
                        fontSize = 15.sp,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.height(13.dp))
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth().height(156.dp)) {
            Image(
                painterResource(R.drawable.mira_green_hills),
                contentDescription = null,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(86.dp),
                contentScale = ContentScale.Crop,
            )
            Surface(
                color = Color(0xFFEAF7D8),
                shape = RoundedCornerShape(5.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.align(Alignment.Center).rotate(-7f),
            ) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("每天", color = Color(0xFF54718A), fontFamily = miraHandFont, fontSize = 17.sp)
                    Text("进步一点点", color = Color(0xFF54718A), fontFamily = miraHandFont, fontSize = 17.sp)
                    Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFC52D), modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}

@Composable
private fun NavigationStyleIcon(
    style: NavigationStyle,
    item: AppScreen,
    selected: Boolean,
    accent: Color,
    contentColor: Color,
) {
    when (style) {
        NavigationStyle.FRESH -> {
            if (item == AppScreen.TIMER) {
                NavigationClockIcon(circleColor = contentColor, handColor = if (selected) accent else Color.White)
            } else {
                Icon(screenIcon(item), null, tint = contentColor, modifier = Modifier.size(24.dp))
            }
        }
        NavigationStyle.COLORFUL -> {
            val emoji = when (item) {
                AppScreen.HOME -> "🏠"; AppScreen.TIMER -> "⏱"; AppScreen.DICTIONARY -> "📖"
                AppScreen.SCHEDULE -> "📅"; AppScreen.WEATHER -> "🌤"; AppScreen.SETTINGS -> "⚙️"
            }
            Text(emoji, fontSize = 21.sp, textAlign = TextAlign.Center)
        }
        NavigationStyle.JOURNAL -> {
            val symbol = when (item) {
                AppScreen.HOME -> "⌂"; AppScreen.TIMER -> "◷"; AppScreen.DICTIONARY -> "书"
                AppScreen.SCHEDULE -> "日"; AppScreen.WEATHER -> "☀"; AppScreen.SETTINGS -> "⚙"
            }
            Text(symbol, color = contentColor, fontFamily = miraHandFont, fontSize = 24.sp, textAlign = TextAlign.Center)
        }
        NavigationStyle.MINIMAL -> Icon(screenIcon(item), null, tint = contentColor, modifier = Modifier.size(21.dp))
        NavigationStyle.STATIONERY -> {
            if (selected) {
                Surface(color = Color.White, shape = CircleShape, modifier = Modifier.size(31.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(screenIcon(item), null, tint = accent, modifier = Modifier.size(20.dp))
                    }
                }
            } else {
                Icon(screenIcon(item), null, tint = screenAccent(item), modifier = Modifier.size(25.dp))
            }
        }
    }
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
private fun ScreenContent(
    screen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onOpenDictionary: (String) -> Unit,
    dictionaryQuery: String,
    navigationStyle: NavigationStyle,
    onNavigationStyleChanged: (NavigationStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().padding(start = 22.dp, end = 24.dp, bottom = 22.dp)) {
        when (screen) {
            AppScreen.HOME -> HomeScreen(onNavigate, onOpenDictionary)
            AppScreen.TIMER -> TimerScreen(onOpenSettings = { onNavigate(AppScreen.SETTINGS) })
            AppScreen.DICTIONARY -> DictionaryScreen(initialQuery = dictionaryQuery)
            AppScreen.SCHEDULE -> ScheduleScreen()
            AppScreen.WEATHER -> WeatherScreen()
            AppScreen.SETTINGS -> SettingsScreen(navigationStyle, onNavigationStyleChanged)
        }
    }
}

@Composable
private fun MiraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEFC))
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
private fun HomeScreen(onNavigate: (AppScreen) -> Unit, onOpenDictionary: (String) -> Unit) {
    val context = LocalContext.current
    val todayIndex = LocalDate.now().dayOfWeek.value - 1
    val store = remember { LocalStore(context) }
    val isWeekend = todayIndex !in 0..4
    val previewDay = if (isWeekend) 0 else todayIndex
    val todayLessons = remember(previewDay) {
        store.loadLessons().filter { it.day == previewDay }.sortedBy { it.period }
    }
    val homeSnapshot = remember { store.loadTimer() }
    val expectedMinutes = homeSnapshot.expectedMinutes
    val focusRecords = remember { store.loadFocusRecords() }
    val weekStart = remember { LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val completedWeekDays = remember(focusRecords, weekStart) {
        focusRecords.map { Instant.ofEpochMilli(it.completedAtMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
            .filter { !it.isBefore(weekStart) && !it.isAfter(weekStart.plusDays(4)) }
            .map { it.dayOfWeek.value }
            .toSet()
    }

    var selectedSubject by rememberSaveable { mutableStateOf(homeSnapshot.subject.ifBlank { "语文" }) }
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
    LaunchedEffect(homeTimerRunning, homeStartedAtMillis, homeSessionBaseSeconds) {
        while (homeTimerRunning) {
            homeElapsedSeconds = homeSessionBaseSeconds +
                ((System.currentTimeMillis() - homeStartedAtMillis) / 1000).toInt().coerceAtLeast(0)
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
        selectedSubject = subject
        store.saveTimer(store.loadTimer().copy(taskName = "${subject}作业", subject = subject))
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

    val dictionaryRepository = remember { DictionaryRepository(context.applicationContext) }
    var homeQuery by rememberSaveable { mutableStateOf("") }
    var homeEntry by remember { mutableStateOf<DictionaryEntry?>(null) }
    LaunchedEffect(homeQuery) {
        val target = if (homeQuery.isBlank()) "智" else extractDictionaryQuery(homeQuery)
        homeEntry = withContext(Dispatchers.IO) {
            dictionaryRepository.findExact(target) ?: dictionaryRepository.search(target, 1).firstOrNull()
        }
    }

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
                voiceError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "没有听清，再说一次吧"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有听到声音，再试一次吧"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "语音服务暂时不可用"
                    else -> "识别没有完成，请再试一次"
                }
            }
            override fun onResults(results: Bundle?) {
                voiceListening = false
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
        onDispose { speechRecognizer?.destroy() }
    }
    fun startHomeVoiceSearch() {
        showVoiceOverlay = true
        voiceTranscript = ""
        voiceError = null
        if (speechRecognizer == null) {
            voiceError = "设备暂不支持语音识别"
            return
        }
        voiceListening = true
        speechRecognizer.startListening(dictionarySpeechIntent().putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true))
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
            if (isFresh && weatherRefreshRequest == 0) {
                locatedWeather = cache.weather
                return@LaunchedEffect
            }
            weatherLoading = true
            weatherError = null
            runCatching { withContext(Dispatchers.IO) { DeviceWeatherRepository.load(context.applicationContext) } }
                .onSuccess {
                    locatedWeather = it
                    store.saveWeatherCache(WeatherCache(it, System.currentTimeMillis()))
                }
                .onFailure { weatherError = it.message ?: "天气获取失败" }
            weatherLoading = false
        }
    }

    Box(Modifier.fillMaxSize()) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiraCard(Modifier.weight(0.92f).fillMaxHeight()) {
            Box(
                Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF7DD), Color(0xFFFFFDF7)))),
            ) {
                Image(
                    painterResource(R.drawable.mira_green_hills),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(92.dp).alpha(0.72f),
                    contentScale = ContentScale.Crop,
                )
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        HomeTimerBadge()
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("作业计时", color = MiraNavy, fontSize = 27.sp, fontWeight = FontWeight.Black)
                            Text("专注学习，高效完成作业", color = Color(0xFF67738A), fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.weight(0.3f))
                    Surface(
                        modifier = Modifier.size(205.dp),
                        shape = CircleShape,
                        color = Color(0xFFFFFEFA),
                        border = androidx.compose.foundation.BorderStroke(12.dp, Color(0xFFFFEDB5)),
                    ) {
                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(if (homeTimerRunning) "正在专注" else "从零开始", color = Color(0xFF62708A), fontSize = 18.sp)
                            Text(formatTimer(homeElapsedSeconds), color = Color(0xFF122F62), fontSize = 50.sp, fontWeight = FontWeight.Black)
                            Text("预计 $expectedMinutes 分钟", color = Color(0xFF62708A), fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("选择学科", color = Color(0xFF7A6A4C), fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
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
                    Spacer(Modifier.height(9.dp))
                    Button(
                        onClick = { toggleHomeTimer() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB91D)),
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (homeTimerRunning) "暂停计时" else "开始计时", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("专注的你，真的了不起！  ★", color = Color(0xFF54708E), fontSize = 16.sp)
                }
            }
        }

        Column(Modifier.weight(1.05f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(1.35f)) {
                Column(
                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFE7FAF1), Color(0xFFF8FFFB)))).padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFF39BE82), shape = CircleShape) {
                            Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = Color.White, modifier = Modifier.padding(12.dp).size(32.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("查字典", color = MiraNavy, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            Text("认识汉字，探索更大的世界", color = Color(0xFF60748B), fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = homeQuery,
                            onValueChange = { homeQuery = it },
                            modifier = Modifier.weight(1f).height(52.dp),
                            placeholder = { Text("请输入要查询的汉字", color = Color(0xFF8B97AB), fontSize = 15.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Color(0xFF8090A8), modifier = Modifier.size(23.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(26.dp),
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
                    Spacer(Modifier.height(12.dp))
                    Surface(color = Color.White.copy(alpha = 0.72f), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxSize()) {
                        Column(Modifier.padding(14.dp)) {
                            Text("今日推荐汉字", color = Color(0xFF315A73), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                                HomeCharacterTile(homeEntry?.word ?: "智", Modifier.size(114.dp))
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(homeEntry?.word ?: "智", color = MiraNavy, fontSize = 32.sp, fontFamily = miraKaiFont)
                                        Text("  ${homeEntry?.pinyin ?: "zhì"}", color = Color(0xFF71809A), fontSize = 18.sp)
                                    }
                                    Text(homeEntry?.meanings?.firstOrNull() ?: "有智慧，能明辨事理。", color = Color(0xFF5C718C), fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Surface(color = Color(0xFFEFF9F1), shape = RoundedCornerShape(15.dp), modifier = Modifier.padding(top = 8.dp)) {
                                        Text("“智者不惑，勤学多思。”", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color(0xFF466B68), fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(0.65f)) {
                Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFC329), modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(9.dp))
                        Text("本周学习进度", color = MiraNavy, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Text("点亮星星，见证成长！", color = Color(0xFF8090A8), fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("周一", "周二", "周三", "周四", "周五").forEachIndexed { index, label ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.Star,
                                    null,
                                    tint = if (index + 1 in completedWeekDays) Color(0xFFFFBF22) else Color(0xFFC9D2DF),
                                    modifier = Modifier.size(39.dp),
                                )
                                Text(label, color = Color(0xFF68758C), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Column(Modifier.weight(1.08f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(1.25f), onClick = { onNavigate(AppScreen.SCHEDULE) }) {
                Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF1F8FF), Color.White))).padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFF4C9AF0), shape = CircleShape) {
                            Icon(Icons.Rounded.CalendarMonth, null, tint = Color.White, modifier = Modifier.padding(12.dp).size(32.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(if (isWeekend) "下次课程" else "今日课程", color = MiraNavy, fontSize = 25.sp, fontWeight = FontWeight.Black)
                            Text(if (isWeekend) "周一课程，提前做好准备" else "好好上课，收获新知识", color = Color(0xFF60748B), fontSize = 14.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("完整课表", color = Color(0xFF458FE6), fontSize = 13.sp)
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Color(0xFF458FE6), modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    (1..7).forEach { period ->
                        HomeLessonRow(period, todayLessons.firstOrNull { it.period == period })
                    }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(0.75f)) {
                Box(
                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFBFE6FF), Color(0xFFEAF8FF)))),
                ) {
                    Image(
                        painterResource(R.drawable.mira_green_hills), contentDescription = null,
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(58.dp).alpha(0.62f), contentScale = ContentScale.Crop,
                    )
                    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
                        when {
                            !hasLocationPermission -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = Color(0xFF4E94E8), modifier = Modifier.size(25.dp))
                                    Spacer(Modifier.width(7.dp))
                                    Text("当前位置天气", color = MiraNavy, fontSize = 20.sp, fontFamily = miraDisplayFont)
                                }
                                Spacer(Modifier.weight(1f))
                                Text("开启定位后显示身边的实时天气", color = Color(0xFF536B89), fontSize = 14.sp)
                                OutlinedButton(onClick = {
                                    locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                                }) { Text("开启定位") }
                            }
                            weatherLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("正在获取当前位置天气…", color = Color(0xFF536B89)) }
                            locatedWeather != null -> {
                                val weather = locatedWeather!!
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(weather.placeName, color = MiraNavy, fontSize = 22.sp, fontWeight = FontWeight.Black)
                                    Text("  ·  ${weather.info.description}", color = Color(0xFF536B89), fontSize = 14.sp)
                                    Spacer(Modifier.weight(1f))
                                    HomeWeatherIcon()
                                }
                                Spacer(Modifier.weight(1f))
                                Text("${weather.info.temperature.toInt()}°C", color = Color(0xFF102E61), fontSize = 48.sp, fontWeight = FontWeight.Black)
                                Text("湿度 ${weather.info.humidity}%  ·  风速 ${weather.info.windSpeed.toInt()} km/h", color = Color(0xFF526C8D), fontSize = 13.sp)
                            }
                            else -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = Color(0xFF4E94E8), modifier = Modifier.size(25.dp))
                                    Spacer(Modifier.width(7.dp))
                                    Text("当前位置天气", color = MiraNavy, fontSize = 20.sp, fontFamily = miraDisplayFont)
                                }
                                Spacer(Modifier.weight(1f))
                                Text(weatherError ?: "暂时无法获取天气", color = Color(0xFF536B89), fontSize = 13.sp)
                                TextButton(onClick = { weatherRefreshRequest++ }) { Text("重新获取") }
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
                    showVoiceOverlay = false
                    voiceListening = false
                },
                onComplete = {
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
private fun HomeTimerBadge() {
    Box(
        modifier = Modifier.size(62.dp).background(Color(0xFFFFD469), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(45.dp)) {
            drawCircle(Color(0xFFFFFBEC))
            val ring = Stroke(width = 5f, cap = StrokeCap.Round)
            drawArc(Color(0xFFF39A17), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = ring)
            drawCircle(Color(0xFFF39A17), radius = 3.2f, center = center)
            drawLine(Color(0xFFF39A17), center, Offset(center.x, size.height * 0.24f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(Color(0xFFF39A17), center, Offset(size.width * 0.72f, size.height * 0.62f), strokeWidth = 4f, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun HomeWeatherIcon() {
    Surface(color = Color(0xFF4C9AF0), shape = CircleShape, modifier = Modifier.size(52.dp)) {
        Box(Modifier.fillMaxSize()) {
            Icon(
                Icons.Rounded.WbSunny,
                null,
                tint = Color(0xFFFFCF32),
                modifier = Modifier.align(Alignment.TopStart).padding(start = 7.dp, top = 6.dp).size(25.dp),
            )
            Icon(
                Icons.Rounded.Cloud,
                null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 5.dp, bottom = 6.dp).size(32.dp),
            )
        }
    }
}

@Composable
private fun HomeCharacterTile(character: String, modifier: Modifier = Modifier) {
    Box(modifier.background(Color(0xFFF9FFF9), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val lineColor = Color(0xFF75D5A7).copy(alpha = 0.7f)
            drawLine(lineColor, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), 2f)
            drawLine(lineColor, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), 2f)
            drawLine(lineColor.copy(alpha = 0.35f), Offset.Zero, Offset(size.width, size.height), 1.5f)
            drawLine(lineColor.copy(alpha = 0.35f), Offset(size.width, 0f), Offset(0f, size.height), 1.5f)
        }
        Text(
            character,
            color = Color(0xFF102650),
            fontSize = 68.sp,
            lineHeight = 68.sp,
            fontFamily = miraHandFont,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HomeLessonRow(period: Int, lesson: Lesson?) {
    val colors = listOf(Color(0xFFFFE8E5), Color(0xFFE5F1FF), Color(0xFFE8F7E5), Color(0xFFFFF2CE))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (lesson != null) colors[(period - 1).mod(colors.size)] else Color(0xFFF2F5F9),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("第${period}节", color = if (lesson != null) MiraNavy else Color(0xFF8C99AC), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(62.dp))
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
private fun TimerScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val store = remember { LocalStore(context) }
    val snapshot = remember { store.loadTimer() }
    val records = remember { mutableStateListOf<FocusRecord>().also { it.addAll(store.loadFocusRecords()) } }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) Toast.makeText(context, "未开启通知权限，计时仍可使用，但后台结束时不会弹出提醒", Toast.LENGTH_LONG).show()
    }
    var taskName by rememberSaveable { mutableStateOf(snapshot.taskName) }
    var subject by rememberSaveable { mutableStateOf(snapshot.subject) }
    var expectedMinutes by rememberSaveable { mutableIntStateOf(snapshot.expectedMinutes) }
    val restoredElapsed = snapshot.elapsedSeconds + if (snapshot.running && snapshot.startedAtMillis > 0L) {
        ((System.currentTimeMillis() - snapshot.startedAtMillis) / 1000).toInt().coerceAtLeast(0)
    } else 0
    var elapsedSeconds by rememberSaveable { mutableIntStateOf(restoredElapsed) }
    var sessionBaseSeconds by rememberSaveable { mutableIntStateOf(snapshot.elapsedSeconds) }
    var running by rememberSaveable { mutableStateOf(snapshot.running) }
    var startedAtMillis by rememberSaveable {
        mutableLongStateOf(if (snapshot.running) snapshot.startedAtMillis else 0L)
    }

    LaunchedEffect(running, startedAtMillis, sessionBaseSeconds) {
        while (running) {
            elapsedSeconds = sessionBaseSeconds + ((System.currentTimeMillis() - startedAtMillis) / 1000).toInt().coerceAtLeast(0)
            delay(500)
        }
    }

    fun saveCurrent() = store.saveTimer(
        TimerSnapshot(taskName, subject, expectedMinutes, elapsedSeconds, running, startedAtMillis)
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
                    context, taskName.ifBlank { "专注作业" }, expectedMinutes,
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

    fun completeTimer() {
        val completedAt = System.currentTimeMillis()
        if (running) {
            elapsedSeconds = sessionBaseSeconds + ((completedAt - startedAtMillis) / 1000).toInt().coerceAtLeast(0)
        }
        val actualMinutes = max(1, (elapsedSeconds + 59) / 60)
        running = false
        startedAtMillis = 0L
        TimerAlarmScheduler.cancel(context)
        store.addFocusRecord(FocusRecord(completedAt, taskName.ifBlank { "专注作业" }, subject, actualMinutes, completedAt))
        records.clear()
        records.addAll(store.loadFocusRecords())
        elapsedSeconds = 0
        sessionBaseSeconds = 0
        saveCurrent()
    }

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.weight(0.93f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(0.47f)) {
                Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 17.dp)) {
                    SectionTitle("📖", "本次作业", Color(0xFF2B75D6))
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        singleLine = true,
                        trailingIcon = {
                            androidx.compose.material3.IconButton(onClick = { taskName = "" }) {
                                Icon(Icons.Rounded.Close, "清空", tint = Color(0xFF74839A))
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFAFCFF),
                            unfocusedContainerColor = Color(0xFFFAFCFF),
                            focusedBorderColor = Color(0xFFCBD9E9),
                            unfocusedBorderColor = Color(0xFFD9E1EB),
                        ),
                    )
                    Spacer(Modifier.height(9.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        homeworkSubjects.forEach { item ->
                            FilterChip(
                                selected = subject == item,
                                onClick = {
                                    val oldSubject = subject
                                    subject = item
                                    if (taskName.isBlank() || taskName == "${oldSubject}作业") taskName = "${item}作业"
                                    saveCurrent()
                                },
                                label = { Text(item, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFFE8EDF3))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AccessTime, null, tint = Color(0xFF173C72), modifier = Modifier.size(25.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("预计 ", color = Color(0xFF29476E), fontWeight = FontWeight.Bold)
                        Text("$expectedMinutes", color = Color(0xFF3A87F5), fontSize = 27.sp, fontWeight = FontWeight.Black)
                        Text(" 分钟", color = Color(0xFF29476E), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onOpenSettings) {
                            Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("预计时间设置")
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, modifier = Modifier.size(19.dp))
                        }
                    }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(0.53f)) {
                TimerWeeklyChart(records = records)
            }
        }

        Column(Modifier.weight(1.18f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(0.62f)) {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color.White, Color(0xFFFBFDFF))))) {
                    Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFD339), modifier = Modifier.align(Alignment.TopStart).padding(24.dp).size(37.dp))
                    Surface(
                        color = Color(0xFFFFF5CB), shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).rotate(-6f),
                    ) {
                        Text("专注\n让成长看得见！", Modifier.padding(horizontal = 13.dp, vertical = 8.dp), color = Color(0xFF39516F), fontFamily = miraHandFont, fontSize = 15.sp)
                    }
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Canvas(Modifier.size(266.dp)) {
                                val stroke = Stroke(width = 18f, cap = StrokeCap.Round)
                                drawArc(Color(0xFFE8EEF5), -225f, 270f, false, style = stroke)
                                val progress = min(1f, elapsedSeconds / (expectedMinutes * 60f))
                                if (progress > 0f) drawArc(Color(0xFF4B91F2), -225f, 270f * progress, false, style = stroke)
                                drawCircle(Color(0xFFA8BDD7), 9f, Offset(size.width * 0.25f, size.height * 0.09f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(formatTimer(elapsedSeconds), color = Color(0xFF082C63), fontSize = 65.sp, fontWeight = FontWeight.Black)
                                Text(if (running) "正在专注" else "从零开始", color = Color(0xFF71809A), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { toggleTimer() },
                                modifier = Modifier.weight(1.2f).height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFAF08)),
                            ) {
                                Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (running) "暂停" else "开始", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(onClick = { resetTimer() }, modifier = Modifier.weight(0.9f).height(56.dp), shape = RoundedCornerShape(28.dp)) {
                                Icon(Icons.Rounded.Refresh, null)
                                Spacer(Modifier.width(5.dp))
                                Text("重置", fontSize = 17.sp)
                            }
                            OutlinedButton(
                                onClick = { completeTimer() },
                                modifier = Modifier.weight(0.9f).height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF159565)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF159565)),
                            ) {
                                Icon(Icons.Rounded.Check, null)
                                Spacer(Modifier.width(4.dp))
                                Text("完成", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(0.38f)) {
                TimerMotivationBanner()
            }
        }
    }
}

@Composable
private fun TimerWeeklyChart(records: List<FocusRecord>) {
    var bySubject by rememberSaveable { mutableStateOf(false) }
    val weekStart = remember { LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val weekRecords = records.filter { record ->
        val date = Instant.ofEpochMilli(record.completedAtMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        !date.isBefore(weekStart) && !date.isAfter(weekStart.plusDays(6))
    }
    val rows = if (bySubject) {
        homeworkSubjects.map { item -> item to weekRecords.filter { it.subject == item }.sumOf { it.durationMinutes } }
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
    val colors = listOf(
        Color(0xFF5A98F5), Color(0xFF64CEB0), Color(0xFFFFD852), Color(0xFFFF8175),
        Color(0xFF9B72E8), Color(0xFF45ADEB), Color(0xFF8BD15B),
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 17.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("▥", color = Color(0xFF39C3B1), fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(9.dp))
            Text("本周作业时长", color = MiraNavy, fontSize = 21.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
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
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            rows.forEachIndexed { index, (label, minutes) ->
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.weight(1f))
                    Text(if (minutes == 0) "" else minutes.toString(), color = Color(0xFF41587A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(3.dp))
                    Box(Modifier.height(112.dp).width(30.dp), contentAlignment = Alignment.BottomCenter) {
                        Box(
                            Modifier.fillMaxWidth().height(
                                if (minutes == 0) 3.dp else (18f + 94f * minutes / maximum.toFloat()).dp,
                            ).background(colors[index % colors.size], RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(label, color = Color(0xFF526A88), fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun TimerMotivationBanner() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFFFFF5CD), Color(0xFFE6F8FF), Color(0xFFD8F3E5))),
        ),
    ) {
        Image(
            painterResource(R.drawable.mira_green_hills),
            contentDescription = null,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(74.dp).alpha(0.55f),
            contentScale = ContentScale.FillBounds,
        )
        Surface(
            color = Color(0xFFFFF6D6),
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
) {
    val context = LocalContext.current
    val store = remember { LocalStore(context) }
    var expectedMinutes by rememberSaveable { mutableIntStateOf(store.loadExpectedMinutes()) }
    var weatherRefreshMinutes by rememberSaveable { mutableIntStateOf(store.loadWeatherRefreshMinutes()) }

    fun updateExpected(minutes: Int) {
        expectedMinutes = minutes.coerceIn(1, 240)
        store.saveExpectedMinutes(expectedMinutes)
        val timer = store.loadTimer()
        if (!timer.running && timer.elapsedSeconds == 0) store.saveTimer(timer.copy(expectedMinutes = expectedMinutes))
    }

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        MiraCard(Modifier.weight(1.05f).fillMaxHeight()) {
            Column(
                Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFFBF2), Color.White))).padding(30.dp),
            ) {
                SectionTitle("⏰", "计时设置", Color(0xFFFF9F1C))
                Spacer(Modifier.height(18.dp))
                Text("默认预计完成时间", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text("到达预计时间会提醒，计时不会停止。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(20, 30, 35, 45, 60, 90).forEach { minutes ->
                        FilterChip(selected = expectedMinutes == minutes, onClick = { updateExpected(minutes) }, label = { Text("${minutes}分") })
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    FilledTonalButton(onClick = { updateExpected(expectedMinutes - 5) }, shape = CircleShape) { Text("−", fontSize = 26.sp) }
                    Surface(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(22.dp),
                        shadowElevation = 2.dp,
                    ) {
                        Text("$expectedMinutes 分钟", Modifier.padding(horizontal = 30.dp, vertical = 18.dp), style = MaterialTheme.typography.headlineLarge, color = MiraBlue)
                    }
                    FilledTonalButton(onClick = { updateExpected(expectedMinutes + 5) }, shape = CircleShape) { Text("＋", fontSize = 24.sp) }
                }
                HorizontalDivider(Modifier.padding(vertical = 22.dp), color = Color(0xFFE7EAF0))
                SectionTitle("🌤️", "天气刷新间隔", Color(0xFF4A9BE8))
                Spacer(Modifier.height(7.dp))
                Text("首页优先显示缓存天气，到达间隔后才重新定位获取。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(15 to "15分", 30 to "30分", 60 to "1小时", 180 to "3小时").forEach { (minutes, label) ->
                        FilterChip(
                            selected = weatherRefreshMinutes == minutes,
                            onClick = {
                                weatherRefreshMinutes = minutes
                                store.saveWeatherRefreshMinutes(minutes)
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Surface(color = Color(0xFFFFF1C9), shape = RoundedCornerShape(18.dp)) {
                    Text("🔔 到达时间会提醒，完成后记得点击“完成”", Modifier.fillMaxWidth().padding(16.dp), color = Color(0xFF7C5413))
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MiraCard(Modifier.fillMaxWidth().weight(1.2f)) {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    SectionTitle("🎨", "菜单栏风格", Color(0xFF7B72E9))
                    Spacer(Modifier.height(12.dp))
                    Text("点击即可实时预览并自动保存", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    NavigationStyle.entries.chunked(2).forEach { rowStyles ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowStyles.forEach { style ->
                                Surface(
                                    onClick = { onNavigationStyleChanged(style) },
                                    modifier = Modifier.weight(1f).padding(vertical = 3.dp),
                                    color = if (navigationStyle == style) Color(0xFFEAF2FF) else Color(0xFFF7F9FC),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        NavigationStylePreview(style)
                                        Spacer(Modifier.width(8.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(style.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(style.description.substringBefore('，'), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                        }
                                        Text(if (navigationStyle == style) "✓" else "", color = MiraBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            MiraCard(Modifier.fillMaxWidth().weight(0.8f)) {
                Box(Modifier.fillMaxSize().background(Color(0xFFF1FBF5))) {
                    Image(
                        painterResource(R.drawable.mira_green_hills),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(90.dp).alpha(0.42f),
                        contentScale = ContentScale.FillBounds,
                    )
                    Column(Modifier.fillMaxSize().padding(24.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            SectionTitle("📖", "字典数据", MiraGreen)
                            Spacer(Modifier.weight(1f))
                            Text("✓ 内置离线 · 可用", color = MiraGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("约 3500 个常用汉字", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        Text("✓ 释义与组词已内置     ✓ 笔顺动画已内置", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        Text("所有学习记录仅保存在本机", color = MiraGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationStylePreview(style: NavigationStyle) {
    val background = when (style) {
        NavigationStyle.FRESH -> MiraSky
        NavigationStyle.COLORFUL -> Color(0xFFFFE9B8)
        NavigationStyle.JOURNAL -> Color(0xFFFFF0C7)
        NavigationStyle.MINIMAL -> Color.White
        NavigationStyle.STATIONERY -> Color(0xFF438FF4)
    }
    val accent = when (style) {
        NavigationStyle.COLORFUL -> screenAccent(AppScreen.HOME)
        NavigationStyle.JOURNAL -> MiraGreen
        NavigationStyle.STATIONERY -> Color(0xFF438FF4)
        else -> MiraBlue
    }
    val contentColor = if (style == NavigationStyle.FRESH || style == NavigationStyle.STATIONERY) Color.White else accent
    Surface(color = background, shape = RoundedCornerShape(12.dp), shadowElevation = if (style == NavigationStyle.MINIMAL) 2.dp else 0.dp) {
        Box(Modifier.padding(horizontal = 10.dp, vertical = 7.dp).size(24.dp), contentAlignment = Alignment.Center) {
            NavigationStyleIcon(style, AppScreen.HOME, selected = true, accent = accent, contentColor = contentColor)
        }
    }
}

@Composable
private fun DictionaryScreen(initialQuery: String = "") {
    val context = LocalContext.current
    val repository = remember { DictionaryRepository(context.applicationContext) }
    var query by rememberSaveable { mutableStateOf("") }
    var entry by remember { mutableStateOf<DictionaryEntry?>(null) }
    var results by remember { mutableStateOf<List<DictionaryEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var listening by remember { mutableStateOf(false) }
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) query = initialQuery
    }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        listening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                val extracted = extractDictionaryQuery(spoken)
                query = extracted
                Toast.makeText(context, "听到：$spoken\n正在查：$extracted", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val microphonePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            listening = true
            runCatching { speechLauncher.launch(dictionarySpeechIntent()) }.onFailure {
                listening = false
                Toast.makeText(context, "系统语音识别暂不可用", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "需要麦克风权限才能语音查字", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(query) {
        delay(120)
        loading = true
        val found = withContext(Dispatchers.IO) { repository.search(query) }
        results = found
        entry = found.firstOrNull()
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("内置字典：输入汉字、拼音或词语") },
                leadingIcon = { Text("⌕", fontSize = 26.sp) },
                trailingIcon = {
                    Text(if (loading) "查询中" else "${results.size} 条", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        listening = true
                        runCatching { speechLauncher.launch(dictionarySpeechIntent()) }.onFailure {
                            listening = false
                            Toast.makeText(context, "系统语音识别暂不可用", Toast.LENGTH_SHORT).show()
                        }
                    } else microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                },
                enabled = !listening,
                modifier = Modifier.height(56.dp),
            ) { Text(if (listening) "正在听…" else "🎤 语音查字") }
        }
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
    var visibleStrokes by remember(character, parsedPaths) { mutableIntStateOf(0) }
    var playing by remember(character) { mutableStateOf(false) }
    var replay by remember(character) { mutableIntStateOf(0) }

    LaunchedEffect(character, parsedPaths, replay) {
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
            Text(character, fontSize = 142.sp, fontWeight = FontWeight.Medium, color = MiraNavy, modifier = Modifier.align(Alignment.Center))
        }
        FilledTonalButton(
            onClick = { replay++ },
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
    val dayLessons = lessons.filter { it.day == selectedDay }.sortedBy { it.period }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                weekdays.forEachIndexed { index, day -> FilterChip(selected = selectedDay == index, onClick = { selectedDay = index }, label = { Text(day) }) }
            }
            Button(onClick = { showAdd = true }) { Text("＋ 添加课程") }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MiraCard(Modifier.weight(1.55f).fillMaxHeight()) {
                Column(Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState())) {
                    SectionTitle("📅", "${weekdays[selectedDay]}课程", MiraOrange)
                    Spacer(Modifier.height(18.dp))
                    if (dayLessons.isEmpty()) {
                        Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { Text("今天还没有课程，可以点击右上角添加。") }
                    }
                    dayLessons.forEach { lesson ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) { LessonRow(lesson) }
                            TextButton(onClick = { editingLesson = lesson }) { Text("编辑") }
                            TextButton(onClick = { deletingLesson = lesson }) { Text("删除") }
                        }
                    }
                }
            }
            Column(Modifier.weight(0.7f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MiraCard(Modifier.fillMaxWidth().weight(0.9f)) {
                    Column(
                        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF3C9), Color.White))).padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(0.8f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${weekdays[selectedDay]}", style = MaterialTheme.typography.titleLarge)
                                Text("${dayLessons.size}", style = MaterialTheme.typography.displayLarge, color = Color(0xFFFF8A24), fontSize = 66.sp)
                                Text("节课", style = MaterialTheme.typography.headlineMedium)
                            }
                            Image(
                                painterResource(R.drawable.mira_backpack_books),
                                contentDescription = null,
                                modifier = Modifier.weight(1.2f).height(138.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                        Text("认真上好每一节课", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
                MiraCard(Modifier.fillMaxWidth().weight(1.1f)) {
                    Column(Modifier.fillMaxSize().background(Color(0xFFF4FAFF)).padding(24.dp)) {
                        SectionTitle("💡", "课前小提醒", Color(0xFFFFB21E))
                        Spacer(Modifier.height(18.dp))
                        listOf("带好课本和文具", "课前准备好学习用品", "认真听讲，积极思考").forEach {
                            Text("✅  $it", Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.weight(1f)); Text("每一节课，都是成长的机会！", color = MiraBlue)
                    }
                }
            }
        }
    }

    if (showAdd) {
        LessonDialog(day = selectedDay, initial = null, onDismiss = { showAdd = false }, onSave = { lesson ->
            val conflict = lessons.any { it.day == lesson.day && it.period == lesson.period }
            if (conflict) Toast.makeText(context, "第 ${lesson.period} 节已经有课程了", Toast.LENGTH_SHORT).show()
            else { lessons.add(lesson); store.saveLessons(lessons); showAdd = false }
        })
    }
    editingLesson?.let { original ->
        LessonDialog(day = original.day, initial = original, onDismiss = { editingLesson = null }, onSave = { updated ->
            val conflict = lessons.any { it != original && it.day == updated.day && it.period == updated.period }
            if (conflict) Toast.makeText(context, "第 ${updated.period} 节已经有课程了", Toast.LENGTH_SHORT).show()
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
            text = { Text("确定删除“第 ${lesson.period} 节 ${lesson.name}”吗？") },
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
private fun LessonRow(lesson: Lesson) {
    val colors = listOf(Color(0xFFFFE5E3), Color(0xFFE5F2FF), Color(0xFFE8F7E8), Color(0xFFFFF0C9), Color(0xFFF0E9FF), Color(0xFFE1F7F3))
    val icon = when {
        lesson.name.contains("语文") -> "📖"
        lesson.name.contains("数学") -> "📐"
        lesson.name.contains("英语") -> "ABC"
        lesson.name.contains("体育") -> "⚽"
        lesson.name.contains("美术") -> "🎨"
        lesson.name.contains("音乐") -> "♫"
        lesson.name.contains("科学") -> "🔬"
        else -> "✦"
    }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        color = colors[(lesson.period - 1).mod(colors.size)],
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("第${lesson.period}节", fontWeight = FontWeight.Bold, color = MiraNavy, modifier = Modifier.width(64.dp))
            Text(icon, fontSize = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.width(52.dp))
            Text(lesson.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("按节次", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun LessonDialog(day: Int, initial: Lesson?, onDismiss: () -> Unit, onSave: (Lesson) -> Unit) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var period by remember(initial) { mutableIntStateOf(initial?.period ?: 1) }
    val canSave = name.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "添加课程" else "编辑课程") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("课程名称") }, singleLine = true)
                Text("选择节次", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..12).forEach { number ->
                        FilterChip(selected = period == number, onClick = { period = number }, label = { Text("第${number}节") })
                    }
                }
                Text("课程只按节次排列，不需要填写具体时间。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(enabled = canSave, onClick = { onSave(Lesson(day, name.trim(), period)) }) { Text("保存") } },
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
                    if (cityIndex == 0) {
                        Image(
                            painterResource(R.drawable.mira_shanghai_weather),
                            contentDescription = "上海城市天气插画",
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Text(info?.symbol ?: "🌤️", fontSize = 118.sp)
                    }
                    Spacer(Modifier.height(12.dp))
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
