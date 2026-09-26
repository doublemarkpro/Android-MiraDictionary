package com.handdict.studyassistant.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.handdict.studyassistant.R
import com.handdict.studyassistant.data.LocalStore
import com.handdict.studyassistant.learning.BORROWING_DAILY_LIMIT
import com.handdict.studyassistant.learning.BorrowingAction
import com.handdict.studyassistant.learning.BorrowingEvent
import com.handdict.studyassistant.learning.BorrowPlace
import com.handdict.studyassistant.learning.BorrowingMath
import com.handdict.studyassistant.learning.BorrowingProblem
import com.handdict.studyassistant.learning.BorrowingRoundReducer
import com.handdict.studyassistant.learning.BorrowingRoundState
import com.handdict.studyassistant.ui.theme.MiraBlue
import com.handdict.studyassistant.ui.theme.MiraGreen
import com.handdict.studyassistant.ui.theme.MiraNavy
import kotlin.math.sin

private val BorrowBlue = Color(0xFF2677ED)
private val BorrowOrange = Color(0xFFFFA51E)
private val BorrowHundreds = Color(0xFFE8F5FF)
private val BorrowTens = Color(0xFFE9FBF2)
private val BorrowOnes = Color(0xFFFFF0E5)

@Composable
internal fun BorrowingTrainerScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { LocalStore(context.applicationContext) }
    val problems = remember { BorrowingMath.randomProblems(BORROWING_DAILY_LIMIT) }
    var completedToday by remember { mutableIntStateOf(store.loadBorrowingCompletedToday()) }
    var problemOrdinal by remember { mutableIntStateOf(completedToday) }

    if (completedToday >= BORROWING_DAILY_LIMIT && problemOrdinal >= BORROWING_DAILY_LIMIT) {
        BorrowingDailyComplete(completedToday)
        return
    }

    val problem = problems[problemOrdinal.coerceIn(0, BORROWING_DAILY_LIMIT - 1)]
    var roundState by remember(problemOrdinal) { mutableStateOf(BorrowingRoundState(problem)) }
    var lastBorrowPlace by remember(problemOrdinal) { mutableStateOf<BorrowPlace?>(null) }
    var replayEpoch by remember(problemOrdinal) { mutableIntStateOf(0) }
    var feedback by remember(problemOrdinal) { mutableStateOf("先从个位开始，一位一位完成吧！") }
    val currentPlace = roundState.currentPlace
    val currentDigitFinished = roundState.answerDigits[currentPlace.power] != null

    fun dispatch(action: BorrowingAction) {
        val before = roundState
        val transition = BorrowingRoundReducer.reduce(before, action)
        roundState = transition.state
        feedback = when (transition.event) {
            BorrowingEvent.BORROWED -> {
                lastBorrowPlace = before.currentPlace
                replayEpoch++
                val available = BorrowingMath.availableBeforeBorrow(
                    before.problem,
                    before.currentPlace,
                    transition.state.borrowedPlaces,
                ) + 10
                "借位成功！现在有 $available 个${before.currentPlace.unit}，请只算${before.currentPlace.label}。"
            }
            BorrowingEvent.BORROW_NOT_NEEDED -> "这一位够减，不需要借位。"
            BorrowingEvent.NEED_BORROW -> "这里不够减，要先完成借位哦。"
            BorrowingEvent.CORRECT_DIGIT -> if (before.currentPlace == BorrowPlace.HUNDREDS) {
                "三位都答对啦！点“完成本题”记录进度。"
            } else {
                "这一位答对啦！点“下一步”继续。"
            }
            BorrowingEvent.WRONG_DIGIT -> "再想一想：只计算${before.currentPlace.label}，先别看其他位。"
            BorrowingEvent.ALREADY_ANSWERED -> "这一位已经答对啦，点“下一步”继续。"
            BorrowingEvent.NEED_DIGIT -> "请先填写${before.currentPlace.label}的答案。"
            BorrowingEvent.ADVANCED -> "很好，现在只看${transition.state.currentPlace.label}。"
            BorrowingEvent.COMPLETED -> {
                completedToday = store.recordBorrowingCompletion()
                if (completedToday >= BORROWING_DAILY_LIMIT) {
                    "今天的 20 题全部完成，太棒了！"
                } else {
                    "本题完成！今天已经完成 $completedToday 题。"
                }
            }
            BorrowingEvent.ALREADY_COMPLETED -> feedback
        }
    }

    fun advance() {
        if (roundState.completed) {
            problemOrdinal = completedToday
        } else {
            dispatch(BorrowingAction.Advance)
        }
    }

    Column(Modifier.fillMaxSize()) {
        BorrowingProgressHeader(
            problem = problem,
            completedToday = completedToday,
            problemFinished = roundState.completed,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BorrowingWorkCard(
                problem = problem,
                currentPlace = currentPlace,
                borrowedPlaces = roundState.borrowedPlaces,
                answerDigits = roundState.answerDigits,
                lastBorrowPlace = lastBorrowPlace,
                replayEpoch = replayEpoch,
                feedback = feedback,
                onDigit = { dispatch(BorrowingAction.EnterDigit(it)) },
                modifier = Modifier.weight(1.82f).fillMaxHeight(),
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BorrowingGuideCard(
                    problem = problem,
                    place = currentPlace,
                    borrowedPlaces = roundState.borrowedPlaces,
                    digitFinished = currentDigitFinished,
                    problemFinished = roundState.completed,
                    onBorrow = { dispatch(BorrowingAction.Borrow) },
                    modifier = Modifier.weight(1.15f).fillMaxWidth(),
                )
                BorrowingRecordCard(
                    borrowedPlaces = roundState.borrowedPlaces,
                    answerDigits = roundState.answerDigits,
                    currentPlace = currentPlace,
                    problemFinished = roundState.completed,
                    modifier = Modifier.weight(0.8f).fillMaxWidth(),
                )
                Row(Modifier.fillMaxWidth().height(58.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = ::advance,
                        modifier = Modifier.weight(1.28f).fillMaxHeight(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BorrowBlue),
                    ) {
                        Text(
                            when {
                                roundState.completed -> if (completedToday >= BORROWING_DAILY_LIMIT) "查看成果" else "下一题"
                                roundState.currentPlaceIndex == 2 && currentDigitFinished -> "完成本题"
                                else -> "我懂了，下一步"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    OutlinedButton(
                        onClick = { replayEpoch++ },
                        enabled = lastBorrowPlace != null,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Icon(Icons.Rounded.Refresh, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("再演示一次", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BorrowingProgressHeader(
    problem: BorrowingProblem,
    completedToday: Int,
    problemFinished: Boolean,
) {
    Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = Color(0xFFE5F4FF), shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Edit, null, tint = BorrowBlue, modifier = Modifier.size(25.dp))
                Spacer(Modifier.width(9.dp))
                Text(
                    "计算：${problem.minuend} − ${problem.subtrahend}",
                    color = MiraNavy,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Surface(color = Color(0xFFFFF1E3), shape = RoundedCornerShape(50)) {
            Text(
                "🔥 今日最多 $BORROWING_DAILY_LIMIT 题",
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFFD96118),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.width(190.dp)) {
            val displayNumber = if (problemFinished) completedToday else (completedToday + 1).coerceAtMost(BORROWING_DAILY_LIMIT)
            Text("第 $displayNumber / $BORROWING_DAILY_LIMIT 题", color = MiraNavy, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(5.dp))
            Box(Modifier.fillMaxWidth().height(9.dp).background(Color(0xFFE3EAF4), CircleShape)) {
                Box(
                    Modifier
                        .fillMaxWidth((completedToday.toFloat() / BORROWING_DAILY_LIMIT).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF2F8DF4), Color(0xFF4BB7F6))), CircleShape),
                )
            }
        }
    }
}

@Composable
private fun BorrowingWorkCard(
    problem: BorrowingProblem,
    currentPlace: BorrowPlace,
    borrowedPlaces: Set<BorrowPlace>,
    answerDigits: List<Int?>,
    lastBorrowPlace: BorrowPlace?,
    replayEpoch: Int,
    feedback: String,
    onDigit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                // Keep the number pad clear of Android's bottom gesture/pinning zone.
                .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 54.dp),
        ) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(top = 72.dp, start = 44.dp, end = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf(BorrowPlace.HUNDREDS, BorrowPlace.TENS, BorrowPlace.ONES).forEach { place ->
                        BorrowingPlaceColumn(
                            problem = problem,
                            place = place,
                            borrowedPlaces = borrowedPlaces,
                            answer = answerDigits[place.power],
                            active = currentPlace == place,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
                BorrowingMotionLayer(
                    place = lastBorrowPlace,
                    replayEpoch = replayEpoch,
                    modifier = Modifier.fillMaxWidth().height(72.dp).align(Alignment.TopCenter),
                )
            }
            Surface(
                color = Color(0xFFF2F8FF),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    feedback,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color(0xFF42658D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                (0..9).forEach { digit ->
                    Button(
                        onClick = { onDigit(digit) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF378DF1)),
                    ) {
                        Text(digit.toString(), fontSize = 23.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun BorrowingPlaceColumn(
    problem: BorrowingProblem,
    place: BorrowPlace,
    borrowedPlaces: Set<BorrowPlace>,
    answer: Int?,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val background = when (place) {
        BorrowPlace.HUNDREDS -> BorrowHundreds
        BorrowPlace.TENS -> BorrowTens
        BorrowPlace.ONES -> BorrowOnes
    }
    val outline = when (place) {
        BorrowPlace.HUNDREDS -> Color(0xFF8BC9F4)
        BorrowPlace.TENS -> Color(0xFF72D8A7)
        BorrowPlace.ONES -> Color(0xFFFFB883)
    }
    val lentToLower = when (place) {
        BorrowPlace.ONES -> false
        BorrowPlace.TENS -> BorrowPlace.ONES in borrowedPlaces
        BorrowPlace.HUNDREDS -> BorrowPlace.TENS in borrowedPlaces
    }
    val receivedBorrow = place in borrowedPlaces
    val originalTop = BorrowingMath.digit(problem.minuend, place)
    val available = BorrowingMath.availableBeforeBorrow(problem, place, borrowedPlaces)
    val bottom = BorrowingMath.digit(problem.subtrahend, place)

    Column(
        modifier = modifier
            .background(background, RoundedCornerShape(18.dp))
            .then(if (active) Modifier.border(3.dp, outline, RoundedCornerShape(18.dp)) else Modifier)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(place.label, color = MiraNavy, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(3.dp))
        Text(
            originalTop.toString(),
            color = MiraNavy,
            fontSize = 38.sp,
            lineHeight = 39.sp,
            fontWeight = FontWeight.Black,
            textDecoration = if (lentToLower) TextDecoration.LineThrough else TextDecoration.None,
        )
        BorrowCorrectionSlot(if (lentToLower) available else null)
        BorrowCorrectionSlot(if (receivedBorrow) available + 10 else null)
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (place == BorrowPlace.HUNDREDS) "−" else "", color = MiraNavy, fontSize = 24.sp, fontWeight = FontWeight.Black)
            if (place == BorrowPlace.HUNDREDS) Spacer(Modifier.width(13.dp))
            Text(bottom.toString(), color = MiraNavy, fontSize = 38.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth().height(2.dp).background(MiraNavy))
        Spacer(Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.White.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                .border(2.dp, outline.copy(alpha = if (answer == null) 0.6f else 1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(answer?.toString().orEmpty(), color = BorrowBlue, fontSize = 29.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun BorrowCorrectionSlot(value: Int?) {
    Box(
        modifier = Modifier.fillMaxWidth().height(30.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null) {
            Text(
                text = value.toString(),
                color = BorrowBlue,
                fontSize = 27.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BorrowingMotionLayer(
    place: BorrowPlace?,
    replayEpoch: Int,
    modifier: Modifier = Modifier,
) {
    if (place == null) return
    val motion = remember { Animatable(0f) }
    LaunchedEffect(place, replayEpoch) {
        motion.snapTo(0f)
        motion.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }
    BoxWithConstraints(modifier) {
        val targetFraction = when (place) {
            BorrowPlace.TENS -> 0.50f
            BorrowPlace.ONES -> 0.82f
            BorrowPlace.HUNDREDS -> 0.50f
        }
        val sourceFraction = when (place) {
            BorrowPlace.TENS -> 0.18f
            BorrowPlace.ONES -> 0.50f
            BorrowPlace.HUNDREDS -> 0.18f
        }
        Canvas(Modifier.fillMaxSize()) {
            val start = Offset(size.width * sourceFraction, size.height * 0.66f)
            val end = Offset(size.width * targetFraction, size.height * 0.66f)
            val path = Path().apply {
                moveTo(start.x, start.y)
                quadraticTo((start.x + end.x) / 2f, size.height * 0.02f, end.x, end.y)
            }
            drawPath(path, BorrowOrange, style = Stroke(width = 5f, cap = StrokeCap.Round))
            drawLine(BorrowOrange, end, Offset(end.x - 12f, end.y - 12f), 5f, StrokeCap.Round)
            drawLine(BorrowOrange, end, Offset(end.x - 14f, end.y + 8f), 5f, StrokeCap.Round)
        }
        val x = maxWidth * (sourceFraction + (targetFraction - sourceFraction) * motion.value)
        val y = (16f - sin(Math.PI * motion.value).toFloat() * 12f).dp
        Surface(
            modifier = Modifier.offset(x = x - 22.dp, y = y).size(44.dp),
            shape = CircleShape,
            color = Color(0xFFFFC83D),
            shadowElevation = 5.dp,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("10", color = Color(0xFFB65308), fontSize = 19.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun BorrowingGuideCard(
    problem: BorrowingProblem,
    place: BorrowPlace,
    borrowedPlaces: Set<BorrowPlace>,
    digitFinished: Boolean,
    problemFinished: Boolean,
    onBorrow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val available = BorrowingMath.availableBeforeBorrow(problem, place, borrowedPlaces)
    val bottom = BorrowingMath.digit(problem.subtrahend, place)
    val needsBorrow = BorrowingMath.requiresBorrow(problem, place, borrowedPlaces)
    val borrowed = place in borrowedPlaces
    val source = when (place) {
        BorrowPlace.ONES -> BorrowPlace.TENS
        BorrowPlace.TENS -> BorrowPlace.HUNDREDS
        BorrowPlace.HUNDREDS -> null
    }
    val guide = when {
        problemFinished -> "三位都是你自己算出来的！\n完整答案现在才出现。"
        digitFinished -> "${place.label}已经答对。\n点“下一步”继续。"
        needsBorrow && !borrowed -> "$available 不够减 $bottom\n向${source?.label}借 1 个${source?.unit}\n1 个${source?.unit} = 10 个${place.unit}\n${source?.label}记得减 1"
        borrowed -> "借位完成！\n现在有 ${available + 10} 个${place.unit}\n请计算 ${available + 10} − $bottom\n只填写${place.label}"
        else -> "$available 够减 $bottom\n这一位不用借\n算出结果后\n只填写${place.label}"
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDFF2FF)),
    ) {
        Row(Modifier.fillMaxSize().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1.1f).fillMaxHeight()) {
                Surface(color = Color(0xFFFFEBA6), shape = RoundedCornerShape(50)) {
                    Text(
                        "先看${place.label}",
                        Modifier.padding(horizontal = 15.dp, vertical = 6.dp),
                        color = MiraNavy,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(guide, color = MiraNavy, fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                if (needsBorrow && !borrowed && source != null) {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onBorrow,
                        modifier = Modifier.fillMaxWidth().height(43.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BorrowOrange),
                    ) {
                        Text("向${source.label}借 1", fontSize = 15.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            Image(
                painter = painterResource(R.drawable.mira_borrowing_pencil_teacher),
                contentDescription = "铅笔老师",
                modifier = Modifier.weight(0.72f).fillMaxHeight().padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun BorrowingRecordCard(
    borrowedPlaces: Set<BorrowPlace>,
    answerDigits: List<Int?>,
    currentPlace: BorrowPlace,
    problemFinished: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF9EF)),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 15.dp, vertical = 10.dp)) {
            Text("📖  借位记录", color = MiraNavy, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            BorrowingMath.placesInSolveOrder.forEach { place ->
                val borrowed = place in borrowedPlaces
                val lent = when (place) {
                    BorrowPlace.ONES -> false
                    BorrowPlace.TENS -> BorrowPlace.ONES in borrowedPlaces
                    BorrowPlace.HUNDREDS -> BorrowPlace.TENS in borrowedPlaces
                }
                val status = when {
                    borrowed && lent -> "借入 1 次 · 借出 1 次"
                    borrowed -> "借过 1 次"
                    lent -> "借出 1 次"
                    answerDigits[place.power] != null -> "无需借位"
                    currentPlace == place && !problemFinished -> "正在判断"
                    else -> "等待"
                }
                Row(
                    Modifier.fillMaxWidth().weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = if (borrowed || lent || answerDigits[place.power] != null) MiraGreen else Color(0xFFFFC34A),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (borrowed || lent || answerDigits[place.power] != null) {
                                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(19.dp))
                            } else {
                                Text("•", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Spacer(Modifier.width(9.dp))
                    Text("${place.label}：$status", color = MiraNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BorrowingDailyComplete(completedToday: Int) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
    ) {
        Row(
            Modifier.fillMaxSize().padding(36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.mira_borrowing_pencil_teacher),
                contentDescription = null,
                modifier = Modifier.fillMaxHeight(0.8f).weight(0.7f),
            )
            Spacer(Modifier.width(28.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉 今日挑战完成！", color = MiraNavy, fontSize = 34.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                Text(
                    "你已经独立完成 $completedToday 道借位减法。\n今天先休息一下，明天再继续进步！",
                    color = Color(0xFF4F6E95),
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(18.dp))
                Surface(color = Color(0xFFEAF9EF), shape = RoundedCornerShape(50)) {
                    Text(
                        "每一位都只借一次，你做得很认真！",
                        Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                        color = MiraGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}
