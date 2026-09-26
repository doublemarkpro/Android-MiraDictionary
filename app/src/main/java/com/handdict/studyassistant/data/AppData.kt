package com.handdict.studyassistant.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import org.json.JSONArray
import com.handdict.studyassistant.learning.BORROWING_DAILY_LIMIT
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import java.time.LocalDate
import java.util.Locale
import kotlin.coroutines.resume

data class DictionaryEntry(
    val word: String,
    val pinyin: String,
    val radical: String,
    val strokes: Int,
    val structure: String,
    val meanings: List<String>,
    val phrases: List<String>,
    val related: List<String>,
    val strokePaths: List<String> = emptyList(),
)

object MiniDictionary {
    val entries = listOf(
        DictionaryEntry("规", "guī", "见", 8, "左右结构", listOf("法则；章程；标准。", "劝告；使改正。"), listOf("规则", "规定", "规范", "规矩", "常规"), listOf("观", "现", "视", "觉")),
        DictionaryEntry("学", "xué", "子", 8, "上下结构", listOf("学习；钻研知识。", "学问；知识。"), listOf("学习", "学校", "学生", "数学", "好学"), listOf("字", "教", "校", "习")),
        DictionaryEntry("习", "xí", "乙", 3, "独体结构", listOf("学习；复习。", "对某事熟练或常常接触。"), listOf("学习", "练习", "复习", "习惯", "自习"), listOf("学", "羽", "练", "复")),
        DictionaryEntry("明", "míng", "日", 8, "左右结构", listOf("明亮；光线充足。", "明白；清楚。"), listOf("明天", "明白", "光明", "说明", "聪明"), listOf("日", "月", "亮", "暗")),
        DictionaryEntry("山", "shān", "山", 3, "独体结构", listOf("地面上由土石构成的高起部分。"), listOf("高山", "山水", "山峰", "上山"), listOf("岳", "峰", "岭", "岩")),
        DictionaryEntry("水", "shuǐ", "水", 4, "独体结构", listOf("无色、无味、透明的液体。", "江、河、湖、海的通称。"), listOf("水果", "开水", "河水", "山水"), listOf("河", "海", "湖", "泉")),
        DictionaryEntry("天", "tiān", "大", 4, "独体结构", listOf("天空。", "一昼夜；一天。", "自然的；生来的。"), listOf("天空", "天气", "今天", "明天", "天然"), listOf("空", "日", "云", "气")),
        DictionaryEntry("人", "rén", "人", 2, "独体结构", listOf("能制造和使用工具进行劳动的高等动物。", "别人；他人。"), listOf("人民", "大人", "主人", "人才"), listOf("众", "你", "我", "他")),
        DictionaryEntry("书", "shū", "乛", 4, "独体结构", listOf("装订成册的著作。", "写字；记录。"), listOf("书本", "读书", "图书", "书写"), listOf("本", "册", "读", "写")),
        DictionaryEntry("好", "hǎo", "女", 6, "左右结构", listOf("优点多的；使人满意的。", "友爱；和睦。"), listOf("好人", "美好", "好学", "友好"), listOf("美", "善", "友", "妙")),
    )

    fun search(query: String): List<DictionaryEntry> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return entries
        return entries.filter { entry ->
            entry.word.contains(normalized, ignoreCase = true) ||
                entry.pinyin.contains(normalized, ignoreCase = true) ||
                entry.phrases.any { it.contains(normalized, ignoreCase = true) }
        }
    }
}

