package com.handdict.studyassistant.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DictionarySpeechQueryTest {
    @Test
    fun extractsCharacterFromNaturalQuestion() {
        assertEquals("智", extractDictionaryQuery("智能的智怎么写"))
        assertEquals("智", extractDictionaryQuery("智字怎么写？"))
        assertEquals("规", extractDictionaryQuery("请帮我查一下规"))
    }

    @Test
    fun preservesUsefulWordWhenNoQuestionPatternMatches() {
        assertEquals("智慧", extractDictionaryQuery("智慧"))
    }
}
