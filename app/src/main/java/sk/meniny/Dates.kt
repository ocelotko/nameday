package sk.meniny

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val SLOVAK: Locale = Locale.forLanguageTag("sk-SK")

private val longDate = DateTimeFormatter.ofPattern("EEEE d. MMMM", SLOVAK)
private val monthName = DateTimeFormatter.ofPattern("LLLL", SLOVAK)
private val shortWeekday = DateTimeFormatter.ofPattern("EEE", SLOVAK)

/** napr. „Piatok 23. októbra" */
fun formatLongDate(date: LocalDate): String = longDate.format(date).capitalized()

/** napr. „Október" */
fun formatMonth(date: LocalDate): String = monthName.format(date).capitalized()

/** napr. „pi" */
fun formatShortWeekday(date: LocalDate): String = shortWeekday.format(date)

private fun String.capitalized() = replaceFirstChar { it.titlecase(SLOVAK) }
