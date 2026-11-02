package app.passstudio.ui

// Made by the art script (wallet-pass-art/art2/export_app.py); don't edit by hand.

/**
 * Where each theme's moving parts are on its page art (1032 x 2240): chimneys, flames, lights, water and so on,
 * three numbers each (x, y, and a size or color).
 */
object ThemeAnchors {
    private val empty = FloatArray(0)

    private val all: Map<String, Map<String, FloatArray>> by lazy {
        mapOf(
            "thanksgiving" to thanksgiving(),
            "halloween" to halloween(),
            "dia_de_muertos" to diaDeMuertos(),
            "christmas" to christmas(),
            "new_year" to newYear(),
            "valentines" to valentines(),
            "st_patricks" to stPatricks(),
            "easter" to easter(),
            "cinco_de_mayo" to cincoDeMayo(),
            "july_4" to july4(),
            "spring" to spring(),
            "summer" to summer(),
            "fall" to fall(),
            "winter" to winter(),
            "aurora" to aurora(),
            "ocean" to ocean(),
            "desert" to desert(),
            "city" to city(),
            "farm" to farm(),
            "mountains" to mountains(),
            "warehouse" to warehouse(),
            "shopping" to shopping(),
            "horizon" to horizon(),
        )
    }

    fun of(art: String, kind: String): FloatArray = all[art]?.get(kind) ?: empty

    private fun thanksgiving(): Map<String, FloatArray> = mapOf(
            "smoke" to floatArrayOf(815.0f, 1795.0f, 1.0f),
            "sun" to floatArrayOf(690.0f, 1858.0f, 74.0f),
            "window" to floatArrayOf(738.5f, 1879.7f, 27.0f, 801.5f, 1879.7f, 27.0f),
        )

    private fun halloween(): Map<String, FloatArray> = mapOf(
            "lantern" to floatArrayOf(375.0f, 2182.1f, 56.0f, 492.0f, 2204.8f, 38.0f, 608.0f, 2189.0f, 50.0f),
            "window" to floatArrayOf(681.4f, 1736.8f, 19.7f, 772.5f, 1718.7f, 21.3f, 813.5f, 1718.7f, 21.3f, 876.6f, 1690.0f, 24.6f, 787.2f, 1654.0f, 18.0f, 831.5f, 1815.5f, 24.6f),
        )

    private fun diaDeMuertos(): Map<String, FloatArray> = mapOf(
            "bulb" to floatArrayOf(14.3f, 321.0f, 16756782.0f, 62.9f, 333.2f, 15884170.0f, 111.4f, 340.6f, 5620208.0f, 160.0f, 343.0f, 8115081.0f, 208.6f, 340.6f, 16756782.0f, 257.1f, 333.2f, 15884170.0f, 305.7f, 321.0f, 5620208.0f, 724.4f, 321.0f, 8115081.0f, 773.3f, 333.2f, 16756782.0f, 822.1f, 340.6f, 15884170.0f, 871.0f, 343.0f, 5620208.0f, 919.9f, 340.6f, 8115081.0f, 968.7f, 333.2f, 16756782.0f, 1017.6f, 321.0f, 15884170.0f),
            "flame" to floatArrayOf(292.0f, 2014.8f, 14.0f, 364.8f, 2014.8f, 14.0f, 667.2f, 2014.8f, 14.0f, 740.0f, 2014.8f, 14.0f, 516.0f, 1784.4f, 22.0f, 210.0f, 2140.0f, 20.0f, 250.0f, 2114.0f, 20.0f, 790.0f, 2126.0f, 20.0f, 830.0f, 2150.0f, 20.0f),
        )

