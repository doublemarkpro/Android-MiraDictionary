package com.handdict.studyassistant.learning

import kotlin.random.Random

const val BORROWING_DAILY_LIMIT = 20

enum class BorrowPlace(val power: Int, val label: String, val unit: String) {
    ONES(0, "个位", "一"),
    TENS(1, "十位", "十"),
    HUNDREDS(2, "百位", "百"),
}

data class BorrowingProblem(
    val minuend: Int,
    val subtrahend: Int,
) {
    init {
        require(minuend in 100..999)
        require(subtrahend in 100 until minuend)
    }

    val difference: Int get() = minuend - subtrahend
}

data class BorrowingRoundState(
    val problem: BorrowingProblem,
    val currentPlaceIndex: Int = 0,
    val borrowedPlaces: Set<BorrowPlace> = emptySet(),
    val answerDigits: List<Int?> = listOf(null, null, null),
    val completed: Boolean = false,
) {
    init {
        require(currentPlaceIndex in 0..2)
        require(answerDigits.size == 3)
    }

    val currentPlace: BorrowPlace get() = BorrowingMath.placesInSolveOrder[currentPlaceIndex]
}

sealed interface BorrowingAction {
    data object Borrow : BorrowingAction
    data class EnterDigit(val digit: Int) : BorrowingAction
    data object Advance : BorrowingAction
}

enum class BorrowingEvent {
    BORROWED,
    BORROW_NOT_NEEDED,
    NEED_BORROW,
    CORRECT_DIGIT,
    WRONG_DIGIT,
    ALREADY_ANSWERED,
    NEED_DIGIT,
    ADVANCED,
    COMPLETED,
    ALREADY_COMPLETED,
}

data class BorrowingTransition(
    val state: BorrowingRoundState,
    val event: BorrowingEvent,
)

object BorrowingRoundReducer {
    fun reduce(state: BorrowingRoundState, action: BorrowingAction): BorrowingTransition {
        if (state.completed) return BorrowingTransition(state, BorrowingEvent.ALREADY_COMPLETED)
        val place = state.currentPlace
        return when (action) {
            BorrowingAction.Borrow -> {
                if (!BorrowingMath.requiresBorrow(state.problem, place, state.borrowedPlaces)) {
                    BorrowingTransition(state, BorrowingEvent.BORROW_NOT_NEEDED)
                } else {
                    BorrowingTransition(
                        state.copy(borrowedPlaces = state.borrowedPlaces + place),
                        BorrowingEvent.BORROWED,
                    )
                }
            }

            is BorrowingAction.EnterDigit -> {
                when {
                    action.digit !in 0..9 -> BorrowingTransition(state, BorrowingEvent.WRONG_DIGIT)
                    state.answerDigits[place.power] != null -> BorrowingTransition(state, BorrowingEvent.ALREADY_ANSWERED)
                    BorrowingMath.requiresBorrow(state.problem, place, state.borrowedPlaces) ->
                        BorrowingTransition(state, BorrowingEvent.NEED_BORROW)
                    action.digit != BorrowingMath.answerDigit(state.problem, place, state.borrowedPlaces) ->
                        BorrowingTransition(state, BorrowingEvent.WRONG_DIGIT)
                    else -> BorrowingTransition(
                        state.copy(
                            answerDigits = state.answerDigits.toMutableList().also { it[place.power] = action.digit },
                        ),
                        BorrowingEvent.CORRECT_DIGIT,
                    )
                }
            }

            BorrowingAction.Advance -> {
                when {
                    state.answerDigits[place.power] == null &&
                        BorrowingMath.requiresBorrow(state.problem, place, state.borrowedPlaces) ->
                        BorrowingTransition(state, BorrowingEvent.NEED_BORROW)
                    state.answerDigits[place.power] == null -> BorrowingTransition(state, BorrowingEvent.NEED_DIGIT)
                    state.currentPlaceIndex < 2 -> BorrowingTransition(
                        state.copy(currentPlaceIndex = state.currentPlaceIndex + 1),
                        BorrowingEvent.ADVANCED,
                    )
                    else -> BorrowingTransition(state.copy(completed = true), BorrowingEvent.COMPLETED)
                }
            }
        }
    }
}

object BorrowingMath {
    val placesInSolveOrder = listOf(BorrowPlace.ONES, BorrowPlace.TENS, BorrowPlace.HUNDREDS)

    fun digit(value: Int, place: BorrowPlace): Int = value / powerOfTen(place.power) % 10

    fun availableBeforeBorrow(
        problem: BorrowingProblem,
        place: BorrowPlace,
        borrowedPlaces: Set<BorrowPlace>,
    ): Int {
        val lentToLowerPlace = when (place) {
            BorrowPlace.ONES -> false
            BorrowPlace.TENS -> BorrowPlace.ONES in borrowedPlaces
            BorrowPlace.HUNDREDS -> BorrowPlace.TENS in borrowedPlaces
        }
        return digit(problem.minuend, place) - if (lentToLowerPlace) 1 else 0
    }

    fun requiresBorrow(
        problem: BorrowingProblem,
        place: BorrowPlace,
        borrowedPlaces: Set<BorrowPlace>,
    ): Boolean = place != BorrowPlace.HUNDREDS && place !in borrowedPlaces &&
        availableBeforeBorrow(problem, place, borrowedPlaces) < digit(problem.subtrahend, place)

    fun answerDigit(
        problem: BorrowingProblem,
        place: BorrowPlace,
        borrowedPlaces: Set<BorrowPlace>,
    ): Int {
        val available = availableBeforeBorrow(problem, place, borrowedPlaces) +
            if (place in borrowedPlaces) 10 else 0
        return available - digit(problem.subtrahend, place)
    }

    fun randomProblem(random: Random = Random.Default): BorrowingProblem {
        while (true) {
            val topHundreds = random.nextInt(4, 10)
            val topTens = random.nextInt(1, 10)
            val topOnes = random.nextInt(0, 10)
            val bottomTens = random.nextInt(0, 10)
            val bottomOnes = random.nextInt(0, 10)
            val onesBorrow = topOnes < bottomOnes
            val tensAvailable = topTens - if (onesBorrow) 1 else 0
            val tensBorrow = tensAvailable < bottomTens
            if (!onesBorrow && !tensBorrow) continue
            val maxBottomHundreds = topHundreds - if (tensBorrow) 2 else 1
            if (maxBottomHundreds < 1) continue
            val bottomHundreds = random.nextInt(1, maxBottomHundreds + 1)
            val problem = BorrowingProblem(
                minuend = topHundreds * 100 + topTens * 10 + topOnes,
                subtrahend = bottomHundreds * 100 + bottomTens * 10 + bottomOnes,
            )
            if (problem.difference >= 100) return problem
        }
    }

    fun randomProblems(count: Int, random: Random = Random.Default): List<BorrowingProblem> {
        require(count >= 0)
        val problems = linkedSetOf<BorrowingProblem>()
        while (problems.size < count) problems += randomProblem(random)
        return problems.toList()
    }

    private fun powerOfTen(power: Int): Int = when (power) {
        0 -> 1
        1 -> 10
        else -> 100
    }
}