class DictionaryRepository(private val context: Context) {
    private val database: SQLiteDatabase by lazy {
        val target = File(context.noBackupFilesDir, "mira-dictionary-v3.db")
        if (!target.exists()) {
            context.assets.open("dictionary.db").use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
        SQLiteDatabase.openDatabase(target.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    }

    fun search(query: String, limit: Int = 20): List<DictionaryEntry> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return listOfNotNull(findExact("智"))
        val numberedPinyin = parseNumberedPinyin(trimmed)
        val normalizedPinyin = numberedPinyin?.first ?: normalizePinyin(trimmed)
        val databaseLimit = if (numberedPinyin != null) 300 else limit
        val sql = """
            SELECT character, pinyin, radical, stroke_count, structure,
                   frequency, meanings, phrases, related, stroke_paths
            FROM characters
            WHERE character = ? OR pinyin_search LIKE ? OR phrases LIKE ?
            ORDER BY CASE
                WHEN character = ? THEN 0
                WHEN pinyin_search = ? THEN 1
                WHEN pinyin_search LIKE ? THEN 2
                ELSE 3
            END, frequency, stroke_count, character
            LIMIT ?
        """.trimIndent()
        val args = arrayOf(
            trimmed,
            "%$normalizedPinyin%",
            "%$trimmed%",
            trimmed,
            normalizedPinyin,
            "$normalizedPinyin%",
            databaseLimit.toString(),
        )
        val matches = database.rawQuery(sql, args).use { cursor -> cursor.toEntries() }
        return if (numberedPinyin == null) {
            matches
        } else {
            val (syllable, tone) = numberedPinyin
            matches.filter { entry -> entry.pinyin.matchesTone(syllable, tone) }.take(limit)
        }
    }

    fun findExact(character: String): DictionaryEntry? {
        val sql = """
            SELECT character, pinyin, radical, stroke_count, structure,
                   frequency, meanings, phrases, related, stroke_paths
            FROM characters WHERE character = ? LIMIT 1
        """.trimIndent()
        return database.rawQuery(sql, arrayOf(character)).use { cursor ->
            if (cursor.moveToFirst()) cursor.toEntry() else null
        }
    }

    private fun Cursor.toEntries(): List<DictionaryEntry> {
        val result = mutableListOf<DictionaryEntry>()
        while (moveToNext()) result += toEntry()
        return result
    }

    private fun Cursor.toEntry() = DictionaryEntry(
        word = getString(0),
        pinyin = getString(1),
        radical = getString(2),
        strokes = getInt(3),
        structure = getString(4),
        meanings = getString(6).toStringList(),
        phrases = getString(7).toStringList(),
        related = getString(8).toStringList(),
        strokePaths = getString(9).toStringList(),
    )

    private fun String.toStringList(): List<String> = runCatching {
        val array = JSONArray(this)
        List(array.length()) { index -> array.getString(index) }
    }.getOrDefault(emptyList())

    private fun normalizePinyin(value: String): String {
        val decomposed = Normalizer.normalize(value.replace('ɡ', 'g').lowercase(), Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{M}+"), "").replace('ü', 'v')
    }

    private fun parseNumberedPinyin(value: String): Pair<String, Int>? {
        val match = Regex("^([a-zA-ZüÜvV]+)([1-5])$").matchEntire(value.trim()) ?: return null
        return normalizePinyin(match.groupValues[1]) to match.groupValues[2].toInt()
    }

    private fun String.matchesTone(expectedSyllable: String, expectedTone: Int): Boolean =
        split(Regex("[\\s/,;·・]+")).any { rawSyllable ->
            normalizePinyin(rawSyllable) == expectedSyllable && pinyinTone(rawSyllable) == expectedTone
        }

    private fun pinyinTone(value: String): Int {
        val marked = value.lowercase()
        return when {
            marked.any { it in "āēīōūǖ" } -> 1
            marked.any { it in "áéíóúǘ" } -> 2
            marked.any { it in "ǎěǐǒǔǚ" } -> 3
            marked.any { it in "àèìòùǜ" } -> 4
            else -> 5
        }
    }
}

data class Lesson(val day: Int, val name: String, val period: Int)

data class TimerSnapshot(
    val taskName: String = "语文作业",
    val subject: String = "语文",
    val expectedMinutes: Int = 35,
    val elapsedSeconds: Int = 0,
    val running: Boolean = false,
    val startedAtMillis: Long = 0L,
)

data class FocusRecord(
    val id: Long,
    val taskName: String,
    val subject: String,
    val durationMinutes: Int,
    val completedAtMillis: Long,
)

val defaultHomeworkSubjects = listOf("语文", "数学", "英语")
val homeworkSubjects = listOf("语文", "数学", "英语", "科学", "其他")
const val FOCUS_SUBJECT_MAX_MINUTES = 60
const val DEFAULT_DICTIONARY_DAILY_LIMIT = 20

fun maximumExpectedMinutes(subject: String): Int =
    if (subject in defaultHomeworkSubjects) FOCUS_SUBJECT_MAX_MINUTES else 99

fun inferSubject(taskName: String): String = homeworkSubjects
    .dropLast(1)
    .firstOrNull { taskName.contains(it) }
    ?: "其他"

class LocalStore(context: Context) {
    private val preferences = context.getSharedPreferences("mira_study", Context.MODE_PRIVATE)

    fun loadLessons(): List<Lesson> {
        val stored = preferences.getString("lessons", null) ?: return defaultLessons
        return stored.lineSequence().mapNotNull { line ->
            val fields = line.split('|')
            when (fields.size) {
                3 -> Lesson(fields[0].toIntOrNull() ?: 0, fields[1], fields[2].toIntOrNull()?.coerceIn(1, 12) ?: 1)
                4 -> Lesson(fields[0].toIntOrNull() ?: 0, fields[1], legacyPeriod(fields[2]))
                else -> null
            }
        }.toList().ifEmpty { defaultLessons }
    }

    fun saveLessons(lessons: List<Lesson>) {
        val encoded = lessons.joinToString("\n") { "${it.day}|${it.name}|${it.period}" }
        preferences.edit { putString("lessons", encoded) }
    }

    fun loadTimer(): TimerSnapshot {
        val taskName = preferences.getString("timer_name", "语文作业") ?: "语文作业"
        val subject = preferences.getString("timer_subject", null) ?: inferSubject(taskName)
        return TimerSnapshot(
            taskName = taskName,
            subject = subject,
            expectedMinutes = preferences.getInt("timer_expected", loadExpectedMinutes())
                .coerceIn(10, maximumExpectedMinutes(subject)),
            elapsedSeconds = preferences.getInt("timer_elapsed", 0).coerceAtLeast(0),
            running = preferences.getBoolean("timer_running", false),
            startedAtMillis = preferences.getLong("timer_started", 0L),
        )
    }

    fun saveTimer(snapshot: TimerSnapshot) {
        preferences.edit {
            putString("timer_name", snapshot.taskName)
            putString("timer_subject", snapshot.subject)
            putInt("timer_expected", snapshot.expectedMinutes.coerceIn(10, maximumExpectedMinutes(snapshot.subject)))
            putInt("timer_elapsed", snapshot.elapsedSeconds)
            putBoolean("timer_running", snapshot.running)
            putLong("timer_started", snapshot.startedAtMillis)
        }
    }

    fun loadExpectedMinutes(): Int = preferences.getInt("default_expected_minutes", 35).coerceIn(1, 240)

    fun saveExpectedMinutes(minutes: Int) {
        preferences.edit { putInt("default_expected_minutes", minutes.coerceIn(1, 240)) }
    }

    fun loadExpectedMinutes(subject: String): Int {
        val key = "expected_minutes_${subject.trim()}"
        return preferences.getInt(key, loadExpectedMinutes()).coerceIn(10, maximumExpectedMinutes(subject))
    }

    fun saveExpectedMinutes(subject: String, minutes: Int) {
        val cleanedSubject = subject.trim()
        if (cleanedSubject.isBlank()) return
        preferences.edit {
            putInt("expected_minutes_$cleanedSubject", minutes.coerceIn(10, maximumExpectedMinutes(cleanedSubject)))
        }
    }

    fun isFocusModeActive(): Boolean = preferences.getBoolean("focus_mode_active", false)

    fun saveFocusModeActive(active: Boolean) {
        preferences.edit { putBoolean("focus_mode_active", active) }
    }

    fun loadDictionaryDailyLimit(): Int {
        val today = LocalDate.now().toString()
        if (preferences.getString("dictionary_limit_date", null) != today) return DEFAULT_DICTIONARY_DAILY_LIMIT
        return preferences.getInt("dictionary_daily_limit", DEFAULT_DICTIONARY_DAILY_LIMIT).coerceIn(1, 200)
    }

    fun saveDictionaryDailyLimit(limit: Int) {
        preferences.edit {
            putString("dictionary_limit_date", LocalDate.now().toString())
            putInt("dictionary_daily_limit", limit.coerceIn(1, 200))
        }
    }

    fun loadDictionaryLookupWords(): Set<String> {
        if (preferences.getString("dictionary_usage_date", null) != LocalDate.now().toString()) return emptySet()
        val raw = preferences.getString("dictionary_lookup_words", null) ?: return emptySet()
        return runCatching {
            val array = JSONArray(raw)
            buildSet { repeat(array.length()) { add(array.getString(it)) } }
        }.getOrDefault(emptySet())
    }

    @Synchronized
    fun recordDictionaryLookup(word: String): Boolean {
        val normalized = word.trim()
        if (normalized.isBlank()) return true
        val words = loadDictionaryLookupWords().toMutableSet()
        if (normalized in words) return true
        if (words.size >= loadDictionaryDailyLimit()) return false
        words += normalized
        val array = JSONArray()
        words.sorted().forEach { array.put(it) }
        preferences.edit {
            putString("dictionary_usage_date", LocalDate.now().toString())
            putString("dictionary_lookup_words", array.toString())
        }
        return true
    }

    fun loadBorrowingCompletedToday(): Int {
        if (preferences.getString("borrowing_progress_date", null) != LocalDate.now().toString()) return 0
        return preferences.getInt("borrowing_progress_count", 0).coerceIn(0, BORROWING_DAILY_LIMIT)
    }

    @Synchronized
    fun recordBorrowingCompletion(): Int {
        val today = LocalDate.now().toString()
        val current = if (preferences.getString("borrowing_progress_date", null) == today) {
            preferences.getInt("borrowing_progress_count", 0)
        } else {
            0
        }.coerceIn(0, BORROWING_DAILY_LIMIT)
        val updated = (current + 1).coerceAtMost(BORROWING_DAILY_LIMIT)
        preferences.edit {
            putString("borrowing_progress_date", today)
            putInt("borrowing_progress_count", updated)
        }
        return updated
    }

    fun loadNavigationStyle(): String = preferences.getString("navigation_style", "fresh") ?: "fresh"

    fun saveNavigationStyle(style: String) {
        preferences.edit { putString("navigation_style", style) }
    }

    fun loadVoiceWakeEnabled(): Boolean = preferences.getBoolean("voice_wake_enabled", false)

    fun saveVoiceWakeEnabled(enabled: Boolean) {
        preferences.edit { putBoolean("voice_wake_enabled", enabled) }
    }

    fun loadHomeworkSubjects(): List<String> {
        val stored = preferences.getString("homework_subjects", null)
            ?.split('|')
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            .orEmpty()
        return stored.ifEmpty { defaultHomeworkSubjects }
    }

    fun saveHomeworkSubjects(subjects: List<String>) {
        val cleaned = subjects.map { it.replace("|", "").trim().take(8) }.filter { it.isNotBlank() }.distinct().take(9)
        preferences.edit { putString("homework_subjects", cleaned.ifEmpty { defaultHomeworkSubjects }.joinToString("|")) }
    }

    fun loadWeatherRefreshMinutes(): Int =
        preferences.getInt("weather_refresh_minutes", 60).coerceIn(15, 720)

    fun saveWeatherRefreshMinutes(minutes: Int) {
        preferences.edit { putInt("weather_refresh_minutes", minutes.coerceIn(15, 720)) }
    }

    fun loadWeatherCache(): WeatherCache? {
        val updatedAt = preferences.getLong("weather_updated_at", 0L)
        val place = preferences.getString("weather_place", null) ?: return null
        if (updatedAt <= 0L || !preferences.contains("weather_temperature")) return null
        return WeatherCache(
            weather = LocatedWeather(
                placeName = place,
                info = WeatherInfo(
                    temperature = preferences.getFloat("weather_temperature", 0f).toDouble(),
                    humidity = preferences.getInt("weather_humidity", 0),
                    windSpeed = preferences.getFloat("weather_wind_speed", 0f).toDouble(),
                    weatherCode = preferences.getInt("weather_code", 0),
                    apparentTemperature = preferences.getFloat(
                        "weather_apparent_temperature",
                        preferences.getFloat("weather_temperature", 0f),
                    ).toDouble(),
                    precipitation = preferences.getFloat("weather_precipitation", 0f).toDouble(),
                ),
            ),
            updatedAtMillis = updatedAt,
        )
    }

    fun saveWeatherCache(cache: WeatherCache) {
        preferences.edit {
            putString("weather_place", cache.weather.placeName)
            putFloat("weather_temperature", cache.weather.info.temperature.toFloat())
            putInt("weather_humidity", cache.weather.info.humidity)
            putFloat("weather_wind_speed", cache.weather.info.windSpeed.toFloat())
            putInt("weather_code", cache.weather.info.weatherCode)
            putFloat("weather_apparent_temperature", cache.weather.info.apparentTemperature.toFloat())
            putFloat("weather_precipitation", cache.weather.info.precipitation.toFloat())
            putLong("weather_updated_at", cache.updatedAtMillis)
        }
    }

    fun loadManualWeatherCities(): List<WeatherCity> {
        val raw = preferences.getString("manual_weather_cities", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                WeatherCity(item.getString("name"), item.getDouble("latitude"), item.getDouble("longitude"))
            }
        }.getOrDefault(emptyList())
    }

    fun saveManualWeatherCities(cities: List<WeatherCity>) {
        val array = JSONArray()
        cities.distinctBy { it.name }.take(8).forEach { city ->
            array.put(
                JSONObject()
                    .put("name", city.name)
                    .put("latitude", city.latitude)
                    .put("longitude", city.longitude)
            )
        }
        preferences.edit { putString("manual_weather_cities", array.toString()) }
    }

    fun loadFocusRecords(): List<FocusRecord> {
        val raw = preferences.getString("focus_records", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                FocusRecord(
                    id = item.getLong("id"),
                    taskName = item.getString("task"),
                    subject = item.optString("subject").ifBlank { inferSubject(item.getString("task")) },
                    durationMinutes = item.getInt("duration"),
                    completedAtMillis = item.getLong("completedAt"),
                )
            }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun addFocusRecord(record: FocusRecord) {
        val records = (loadFocusRecords() + record)
            .distinctBy { it.id }
            .sortedByDescending { it.completedAtMillis }
            .take(500)
        val array = JSONArray()
        records.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("task", item.taskName)
                    .put("subject", item.subject)
                    .put("duration", item.durationMinutes)
                    .put("completedAt", item.completedAtMillis)
            )
        }
        preferences.edit { putString("focus_records", array.toString()) }
    }

