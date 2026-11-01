package org.jetbrains.kotlinconf.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The dates of each language the app speaks, for a Thursday and for a single-digit day. */
class DateTimeFormattingTest {
    private val thursday = LocalDate(2026, 10, 22)
    private val sunday = LocalDate(2026, 11, 1)
    private val morning = LocalDateTime(2026, 10, 22, 9, 0)

    @AfterTest fun reset() { DateTimeFormatting.languageOverride = null }

    private fun inLanguage(language: String, check: () -> Unit) {
        DateTimeFormatting.languageOverride = language
        check()
    }

    @Test
    fun englishKeepsItsFormats() = inLanguage("en") {
        assertEquals("October 22", DateTimeFormatting.date(thursday))
        assertEquals("November 01", DateTimeFormatting.date(sunday))
        assertEquals("Oct 22", DateTimeFormatting.shortDate(thursday))
        assertEquals("Thursday, October 22", DateTimeFormatting.weekdayDate(thursday))
        assertEquals("Sunday, November 1", DateTimeFormatting.weekdayDate(sunday))
        assertEquals("October", DateTimeFormatting.month(thursday))
        assertEquals("October 22, 2026", DateTimeFormatting.dateWithYear(morning))
    }

    @Test
    fun germanPutsTheDayFirstWithADot() = inLanguage("de") {
        assertEquals("22. Oktober", DateTimeFormatting.date(thursday))
        assertEquals("1. November", DateTimeFormatting.date(sunday))
        assertEquals("22. Okt.", DateTimeFormatting.shortDate(thursday))
        assertEquals("Donnerstag, 22. Oktober", DateTimeFormatting.weekdayDate(thursday))
        assertEquals("22. Oktober 2026", DateTimeFormatting.dateWithYear(morning))
    }

    @Test
    fun spanishUsesDeBetweenTheParts() = inLanguage("es") {
        assertEquals("22 de octubre", DateTimeFormatting.date(thursday))
        assertEquals("22 oct", DateTimeFormatting.shortDate(thursday))
        assertEquals("jueves, 22 de octubre", DateTimeFormatting.weekdayDate(thursday))
        assertEquals("domingo, 1 de noviembre", DateTimeFormatting.weekdayDate(sunday))
        assertEquals("22 de octubre de 2026", DateTimeFormatting.dateWithYear(morning))
    }

    @Test
    fun frenchHasNoCommaAfterTheWeekday() = inLanguage("fr") {
        assertEquals("22 octobre", DateTimeFormatting.date(thursday))
        assertEquals("1 novembre", DateTimeFormatting.date(sunday))
        assertEquals("22 oct.", DateTimeFormatting.shortDate(thursday))
        assertEquals("jeudi 22 octobre", DateTimeFormatting.weekdayDate(thursday))
        assertEquals("22 octobre 2026", DateTimeFormatting.dateWithYear(morning))
    }

    @Test
    fun portugueseUsesDeAndLongWeekdayNames() = inLanguage("pt") {
        assertEquals("22 de outubro", DateTimeFormatting.date(thursday))
        assertEquals("22 out", DateTimeFormatting.shortDate(thursday))
        assertEquals("quinta-feira, 22 de outubro", DateTimeFormatting.weekdayDate(thursday))
        assertEquals("22 de outubro de 2026", DateTimeFormatting.dateWithYear(morning))
    }

    @Test
    fun otherLanguagesFallBackToEnglish() = inLanguage("ja") {
        assertEquals("Thursday, October 22", DateTimeFormatting.weekdayDate(thursday))
    }

    @Test
    fun timesAreTwentyFourHourEverywhere() {
        for (language in listOf("en", "de", "es", "fr", "pt")) {
            DateTimeFormatting.languageOverride = language
            assertEquals("09:05", DateTimeFormatting.time(LocalDateTime(2026, 10, 22, 9, 5)), language)
            assertEquals("14:30", DateTimeFormatting.time(LocalDateTime(2026, 10, 22, 14, 30)), language)
        }
    }
}