    private fun christmas(): Map<String, FloatArray> = mapOf(
            "bulb" to floatArrayOf(80.9f, 1763.3f, 16731486.0f, 100.7f, 1761.4f, 16765803.0f, 120.5f, 1760.7f, 5034362.0f, 140.3f, 1761.4f, 6543103.0f, 160.1f, 1763.3f, 16731486.0f, 179.9f, 1763.3f, 6543103.0f, 199.7f, 1761.4f, 5034362.0f, 219.5f, 1760.7f, 16765803.0f, 239.3f, 1761.4f, 16731486.0f, 259.1f, 1763.3f, 6543103.0f, 325.7f, 1777.6f, 16731486.0f, 341.1f, 1775.7f, 16765803.0f, 356.5f, 1775.1f, 5034362.0f, 371.9f, 1775.7f, 6543103.0f, 387.3f, 1777.6f, 16731486.0f, 402.7f, 1777.6f, 6543103.0f, 418.1f, 1775.7f, 5034362.0f, 433.5f, 1775.1f, 16765803.0f, 448.9f, 1775.7f, 16731486.0f, 464.3f, 1777.6f, 6543103.0f, 786.0f, 1770.2f, 16731486.0f, 806.9f, 1768.2f, 16765803.0f, 827.8f, 1767.6f, 5034362.0f, 848.6f, 1768.2f, 6543103.0f, 869.5f, 1770.2f, 16731486.0f, 890.5f, 1770.2f, 6543103.0f, 911.4f, 1768.2f, 5034362.0f, 932.2f, 1767.6f, 16765803.0f, 953.1f, 1768.2f, 16731486.0f, 974.0f, 1770.2f, 6543103.0f),
            "light" to floatArrayOf(678.9f, 2055.7f, 15087942.0f, 604.0f, 1976.3f, 15087942.0f, 569.9f, 2061.9f, 16765803.0f, 640.8f, 1942.9f, 16765803.0f, 615.7f, 1870.3f, 16777215.0f, 628.9f, 1945.6f, 16748459.0f, 617.0f, 1875.6f, 16765803.0f, 672.9f, 2001.5f, 11566322.0f, 621.3f, 1867.0f, 16765803.0f, 635.9f, 1912.2f, 11566322.0f, 656.0f, 1980.0f, 16777215.0f, 523.2f, 2125.1f, 11566322.0f, 681.9f, 2079.0f, 16765803.0f),
            "smoke" to floatArrayOf(224.0f, 1747.1f, 1.0f, 437.0f, 1760.8f, 1.0f, 937.0f, 1754.1f, 1.0f),
            "star" to floatArrayOf(620.0f, 1820.0f, 57.6f),
            "window" to floatArrayOf(132.2f, 1850.1f, 32.4f, 207.8f, 1850.1f, 32.4f, 365.6f, 1845.5f, 25.2f, 424.4f, 1845.5f, 25.2f, 840.1f, 1862.7f, 34.2f, 919.9f, 1862.7f, 34.2f),
        )

    private fun newYear(): Map<String, FloatArray> = mapOf(
            "citywin" to floatArrayOf(47.0f, 1803.0f, 1711428.0f, 2.0f, 2019.0f, 1711428.0f, 94.0f, 1973.0f, 1711428.0f, 136.0f, 1805.0f, 1711428.0f, 151.0f, 1967.0f, 1711428.0f, 249.0f, 1881.0f, 1711428.0f, 290.0f, 1789.0f, 1711428.0f, 305.0f, 1987.0f, 1711428.0f, 305.0f, 2005.0f, 1711428.0f, 363.0f, 1845.0f, 1711428.0f, 348.0f, 1971.0f, 1711428.0f, 427.0f, 1833.0f, 1711428.0f, 412.0f, 1941.0f, 1711428.0f, 472.0f, 1995.0f, 1711428.0f, 871.0f, 1761.0f, 1711428.0f, 958.0f, 2002.0f, 1711428.0f, 988.0f, 2020.0f, 1711428.0f, 1059.0f, 1886.0f, 1711428.0f, 64.0f, 2014.0f, 658724.0f, 200.0f, 1966.0f, 658724.0f, 356.0f, 1902.0f, 658724.0f, 622.0f, 1834.0f, 658724.0f, 852.0f, 1950.0f, 658724.0f),
            "water" to floatArrayOf(0.0f, 2040.0f, 0.0f),
        )

    private fun valentines(): Map<String, FloatArray> = emptyMap()

    private fun stPatricks(): Map<String, FloatArray> = mapOf(
            "glow" to floatArrayOf(820.0f, 2059.0f, 182.0f),
            "smoke" to floatArrayOf(336.3f, 1784.6f, 1.0f),
        )

    private fun easter(): Map<String, FloatArray> = mapOf(
            "sun" to floatArrayOf(840.0f, 1550.0f, 60.0f),
        )

    private fun cincoDeMayo(): Map<String, FloatArray> = mapOf(
            "bulb" to floatArrayOf(394.4f, 1869.1f, 16765286.0f, 423.1f, 1878.8f, 16740518.0f, 451.9f, 1885.3f, 3835647.0f, 480.6f, 1888.6f, 448160.0f, 509.4f, 1888.6f, 16765286.0f, 538.1f, 1885.3f, 16740518.0f, 566.9f, 1878.8f, 3835647.0f, 595.6f, 1869.1f, 448160.0f, 15.0f, 321.0f, 16765286.0f, 65.0f, 333.2f, 16740518.0f, 115.0f, 340.6f, 3835647.0f, 165.0f, 343.0f, 448160.0f, 215.0f, 340.6f, 16765286.0f, 265.0f, 333.2f, 16740518.0f, 315.0f, 321.0f, 3835647.0f, 724.4f, 321.0f, 448160.0f, 773.3f, 333.2f, 16765286.0f, 822.1f, 340.6f, 16740518.0f, 871.0f, 343.0f, 3835647.0f, 919.9f, 340.6f, 448160.0f, 968.7f, 333.2f, 16765286.0f, 1017.6f, 321.0f, 16740518.0f),
            "sun" to floatArrayOf(400.0f, 1850.0f, 100.0f),
            "window" to floatArrayOf(615.5f, 1933.1f, 24.7f, 664.9f, 1933.1f, 24.7f, 714.3f, 1933.1f, 24.7f, 763.7f, 1933.1f, 24.7f, 813.1f, 1933.1f, 24.7f),
        )