    fun clearFocusRecords() {
        preferences.edit { remove("focus_records") }
    }

    companion object {
        private fun legacyPeriod(start: String): Int = when (start) {
            "08:00" -> 1; "09:00" -> 2; "10:00" -> 3
            "11:00" -> 4; "14:00" -> 5; "15:00" -> 6
            else -> 1
        }

        val defaultLessons = listOf(
            Lesson(0, "语文", 1), Lesson(0, "数学", 2), Lesson(0, "英语", 3), Lesson(0, "体育", 5),
            Lesson(1, "数学", 1), Lesson(1, "科学", 2), Lesson(1, "美术", 5),
            Lesson(2, "语文", 1), Lesson(2, "英语", 2), Lesson(2, "音乐", 5),
            Lesson(3, "数学", 1), Lesson(3, "信息技术", 3),
            Lesson(4, "语文", 1), Lesson(4, "班会", 6),
        )
    }
}

data class WeatherCity(val name: String, val latitude: Double, val longitude: Double)

val weatherCities = listOf(
    WeatherCity("上海", 31.2304, 121.4737), WeatherCity("北京", 39.9042, 116.4074),
    WeatherCity("青岛", 36.0671, 120.3826), WeatherCity("深圳", 22.5431, 114.0579),
    WeatherCity("成都", 30.5728, 104.0668),
)

