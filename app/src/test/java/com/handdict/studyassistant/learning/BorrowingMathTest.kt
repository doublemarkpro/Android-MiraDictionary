package com.handdict.studyassistant.learning

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BorrowingMathTest {
    @Test
    fun `523 minus 178 walks through two borrows`() {
        val problem = BorrowingProblem(523, 178)
        var state = BorrowingRoundState(problem)

        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Borrow).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(5)).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Advance).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Borrow).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(4)).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Advance).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(3)).state
        val completed = BorrowingRoundReducer.reduce(state, BorrowingAction.Advance)

        assertEquals(BorrowingEvent.COMPLETED, completed.event)
        assertEquals(listOf(5, 4, 3), completed.state.answerDigits)
    }

    @Test
    fun `790 minus 517 accepts three then seven then two`() {
        var state = BorrowingRoundState(BorrowingProblem(790, 517))
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Borrow).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(3)).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Advance).state
        val tens = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(7))

        assertEquals(BorrowingEvent.CORRECT_DIGIT, tens.event)
        assertEquals(8, BorrowingMath.availableBeforeBorrow(state.problem, BorrowPlace.TENS, state.borrowedPlaces))
        state = BorrowingRoundReducer.reduce(tens.state, BorrowingAction.Advance).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(2)).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Advance).state
        assertTrue(state.completed)
        assertEquals(listOf(3, 7, 2), state.answerDigits)
    }

    @Test
    fun `452 minus 315 accepts seven then three then one`() {
        var state = BorrowingRoundState(BorrowingProblem(452, 315))
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Borrow).state
        val ones = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(7))

        assertEquals(BorrowingEvent.CORRECT_DIGIT, ones.event)
        state = BorrowingRoundReducer.reduce(ones.state, BorrowingAction.Advance).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(3)).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Advance).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(1)).state
        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Advance).state
        assertTrue(state.completed)
        assertEquals(listOf(7, 3, 1), state.answerDigits)
    }

    @Test
    fun `thousands of random problems solve to their real difference`() {
        val random = Random(8_611_947)
        repeat(200) {
            val problems = BorrowingMath.randomProblems(BORROWING_DAILY_LIMIT, random)
            assertEquals(BORROWING_DAILY_LIMIT, problems.distinct().size)
            problems.forEach { problem ->
                var state = BorrowingRoundState(problem)
                while (!state.completed) {
                    if (BorrowingMath.requiresBorrow(problem, state.currentPlace, state.borrowedPlaces)) {
                        state = BorrowingRoundReducer.reduce(state, BorrowingAction.Borrow).state
                    }
                    val answer = BorrowingMath.answerDigit(problem, state.currentPlace, state.borrowedPlaces)
                    val entered = BorrowingRoundReducer.reduce(state, BorrowingAction.EnterDigit(answer))
                    assertEquals(BorrowingEvent.CORRECT_DIGIT, entered.event)
                    state = BorrowingRoundReducer.reduce(entered.state, BorrowingAction.Advance).state
                }
                val reconstructed = state.answerDigits[2]!! * 100 + state.answerDigits[1]!! * 10 + state.answerDigits[0]!!
                assertEquals(problem.difference, reconstructed)
                assertTrue(state.borrowedPlaces.isNotEmpty())
                assertFalse(state.answerDigits.any { it == null })
            }
        }
    }
}
