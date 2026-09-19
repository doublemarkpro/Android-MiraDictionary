package com.handdict.studyassistant.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.edit
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer

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
        if (trimmed.isBlank()) return listOfNotNull(findExact("规"))
        val normalizedPinyin = normalizePinyin(trimmed)
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
            limit.toString(),
        )
        return database.rawQuery(sql, args).use { cursor -> cursor.toEntries() }
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
}

data class Lesson(val day: Int, val name: String, val start: String, val end: String)

data class TimerSnapshot(
    val taskName: String = "语文作业",
    val durationMinutes: Int = 35,
    val remainingSeconds: Int = 35 * 60,
    val running: Boolean = false,
    val endAtMillis: Long = 0L,
)

data class FocusRecord(
    val id: Long,
    val taskName: String,
    val durationMinutes: Int,
    val completedAtMillis: Long,
)

class LocalStore(context: Context) {
    private val preferences = context.getSharedPreferences("mira_study", Context.MODE_PRIVATE)

    fun loadLessons(): List<Lesson> {
        val stored = preferences.getString("lessons", null) ?: return defaultLessons
        return stored.lineSequence().mapNotNull { line ->
            val fields = line.split('|')
            if (fields.size == 4) Lesson(fields[0].toIntOrNull() ?: 0, fields[1], fields[2], fields[3]) else null
        }.toList().ifEmpty { defaultLessons }
    }

    fun saveLessons(lessons: List<Lesson>) {
        val encoded = lessons.joinToString("\n") { "${it.day}|${it.name}|${it.start}|${it.end}" }
        preferences.edit { putString("lessons", encoded) }
    }

    fun loadTimer(): TimerSnapshot = TimerSnapshot(
        taskName = preferences.getString("timer_name", "语文作业") ?: "语文作业",
        durationMinutes = preferences.getInt("timer_duration", 35),
        remainingSeconds = preferences.getInt("timer_remaining", 35 * 60),
        running = preferences.getBoolean("timer_running", false),
        endAtMillis = preferences.getLong("timer_end", 0L),
    )

    fun saveTimer(snapshot: TimerSnapshot) {
        preferences.edit {
            putString("timer_name", snapshot.taskName)
            putInt("timer_duration", snapshot.durationMinutes)
            putInt("timer_remaining", snapshot.remainingSeconds)
            putBoolean("timer_running", snapshot.running)
            putLong("timer_end", snapshot.endAtMillis)
        }
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
            .take(50)
        val array = JSONArray()
        records.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("task", item.taskName)
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
        val defaultLessons = listOf(
            Lesson(0, "语文", "08:00", "08:45"), Lesson(0, "数学", "09:00", "09:45"),
            Lesson(0, "英语", "10:00", "10:45"), Lesson(0, "体育", "14:00", "14:45"),
            Lesson(1, "数学", "08:00", "08:45"), Lesson(1, "科学", "09:00", "09:45"),
            Lesson(1, "美术", "14:00", "14:45"), Lesson(2, "语文", "08:00", "08:45"),
            Lesson(2, "英语", "09:00", "09:45"), Lesson(2, "音乐", "14:00", "14:45"),
            Lesson(3, "数学", "08:00", "08:45"), Lesson(3, "信息技术", "10:00", "10:45"),
            Lesson(4, "语文", "08:00", "08:45"), Lesson(4, "班会", "15:00", "15:45"),
        )
    }
}

data class WeatherCity(val name: String, val latitude: Double, val longitude: Double)

val weatherCities = listOf(
    WeatherCity("上海", 31.2304, 121.4737), WeatherCity("北京", 39.9042, 116.4074),
    WeatherCity("青岛", 36.0671, 120.3826), WeatherCity("深圳", 22.5431, 114.0579),
    WeatherCity("成都", 30.5728, 104.0668),
)

data class WeatherInfo(
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
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

object WeatherRepository {
    fun load(city: WeatherCity): WeatherInfo {
        val endpoint = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${city.latitude}&longitude=${city.longitude}" +
            "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m" +
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
            return WeatherInfo(
                current.getDouble("temperature_2m"), current.getInt("relative_humidity_2m"),
                current.getDouble("wind_speed_10m"), current.getInt("weather_code"),
            )
        } finally {
            connection.disconnect()
        }
    }
}