data class LocatedWeather(val placeName: String, val info: WeatherInfo)

data class WeatherCache(val weather: LocatedWeather, val updatedAtMillis: Long)

object DeviceWeatherRepository {
    @SuppressLint("MissingPermission")
    suspend fun locate(context: Context): WeatherCity {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        check(hasFine || hasCoarse) { "需要定位权限" }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        check(providers.isNotEmpty()) { "请先开启设备定位服务" }

        val cached = providers.mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
        val location = if (cached != null && System.currentTimeMillis() - cached.time < 30 * 60 * 1000L) {
            cached
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            currentLocation(manager, providers.first()) ?: cached
        } else cached
        checkNotNull(location) { "暂时无法获取当前位置，请稍后重试" }

        @Suppress("DEPRECATION")
        val address = runCatching {
            Geocoder(context, Locale.CHINA).getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
        }.getOrNull()
        val placeName = address?.locality
            ?: address?.subAdminArea
            ?: address?.adminArea?.removeSuffix("市")
            ?: "当前位置"
        return WeatherCity(placeName.removeSuffix("市"), location.latitude, location.longitude)
    }

    suspend fun load(context: Context): LocatedWeather {
        val city = locate(context)
        return LocatedWeather(city.name, WeatherRepository.load(city))
    }

    fun geocodeCity(context: Context, query: String): WeatherCity {
        val cleanName = query.trim().removeSuffix("市")
        weatherCities.firstOrNull { it.name == cleanName }?.let { return it }
        @Suppress("DEPRECATION")
        val address = runCatching {
            Geocoder(context, Locale.CHINA).getFromLocationName(cleanName, 1)?.firstOrNull()
        }.getOrNull()
        checkNotNull(address) { "没有找到这个城市，请检查名称" }
        val displayName = address.locality
            ?: address.subAdminArea
            ?: address.adminArea
            ?: cleanName
        return WeatherCity(displayName.removeSuffix("市"), address.latitude, address.longitude)
    }

