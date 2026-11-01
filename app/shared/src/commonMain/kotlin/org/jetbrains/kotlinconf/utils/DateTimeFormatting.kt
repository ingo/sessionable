package org.jetbrains.kotlinconf.utils

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

/**
 * Dates and times as the attendee's language writes them. Times are 24-hour everywhere. The language is the one the
 * app is shown in (English when it has no translation of its own).
 */
object DateTimeFormatting {
    /** Replaces the device language, for tests. */
    internal var languageOverride: String? = null

    private fun formats(): Formats {
        val language = languageOverride ?: Locale.current.language
        return styles[language] ?: english
    }

    private val timeFormat = LocalDateTime.Format {
        hour(padding = Padding.ZERO)
        char(':')
        minute(padding = Padding.ZERO)
    }

    internal fun time(dateTime: LocalDateTime): String = dateTime.format(timeFormat)

    internal fun date(dateTime: LocalDateTime): String = date(dateTime.date)

    /** "October 22" */
    internal fun date(date: LocalDate): String = date.format(formats().date)

    /** "Oct 22" */
    internal fun shortDate(date: LocalDate): String = date.format(formats().shortDate)

    /** "Thursday, October 22" */
    internal fun weekdayDate(date: LocalDate): String = date.format(formats().weekdayDate)

    /** "October" */
    internal fun month(date: LocalDate): String = date.format(formats().month)

    /** "October 22, 2026" */
    internal fun dateWithYear(dateTime: LocalDateTime): String = dateTime.format(formats().dateWithYear)

    internal fun timeToTime(start: LocalDateTime, end: LocalDateTime): String = "${time(start)} – ${time(end)}"

    internal fun dateAndTime(dateTime: LocalDateTime): String = "${date(dateTime)}, ${time(dateTime)}"

    internal fun dateAndTime(start: LocalDateTime, end: LocalDateTime): String =
        "${date(start)}, ${time(start)} – ${time(end)}"

    /** How a language puts the day and the month together. */
    private enum class Order { MonthFirst, DayDotMonth, DayDeMonth, DayMonth }

    private class Style(
        val months: MonthNames,
        val shortMonths: MonthNames,
        val days: DayOfWeekNames,
        val order: Order,
        /** Between the weekday and the date. */
        val weekdaySeparator: String,
        /** Between the date and the year, where the year follows. */
        val yearSeparator: String,
    )

    private class Formats(private val style: Style, padDay: Padding) {
        private fun DateTimeFormatBuilder.WithDate.dayAndMonth(short: Boolean, pad: Padding) {
            val names = if (short) style.shortMonths else style.months
            when (style.order) {
                Order.MonthFirst -> {
                    monthName(names); char(' '); day(padding = pad)
                }
                Order.DayDotMonth -> {
                    day(padding = Padding.NONE); chars(". "); monthName(names)
                }
                // "22 de octubre", but "22 oct" when abbreviated
                Order.DayDeMonth -> {
                    day(padding = Padding.NONE); chars(if (short) " " else " de "); monthName(names)
                }
                Order.DayMonth -> {
                    day(padding = Padding.NONE); char(' '); monthName(names)
                }
            }
        }

        val date = LocalDate.Format { dayAndMonth(short = false, pad = padDay) }

        val shortDate = LocalDate.Format { dayAndMonth(short = true, pad = Padding.NONE) }

        val weekdayDate = LocalDate.Format {
            dayOfWeek(style.days)
            chars(style.weekdaySeparator)
            dayAndMonth(short = false, pad = Padding.NONE)
        }

        val month = LocalDate.Format { monthName(style.months) }

        val dateWithYear = LocalDateTime.Format {
            dayAndMonth(short = false, pad = padDay)
            chars(style.yearSeparator)
            year()
        }
    }

    private val english = Formats(
        Style(
            months = MonthNames.ENGLISH_FULL,
            shortMonths = MonthNames.ENGLISH_ABBREVIATED,
            days = DayOfWeekNames.ENGLISH_FULL,
            order = Order.MonthFirst,
            weekdaySeparator = ", ",
            yearSeparator = ", ",
        ),
        // The day of the month has always been zero-padded in the date line ("October 05")
        padDay = Padding.ZERO,
    )

    private val styles: Map<String, Formats> = mapOf(
        "en" to english,
        "de" to Formats(
            Style(
                months = MonthNames(
                    "Januar", "Februar", "März", "April", "Mai", "Juni",
                    "Juli", "August", "September", "Oktober", "November", "Dezember",
                ),
                shortMonths = MonthNames(
                    "Jan.", "Feb.", "März", "Apr.", "Mai", "Juni",
                    "Juli", "Aug.", "Sept.", "Okt.", "Nov.", "Dez.",
                ),
                days = DayOfWeekNames(
                    "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag",
                ),
                order = Order.DayDotMonth,
                weekdaySeparator = ", ",
                yearSeparator = " ",
            ),
            padDay = Padding.NONE,
        ),
        "es" to Formats(
            Style(
                months = MonthNames(
                    "enero", "febrero", "marzo", "abril", "mayo", "junio",
                    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
                ),
                shortMonths = MonthNames(
                    "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic",
                ),
                days = DayOfWeekNames(
                    "lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo",
                ),
                order = Order.DayDeMonth,
                weekdaySeparator = ", ",
                yearSeparator = " de ",
            ),
            padDay = Padding.NONE,
        ),
        "fr" to Formats(
            Style(
                months = MonthNames(
                    "janvier", "février", "mars", "avril", "mai", "juin",
                    "juillet", "août", "septembre", "octobre", "novembre", "décembre",
                ),
                shortMonths = MonthNames(
                    "janv.", "févr.", "mars", "avr.", "mai", "juin",
                    "juil.", "août", "sept.", "oct.", "nov.", "déc.",
                ),
                days = DayOfWeekNames(
                    "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche",
                ),
                order = Order.DayMonth,
                weekdaySeparator = " ",
                yearSeparator = " ",
            ),
            padDay = Padding.NONE,
        ),
        "pt" to Formats(
            Style(
                months = MonthNames(
                    "janeiro", "fevereiro", "março", "abril", "maio", "junho",
                    "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
                ),
                shortMonths = MonthNames(
                    "jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez",
                ),
                days = DayOfWeekNames(
                    "segunda-feira", "terça-feira", "quarta-feira", "quinta-feira",
                    "sexta-feira", "sábado", "domingo",
                ),
                order = Order.DayDeMonth,
                weekdaySeparator = ", ",
                yearSeparator = " de ",
            ),
            padDay = Padding.NONE,
        ),
    )
}