    private fun july4(): Map<String, FloatArray> = mapOf(
            "citywin" to floatArrayOf(571.0f, 1854.0f, 1055300.0f, 690.0f, 1838.0f, 1055300.0f, 906.0f, 1869.0f, 1055300.0f),
            "sparkler" to floatArrayOf(420.0f, 1980.0f, 0.0f, 600.0f, 1970.0f, 0.0f, 760.0f, 1990.0f, 0.0f),
            "water" to floatArrayOf(0.0f, 1885.0f, 0.0f),
        )

    private fun spring(): Map<String, FloatArray> = mapOf(
            "pond" to floatArrayOf(790.0f, 2080.0f, 200.0f),
            "sun" to floatArrayOf(860.0f, 1580.0f, 56.0f),
        )

    private fun summer(): Map<String, FloatArray> = mapOf(
            "sun" to floatArrayOf(640.0f, 1790.0f, 84.0f),
            "water" to floatArrayOf(0.0f, 1800.0f, 0.0f),
        )

    private fun fall(): Map<String, FloatArray> = mapOf(
            "lamp" to floatArrayOf(380.0f, 1863.8f, 27.6f, 760.0f, 1853.8f, 27.6f),
            "sun" to floatArrayOf(800.0f, 1670.0f, 64.0f),
        )

    private fun winter(): Map<String, FloatArray> = mapOf(
            "smoke" to floatArrayOf(861.6f, 1866.0f, 1.0f),
            "sun" to floatArrayOf(800.0f, 1590.0f, 50.0f),
            "window" to floatArrayOf(749.4f, 2027.2f, 39.6f, 850.6f, 2027.2f, 39.6f),
        )

    private fun aurora(): Map<String, FloatArray> = mapOf(
            "aurora" to floatArrayOf(0.0f, 1480.0f, 0.8f, 0.0f, 300.0f, 0.5f),
            "fire" to floatArrayOf(380.0f, 2069.4f, 54.0f),
            "water" to floatArrayOf(0.0f, 1850.0f, 0.0f),
        )

    private fun ocean(): Map<String, FloatArray> = mapOf(
            "beam" to floatArrayOf(150.0f, 1624.0f, 300.0f),
            "sun" to floatArrayOf(820.0f, 1580.0f, 54.0f),
            "water" to floatArrayOf(0.0f, 1780.0f, 0.0f),
        )

    private fun desert(): Map<String, FloatArray> = mapOf(
            "sun" to floatArrayOf(300.0f, 1710.0f, 88.0f),
        )

    private fun city(): Map<String, FloatArray> = mapOf(
            "antenna" to floatArrayOf(140.0f, 1738.0f, 0.0f, 330.0f, 1749.9f, 0.0f, 610.0f, 1745.6f, 0.0f, 790.0f, 1726.1f, 0.0f, 150.0f, 1774.0f, 0.0f, 470.0f, 1774.0f, 0.0f),
            "citywin" to floatArrayOf(210.0f, 1792.0f, 2762840.0f, 555.0f, 1688.0f, 2762840.0f, 555.0f, 1706.0f, 2762840.0f, 570.0f, 1904.0f, 2762840.0f, 620.0f, 1842.0f, 2762840.0f, 723.0f, 1760.0f, 2762840.0f, 769.0f, 1773.0f, 2762840.0f, 858.0f, 1894.0f, 2762840.0f, 828.0f, 1930.0f, 2762840.0f, 983.0f, 1878.0f, 2762840.0f, 2.0f, 1980.0f, 1381686.0f, 98.0f, 1919.0f, 1381686.0f, 113.0f, 2009.0f, 1381686.0f, 283.0f, 1913.0f, 1381686.0f, 575.0f, 1992.0f, 1381686.0f, 678.0f, 1921.0f, 1381686.0f, 809.0f, 1780.0f, 1381686.0f, 809.0f, 1888.0f, 1381686.0f, 954.0f, 1913.0f, 1381686.0f, 1029.0f, 1876.0f, 1381686.0f),
            "ferris" to floatArrayOf(830.0f, 1850.0f, 130.0f),
            "lamp" to floatArrayOf(90.0f, 2039.0f, 18.0f, 520.0f, 2039.0f, 18.0f, 960.0f, 2039.0f, 18.0f),
            "water" to floatArrayOf(0.0f, 2020.0f, 0.0f),
        )

    private fun farm(): Map<String, FloatArray> = mapOf(
            "sun" to floatArrayOf(220.0f, 1680.0f, 70.0f),
        )

    private fun mountains(): Map<String, FloatArray> = mapOf(
            "water" to floatArrayOf(0.0f, 1980.0f, 0.0f),
        )

    private fun warehouse(): Map<String, FloatArray> = mapOf(
            "lamp" to floatArrayOf(130.0f, 422.0f, 30.0f, 900.0f, 422.0f, 30.0f),
        )

    private fun shopping(): Map<String, FloatArray> = emptyMap()

    private fun horizon(): Map<String, FloatArray> = emptyMap()
}