    @SuppressLint("MissingPermission")
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.R)
    private suspend fun currentLocation(manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            manager.getCurrentLocation(provider, cancellationSignal, Runnable::run) { location ->
                if (continuation.isActive) continuation.resume(location)
            }
            continuation.invokeOnCancellation { cancellationSignal.cancel() }
        }
}

data class WeatherInfo(
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val apparentTemperature: Double = temperature,
    val precipitation: Double = 0.0,
) {
    val description: String
        get() = when (weatherCode) {
            0 -> "晴"; 1, 2 -> "晴间多云"; 3 -> "多云"; 45, 48 -> "有雾"
            51, 53, 55, 56, 57 -> "毛毛雨"; 61, 63, 65, 66, 67 -> "有雨"
            71, 73, 75, 77 -> "有雪"; 80, 81, 82 -> "阵雨"; 85, 86 -> "阵雪"
            95, 96, 99 -> "雷雨"; else -> "天气变化"
        }

    val symbol: String
        get() = when (weatherCode) {
            0 -> "☀️"; 1, 2 -> "🌤️"; 3 -> "☁️"; 45, 48 -> "🌫️"
            71, 73, 75, 77, 85, 86 -> "🌨️"; 95, 96, 99 -> "⛈️"; else -> "🌧️"
        }
}

