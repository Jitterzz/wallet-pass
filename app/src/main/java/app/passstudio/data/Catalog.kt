package app.passstudio.data

import app.passstudio.R

/** The built-in themes, their art and the colors to pick from. */
object Catalog {
    /** Where Google Wallet loads each theme's art from: the art/ folder of the public GitHub repo. */
    const val DEFAULT_ART_BASE = "https://raw.githubusercontent.com/Jitterzz/wallet-pass/main/art/"

    /** The public repo picked logos are uploaded to (its logos/ folder). */
    const val DEFAULT_GITHUB_REPO = "Jitterzz/wallet-pass"

    data class Group(val name: String, val ids: List<String>)

    val themes: List<PassTheme> = listOf(
        // Holidays.
        PassTheme("halloween", "Halloween", "#1b1033", "halloween", true),
        PassTheme("dia_de_muertos", "Día de los Muertos", "#2a0f3d", "dia_de_muertos", true),
        PassTheme("thanksgiving", "Thanksgiving", "#f7c66b", "thanksgiving", true),
        PassTheme("christmas", "Christmas", "#0c1b3a", "christmas", true),
        PassTheme("new_year", "New Year's", "#070b1f", "new_year", true),
        PassTheme("valentines", "Valentine's Day", "#ffd6e2", "valentines", true),
        PassTheme("st_patricks", "St. Patrick's Day", "#8fd3ff", "st_patricks", true),
        PassTheme("easter", "Easter", "#cfe8ff", "easter", true),
        PassTheme("cinco_de_mayo", "Cinco de Mayo", "#1d6f7a", "cinco_de_mayo", true),
        PassTheme("july_4", "4th of July", "#060d26", "july_4", true),
        // Seasons.
        PassTheme("spring", "Spring", "#bfe6ff", "spring", true),
        PassTheme("summer", "Summer", "#2f7fd6", "summer", true),
        PassTheme("fall", "Fall", "#ffd59a", "fall", true),
        PassTheme("winter", "Winter", "#b9d8f2", "winter", true),
        // Everyday.
        PassTheme("aurora", "Aurora", "#04101f", "aurora", true),
        PassTheme("ocean", "Ocean", "#4fa7e8", "ocean", true),
        PassTheme("desert", "Desert", "#ffb067", "desert", true),
        PassTheme("city", "City Nights", "#0d1230", "city", true),
        PassTheme("farm", "Farm", "#ffcf8a", "farm", true),
        PassTheme("mountains", "Mountains", "#6fb5f2", "mountains", true),
        // Simple.
        PassTheme("warehouse", "Warehouse", "#1a1a1a", "warehouse", true),
        PassTheme("shopping", "Shopping", "#9147df", "shopping", true),
        PassTheme("horizon", "Horizon", "#1e7e3b", "horizon", true),
    )

    val groups: List<Group> = listOf(
        Group("Holidays", listOf("halloween", "dia_de_muertos", "thanksgiving", "christmas", "new_year", "valentines",
            "st_patricks", "easter", "cinco_de_mayo", "july_4")),
        Group("Seasons", listOf("spring", "summer", "fall", "winter")),
        Group("Everyday", listOf("aurora", "ocean", "desert", "city", "farm", "mountains")),
        Group("Simple", listOf("warehouse", "shopping", "horizon")),
    )

    /** Every piece of art, in the order the theme editor shows them. */
    val arts: List<String> = themes.map { it.art }

    fun artName(key: String): String = themes.firstOrNull { it.art == key }?.name ?: key

    /** The art bundled in the app, for previews. Google Wallet loads the same art from [DEFAULT_ART_BASE]. */
    fun artRes(key: String): Int = when (key) {
        "halloween" -> R.drawable.art_halloween
        "dia_de_muertos" -> R.drawable.art_dia_de_muertos
        "thanksgiving" -> R.drawable.art_thanksgiving
        "christmas" -> R.drawable.art_christmas
        "new_year" -> R.drawable.art_new_year
        "valentines" -> R.drawable.art_valentines
        "st_patricks" -> R.drawable.art_st_patricks
        "easter" -> R.drawable.art_easter
        "cinco_de_mayo" -> R.drawable.art_cinco_de_mayo
        "july_4" -> R.drawable.art_july_4
        "spring" -> R.drawable.art_spring
        "summer" -> R.drawable.art_summer
        "fall" -> R.drawable.art_fall
        "winter" -> R.drawable.art_winter
        "ocean" -> R.drawable.art_ocean
        "desert" -> R.drawable.art_desert
        "city" -> R.drawable.art_city
        "farm" -> R.drawable.art_farm
        "mountains" -> R.drawable.art_mountains
        "warehouse" -> R.drawable.art_warehouse
        "shopping" -> R.drawable.art_shopping
        "horizon" -> R.drawable.art_horizon
        else -> R.drawable.art_aurora
    }

    /** Colors to pick from for a theme. Google advises against very bright neon colors on passes. */
    val swatches: List<String> = listOf(
        "#1a1a1a", "#2b2b2b", "#677088", "#111827", "#1b2a4e", "#1e3a5f", "#216acf", "#0b5c8a",
        "#0f766e", "#1e7e3b", "#2f6b3a", "#3a7d5c", "#5b4a8a", "#6d5bb0", "#9147df", "#5a1f5f",
        "#b0284f", "#d6322d", "#8f1d24", "#d4572f", "#f78f48", "#a14e1b", "#7a3e12", "#f9bb2d",
    )
}
