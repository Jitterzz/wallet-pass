package app.passstudio.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * The times of year a pass can change for: holidays (shown on their days) and the four seasons (the rest of
 * the year). Holidays come first, in order through the year from October.
 */
enum class Occasion(val label: String, val days: String, val holiday: Boolean, val defaultTheme: String) {
    HALLOWEEN("Halloween", "All of October", true, "halloween"),
    DIA_DE_MUERTOS("Día de los Muertos", "November 1 and 2", true, "dia_de_muertos"),
    THANKSGIVING("Thanksgiving", "November 1 to Thanksgiving", true, "thanksgiving"),
    CHRISTMAS("Christmas", "After Thanksgiving to December 26", true, "christmas"),
    NEW_YEAR("New Year's", "December 27 to January 2", true, "new_year"),
    VALENTINES("Valentine's Day", "February 7 to 14", true, "valentines"),
    ST_PATRICKS("St. Patrick's Day", "March 11 to 17", true, "st_patricks"),
    EASTER("Easter", "The week before Easter, to Easter Sunday", true, "easter"),
    CINCO_DE_MAYO("Cinco de Mayo", "May 1 to 5", true, "cinco_de_mayo"),
    JULY_4("4th of July", "July 1 to 5", true, "july_4"),
    SPRING("Spring", "March to May", false, "spring"),
    SUMMER("Summer", "June to August", false, "summer"),
    FALL("Fall", "September to November", false, "fall"),
    WINTER("Winter", "December to February", false, "winter");
}

object Seasons {
    /** Every holiday whose days take in [date]; when more than one does, the first one wins. */
    fun holidaysOn(date: LocalDate): List<Occasion> {
        val month = date.monthValue
        val day = date.dayOfMonth
        // Thanksgiving is the fourth Thursday of November; Christmas's days start the day after it.
        val thanksgiving = LocalDate.of(date.year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY))
        val easter = easterSunday(date.year)
        return buildList {
            if (month == 10) add(Occasion.HALLOWEEN)
            if (month == 11 && day <= 2) add(Occasion.DIA_DE_MUERTOS)
            if (month == 11 && !date.isAfter(thanksgiving)) add(Occasion.THANKSGIVING)
            if ((month == 11 && date.isAfter(thanksgiving)) || (month == 12 && day <= 26)) add(Occasion.CHRISTMAS)
            if ((month == 12 && day >= 27) || (month == 1 && day <= 2)) add(Occasion.NEW_YEAR)
            if (month == 2 && day in 7..14) add(Occasion.VALENTINES)
            if (month == 3 && day in 11..17) add(Occasion.ST_PATRICKS)
            if (!date.isBefore(easter.minusDays(7)) && !date.isAfter(easter)) add(Occasion.EASTER)
            if (month == 5 && day <= 5) add(Occasion.CINCO_DE_MAYO)
            if (month == 7 && day <= 5) add(Occasion.JULY_4)
        }
    }

    /** Spring is March to May, summer June to August, fall September to November, winter December to February. */
    fun seasonOn(date: LocalDate): Occasion = when (date.monthValue) {
        3, 4, 5 -> Occasion.SPRING
        6, 7, 8 -> Occasion.SUMMER
        9, 10, 11 -> Occasion.FALL
        else -> Occasion.WINTER
    }

    /** What a pass that changes on its own shows on [date]: the first holiday that's switched on, else the season. */
    fun current(date: LocalDate, schedule: Map<Occasion, ScheduleEntry>): Occasion =
        holidaysOn(date).firstOrNull { schedule[it]?.on != false } ?: seasonOn(date)

    /** Easter Sunday in [year] (the Anonymous Gregorian algorithm). */
    fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }
}