data class DailyWeather(
    val date: String,
    val weatherCode: Int,
    val minTemperature: Double,
    val maxTemperature: Double,
) {
    val description: String get() = WeatherInfo(0.0, 0, 0.0, weatherCode).description
}

data class WeatherReport(val current: WeatherInfo, val daily: List<DailyWeather>)

object WeatherRepository {
    fun load(city: WeatherCity): WeatherInfo = loadReport(city).current

    fun loadReport(city: WeatherCity): WeatherReport {
        val endpoint = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${city.latitude}&longitude=${city.longitude}" +
            "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,precipitation" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
            "&forecast_days=5" +
            "&timezone=Asia%2FShanghai"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.connectTimeout = 8_000
        connection.readTimeout = 8_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode !in 200..299) error("天气服务返回 ${connection.responseCode}")
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(json).getJSONObject("current")
            val weatherInfo = WeatherInfo(
                current.getDouble("temperature_2m"), current.getInt("relative_humidity_2m"),
                current.getDouble("wind_speed_10m"), current.getInt("weather_code"),
                current.getDouble("apparent_temperature"),
                current.getDouble("precipitation"),
            )
            val daily = JSONObject(json).getJSONObject("daily")
            val dates = daily.getJSONArray("time")
            val codes = daily.getJSONArray("weather_code")
            val minimums = daily.getJSONArray("temperature_2m_min")
            val maximums = daily.getJSONArray("temperature_2m_max")
            val forecasts = List(minOf(5, dates.length())) { index ->
                DailyWeather(
                    date = dates.getString(index),
                    weatherCode = codes.getInt(index),
                    minTemperature = minimums.getDouble(index),
                    maxTemperature = maximums.getDouble(index),
                )
            }
            return WeatherReport(weatherInfo, forecasts)
        } finally {
            connection.disconnect()
        }
    }
}
