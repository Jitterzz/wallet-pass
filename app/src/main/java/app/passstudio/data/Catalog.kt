package app.passstudio.data

import app.passstudio.R

/** The built-in themes, their art and the colors to pick from. */
object Catalog {
    data class Group(val name: String, val ids: List<String>)

    val themes: List<PassTheme> = listOf(
        // Holidays.
        PassTheme("halloween", "Halloween", "#0d0820", "halloween", true),
        PassTheme("dia_de_muertos", "Día de los Muertos", "#1a0828", "dia_de_muertos", true),
        PassTheme("thanksgiving", "Thanksgiving", "#24132f", "thanksgiving", true),
        PassTheme("christmas", "Christmas", "#050c1f", "christmas", true),
        PassTheme("new_year", "New Year's", "#03050f", "new_year", true),
        PassTheme("valentines", "Valentine's Day", "#fff0f5", "valentines", true),
        PassTheme("st_patricks", "St. Patrick's Day", "#7cc8ff", "st_patricks", true),
        PassTheme("easter", "Easter", "#cfe8ff", "easter", true),
        PassTheme("cinco_de_mayo", "Cinco de Mayo", "#0f3d48", "cinco_de_mayo", true),
        PassTheme("july_4", "4th of July", "#020614", "july_4", true),
        // Seasons.
        PassTheme("spring", "Spring", "#a6d9ff", "spring", true),
        PassTheme("summer", "Summer", "#1d69c3", "summer", true),
        PassTheme("fall", "Fall", "#ffe6bd", "fall", true),
        PassTheme("winter", "Winter", "#a0c6e9", "winter", true),
        // Everyday.
        PassTheme("aurora", "Aurora", "#020812", "aurora", true),
        PassTheme("ocean", "Ocean", "#3a95de", "ocean", true),
        PassTheme("desert", "Desert", "#ffb067", "desert", true),
        PassTheme("city", "City Nights", "#060918", "city", true),
        PassTheme("farm", "Farm", "#ffcf8a", "farm", true),
        PassTheme("mountains", "Mountains", "#4f9ae6", "mountains", true),
        // Simple.
        PassTheme("warehouse", "Warehouse", "#121212", "warehouse", true),
        PassTheme("shopping", "Shopping", "#7b35c9", "shopping", true),
        PassTheme("horizon", "Horizon", "#14693a", "horizon", true),
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

    /** Each theme's scene, for the cards on the home screen and the theme pickers. */
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

    /** The full-page art behind a pass: the sky runs up behind the logo, name and barcode; the scene is at the bottom. */
    fun pageRes(key: String): Int = when (key) {
        "halloween" -> R.drawable.bg_halloween
        "dia_de_muertos" -> R.drawable.bg_dia_de_muertos
        "thanksgiving" -> R.drawable.bg_thanksgiving
        "christmas" -> R.drawable.bg_christmas
        "new_year" -> R.drawable.bg_new_year
        "valentines" -> R.drawable.bg_valentines
        "st_patricks" -> R.drawable.bg_st_patricks
        "easter" -> R.drawable.bg_easter
        "cinco_de_mayo" -> R.drawable.bg_cinco_de_mayo
        "july_4" -> R.drawable.bg_july_4
        "spring" -> R.drawable.bg_spring
        "summer" -> R.drawable.bg_summer
        "fall" -> R.drawable.bg_fall
        "winter" -> R.drawable.bg_winter
        "ocean" -> R.drawable.bg_ocean
        "desert" -> R.drawable.bg_desert
        "city" -> R.drawable.bg_city
        "farm" -> R.drawable.bg_farm
        "mountains" -> R.drawable.bg_mountains
        "warehouse" -> R.drawable.bg_warehouse
        "shopping" -> R.drawable.bg_shopping
        "horizon" -> R.drawable.bg_horizon
        else -> R.drawable.bg_aurora
    }

    /** The color the art's sky is behind the text, which decides light or dark text on the full-page pass. */
    fun skyColor(key: String): String = themes.firstOrNull { it.art == key }?.color ?: "#1a1a1a"

    /** Colors to pick from for a theme. */
    val swatches: List<String> = listOf(
        "#1a1a1a", "#2b2b2b", "#677088", "#111827", "#1b2a4e", "#1e3a5f", "#216acf", "#0b5c8a",
        "#0f766e", "#1e7e3b", "#2f6b3a", "#3a7d5c", "#5b4a8a", "#6d5bb0", "#9147df", "#5a1f5f",
        "#b0284f", "#d6322d", "#8f1d24", "#d4572f", "#f78f48", "#a14e1b", "#7a3e12", "#f9bb2d",
    )
}
