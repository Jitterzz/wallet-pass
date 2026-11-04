package app.passstudio.ui

// Made by the art script (art-src/export_app.py); don't edit by hand.

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
            "golden_gate" to goldenGate(),
            "fuji" to fuji(),
            "grand_canyon" to grandCanyon(),
            "cappadocia" to cappadocia(),
            "paris" to paris(),
            "london" to london(),
            "vegas" to vegas(),
            "rainy" to rainy(),
            "reef" to reef(),
            "volcano" to volcano(),
            "space" to space(),
            "retro" to retro(),
        )
    }

    fun of(art: String, kind: String): FloatArray = all[art]?.get(kind) ?: empty

    private fun thanksgiving(): Map<String, FloatArray> = mapOf(
            "smoke" to floatArrayOf(207.6f, 1864.4f, 1.0f),
            "sun" to floatArrayOf(456.0f, 1906.0f, 94.0f),
            "window" to floatArrayOf(140.3f, 1941.6f, 23.8f, 195.7f, 1941.6f, 23.8f, 818.0f, 1850.0f, 22.0f),
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

    private fun goldenGate(): Map<String, FloatArray> = mapOf(
            "antenna" to floatArrayOf(790.0f, 1441.0f, 0.0f, 320.0f, 1597.2f, 0.0f),
            "deck" to floatArrayOf(1032.0f, 1733.7f, 1.0f, 248.6f, 1795.7f, 0.5f),
            "fog" to floatArrayOf(0.0f, 1858.0f, 44.0f),
            "lane" to floatArrayOf(0.0f, 1912.0f, 0.8f, 0.0f, 1972.0f, 1.0f),
            "sun" to floatArrayOf(190.0f, 1710.0f, 42.0f),
            "surf" to floatArrayOf(0.0f, 1998.0f, 30.0f),
            "water" to floatArrayOf(0.0f, 1852.0f, 0.0f),
        )

    private fun fuji(): Map<String, FloatArray> = mapOf(
            "rail" to floatArrayOf(424.0f, 2005.0f, 1.0f, 1032.0f, 2005.0f, 1.0f),
            "sun" to floatArrayOf(868.0f, 1594.0f, 38.0f),
        )

    private fun grandCanyon(): Map<String, FloatArray> = mapOf(
            "condor" to floatArrayOf(720.0f, 1580.0f, 0.0f, 250.0f, 1632.0f, 0.0f),
            "glint" to floatArrayOf(594.4f, 2020.8f, 3.6f, 600.8f, 2021.9f, 3.6f, 607.0f, 2021.6f, 3.6f, 612.8f, 2019.5f, 3.6f, 618.7f, 2017.0f, 3.6f, 565.2f, 2097.0f, 7.0f, 575.9f, 2099.0f, 7.0f, 586.6f, 2100.1f, 7.0f, 597.4f, 2099.3f, 7.0f, 608.4f, 2096.6f, 7.0f, 619.0f, 2093.4f, 7.0f, 628.7f, 2090.7f, 7.0f, 637.4f, 2088.1f, 7.0f, 645.7f, 2086.3f, 7.0f, 654.4f, 2086.3f, 7.0f, 595.9f, 2203.4f, 11.5f, 607.9f, 2206.1f, 11.5f, 620.0f, 2207.9f, 11.5f, 632.1f, 2207.5f, 11.5f, 644.5f, 2204.9f, 11.5f, 656.5f, 2201.5f, 11.5f, 667.1f, 2198.5f, 11.5f, 676.2f, 2195.1f, 11.5f, 685.2f, 2192.5f, 11.5f),
            "shade" to floatArrayOf(0.0f, 1720.0f, 440.0f),
            "sun" to floatArrayOf(360.0f, 1696.0f, 44.0f),
        )

    private fun cappadocia(): Map<String, FloatArray> = mapOf(
            "burner" to floatArrayOf(470.0f, 1518.0f, 2.4f, 560.0f, 1602.8f, 1.7f, 640.0f, 1545.0f, 2.0f, 360.0f, 1651.2f, 1.5f, 900.0f, 1606.5f, 2.2f, 980.0f, 1690.5f, 1.4f, 700.0f, 1689.8f, 1.3f, 150.0f, 1719.8f, 1.3f, 420.0f, 1748.2f, 1.1f, 760.0f, 1511.2f, 1.5f, 300.0f, 1569.0f, 1.2f, 840.0f, 1717.5f, 1.0f, 262.0f, 1858.5f, 3.0f, 470.0f, 1885.5f, 2.6f, 668.0f, 1859.0f, 3.6f, 902.0f, 1899.0f, 2.8f, 560.0f, 1684.0f, 7.2f, 112.0f, 485.2f, 9.5f, 928.0f, 408.5f, 7.0f, 44.0f, 646.5f, 4.6f, 994.0f, 581.0f, 4.4f, 40.0f, 1206.0f, 4.0f, 995.0f, 1041.5f, 4.2f, 150.0f, 1357.0f, 2.0f, 884.0f, 1334.5f, 2.2f, 62.0f, 1308.0f, 1.6f, 968.0f, 1232.8f, 1.7f),
            "rise" to floatArrayOf(300.0f, 2020.0f, 0.4f, 560.0f, 2030.0f, 0.4f, 760.0f, 2000.0f, 0.4f, 440.0f, 1990.0f, 0.3f),
            "sun" to floatArrayOf(806.0f, 1772.0f, 50.0f),
            "window" to floatArrayOf(131.6f, 1917.3f, 9.7f, 69.7f, 1899.7f, 7.7f, 192.3f, 1885.7f, 10.8f, 176.2f, 1887.4f, 7.3f, 336.6f, 1930.9f, 9.2f, 605.6f, 1933.5f, 7.8f, 818.4f, 1941.4f, 9.0f, 88.0f, 2090.0f, 19.5f, 61.0f, 2000.0f, 15.0f, 212.8f, 2060.0f, 13.4f, 950.4f, 2094.8f, 20.8f, 976.0f, 1999.6f, 16.0f, 944.0f, 1952.0f, 12.8f, 849.4f, 2118.0f, 13.4f, 300.0f, 2120.0f, 17.6f, 730.0f, 2122.0f, 16.0f),
        )

    private fun paris(): Map<String, FloatArray> = mapOf(
            "beacon" to floatArrayOf(560.0f, 1502.4f, 700.0f),
            "lamp" to floatArrayOf(90.0f, 1996.8f, 5.5f, 260.0f, 1996.8f, 5.5f, 430.0f, 1996.8f, 5.5f, 600.0f, 1996.8f, 5.5f, 770.0f, 1996.8f, 5.5f, 940.0f, 1996.8f, 5.5f),
            "lane" to floatArrayOf(0.0f, 2176.0f, 1.0f),
            "sparkle" to floatArrayOf(573.2f, 1575.1f, 0.0f, 546.0f, 1711.8f, 0.0f, 550.5f, 1770.7f, 0.0f, 540.4f, 1841.2f, 0.0f, 470.4f, 2049.0f, 0.0f, 538.0f, 1916.9f, 0.0f, 619.7f, 1981.7f, 0.0f, 565.2f, 1545.9f, 0.0f, 566.3f, 1741.4f, 0.0f, 566.7f, 1544.0f, 0.0f, 523.8f, 1881.9f, 0.0f, 568.8f, 1810.2f, 0.0f, 539.4f, 1764.9f, 0.0f, 610.1f, 2007.9f, 0.0f, 579.3f, 1711.9f, 0.0f, 517.0f, 1986.9f, 0.0f, 568.7f, 1705.2f, 0.0f, 649.9f, 2047.2f, 0.0f, 566.3f, 1570.2f, 0.0f, 558.4f, 1599.1f, 0.0f, 584.2f, 1801.6f, 0.0f, 554.1f, 1807.1f, 0.0f, 555.2f, 1648.6f, 0.0f, 567.4f, 1522.6f, 0.0f, 615.3f, 1947.7f, 0.0f, 575.9f, 1879.8f, 0.0f, 568.7f, 1537.3f, 0.0f, 566.9f, 1715.2f, 0.0f, 583.2f, 1767.5f, 0.0f, 546.7f, 1711.0f, 0.0f, 544.1f, 1629.8f, 0.0f, 579.9f, 1672.1f, 0.0f, 558.9f, 1457.2f, 0.0f, 572.0f, 1717.0f, 0.0f, 562.8f, 1538.9f, 0.0f, 574.3f, 1807.3f, 0.0f, 538.2f, 1830.9f, 0.0f, 571.3f, 1557.2f, 0.0f, 555.3f, 1586.2f, 0.0f, 571.8f, 1710.9f, 0.0f, 573.6f, 1858.1f, 0.0f, 578.6f, 1914.3f, 0.0f, 555.6f, 1726.2f, 0.0f, 542.3f, 1865.0f, 0.0f, 578.6f, 1770.2f, 0.0f, 538.1f, 1909.9f, 0.0f, 557.9f, 1661.5f, 0.0f, 558.6f, 1505.1f, 0.0f, 570.4f, 1575.0f, 0.0f, 509.5f, 1960.7f, 0.0f, 560.4f, 1490.4f, 0.0f, 565.4f, 1674.3f, 0.0f, 575.2f, 1737.6f, 0.0f, 576.7f, 1608.3f, 0.0f, 508.7f, 1984.1f, 0.0f, 564.1f, 1642.6f, 0.0f, 563.0f, 1531.3f, 0.0f, 568.0f, 1689.8f, 0.0f, 570.3f, 1814.5f, 0.0f, 546.3f, 1734.0f, 0.0f, 569.0f, 1824.8f, 0.0f, 523.6f, 1883.2f, 0.0f, 517.8f, 1904.4f, 0.0f, 581.3f, 1818.2f, 0.0f, 570.3f, 1826.6f, 0.0f, 526.7f, 1865.2f, 0.0f, 561.3f, 1471.8f, 0.0f, 558.2f, 1484.1f, 0.0f, 572.9f, 1650.0f, 0.0f, 575.8f, 1603.3f, 0.0f, 503.0f, 2010.3f, 0.0f, 584.7f, 1825.1f, 0.0f, 537.1f, 1810.8f, 0.0f, 610.2f, 1941.4f, 0.0f, 557.0f, 1506.9f, 0.0f, 564.0f, 1519.3f, 0.0f, 559.1f, 1454.3f, 0.0f, 582.4f, 1892.0f, 0.0f, 541.0f, 1678.7f, 0.0f, 560.7f, 1499.5f, 0.0f, 477.4f, 2035.0f, 0.0f, 556.2f, 1741.8f, 0.0f, 552.9f, 1703.7f, 0.0f, 480.6f, 2049.3f, 0.0f, 561.2f, 1479.9f, 0.0f, 554.3f, 1811.7f, 0.0f, 578.3f, 1726.0f, 0.0f, 582.7f, 1783.3f, 0.0f, 572.5f, 1776.0f, 0.0f, 548.9f, 1843.2f, 0.0f, 562.7f, 1508.4f, 0.0f, 547.2f, 1594.7f, 0.0f, 578.5f, 1901.5f, 0.0f, 570.3f, 1838.3f, 0.0f, 541.6f, 1856.1f, 0.0f, 530.4f, 1954.6f, 0.0f, 521.1f, 1936.6f, 0.0f, 564.5f, 1510.2f, 0.0f, 583.9f, 1809.3f, 0.0f, 551.8f, 1867.3f, 0.0f, 569.1f, 1673.6f, 0.0f, 557.0f, 1604.9f, 0.0f, 561.9f, 1642.5f, 0.0f, 580.8f, 1864.0f, 0.0f, 549.7f, 1618.3f, 0.0f, 556.0f, 1567.0f, 0.0f, 552.0f, 1745.5f, 0.0f, 560.0f, 1482.6f, 0.0f, 563.1f, 1620.7f, 0.0f, 548.5f, 1595.3f, 0.0f, 580.7f, 1778.8f, 0.0f, 542.5f, 1766.6f, 0.0f, 555.3f, 1716.6f, 0.0f, 545.2f, 1591.5f, 0.0f, 561.6f, 1712.2f, 0.0f, 567.3f, 1748.8f, 0.0f, 560.6f, 1497.3f, 0.0f, 556.7f, 1776.1f, 0.0f, 554.2f, 1525.4f, 0.0f, 557.8f, 1512.9f, 0.0f, 563.7f, 1651.3f, 0.0f, 576.2f, 1690.0f, 0.0f, 513.9f, 1953.8f, 0.0f, 568.5f, 1557.4f, 0.0f, 543.9f, 1686.0f, 0.0f, 580.3f, 1731.1f, 0.0f, 527.8f, 1970.4f, 0.0f, 570.0f, 1792.5f, 0.0f, 549.0f, 1611.5f, 0.0f, 555.9f, 1741.4f, 0.0f, 594.2f, 1942.1f, 0.0f, 562.8f, 1604.3f, 0.0f, 537.0f, 1825.5f, 0.0f, 559.3f, 1494.0f, 0.0f, 585.0f, 1942.0f, 0.0f, 570.9f, 1551.6f, 0.0f, 553.4f, 1741.9f, 0.0f, 574.9f, 1715.1f, 0.0f, 555.1f, 1525.5f, 0.0f, 544.1f, 1718.4f, 0.0f, 561.4f, 1455.4f, 0.0f, 533.2f, 1903.8f, 0.0f, 584.5f, 1767.0f, 0.0f, 561.7f, 1494.2f, 0.0f, 556.2f, 1523.3f, 0.0f, 581.3f, 1813.6f, 0.0f, 581.4f, 1717.1f, 0.0f, 500.7f, 1990.1f, 0.0f, 577.4f, 1772.2f, 0.0f, 612.9f, 1990.3f, 0.0f, 552.8f, 1582.4f, 0.0f, 528.1f, 1904.4f, 0.0f, 536.9f, 1725.6f, 0.0f, 563.2f, 1642.7f, 0.0f, 551.9f, 1556.7f, 0.0f, 553.8f, 1558.6f, 0.0f, 599.0f, 1927.5f, 0.0f, 542.6f, 1755.9f, 0.0f, 555.4f, 1639.3f, 0.0f, 560.9f, 1474.1f, 0.0f),
            "water" to floatArrayOf(0.0f, 2125.0f, 0.0f),
            "window" to floatArrayOf(32.5f, 1882.6f, 14.0f, 67.5f, 1882.6f, 14.0f, 102.5f, 1882.6f, 14.0f, 67.5f, 1908.8f, 14.0f, 244.8f, 1854.2f, 14.0f, 210.2f, 1915.9f, 14.0f, 210.2f, 1946.7f, 14.0f, 244.8f, 1946.7f, 14.0f, 175.8f, 1977.6f, 14.0f, 284.2f, 1919.9f, 14.0f, 393.8f, 1943.9f, 14.0f, 393.8f, 1991.9f, 14.0f, 803.2f, 1915.1f, 14.0f, 839.8f, 1915.1f, 14.0f, 730.2f, 1965.0f, 14.0f, 766.8f, 1965.0f, 14.0f, 839.8f, 1989.9f, 14.0f, 1036.2f, 1859.9f, 14.0f, 877.8f, 1889.8f, 14.0f, 909.5f, 1889.8f, 14.0f, 1004.5f, 1889.8f, 14.0f, 877.8f, 1949.6f, 14.0f, 1004.5f, 1949.6f, 14.0f, 877.8f, 1979.5f, 14.0f),
        )

    private fun london(): Map<String, FloatArray> = mapOf(
            "clock" to floatArrayOf(470.0f, 1690.0f, 19.4f),
            "lamp" to floatArrayOf(560.0f, 1892.5f, 4.4f, 680.0f, 1892.5f, 4.4f, 800.0f, 1892.5f, 4.4f, 920.0f, 1892.5f, 4.4f, 90.0f, 2140.3f, 10.4f, 560.0f, 2140.3f, 10.4f, 960.0f, 2140.3f, 10.4f),
            "lane" to floatArrayOf(0.0f, 2094.0f, 1.0f),
            "road" to floatArrayOf(505.0f, 1932.0f, 1.0f, 1032.0f, 1932.0f, 1.0f),
            "water" to floatArrayOf(0.0f, 1992.0f, 0.0f),
            "wheel" to floatArrayOf(800.0f, 1710.0f, 220.0f),
            "window" to floatArrayOf(73.0f, 1929.0f, 11.0f, 99.0f, 1881.0f, 11.0f, 99.0f, 1964.0f, 11.0f, 125.0f, 1881.0f, 11.0f, 125.0f, 1929.0f, 11.0f, 151.0f, 1881.0f, 11.0f, 203.0f, 1964.0f, 11.0f, 229.0f, 1964.0f, 11.0f, 255.0f, 1929.0f, 11.0f, 281.0f, 1881.0f, 11.0f, 307.0f, 1881.0f, 11.0f, 307.0f, 1929.0f, 11.0f, 307.0f, 1964.0f, 11.0f, 359.0f, 1881.0f, 11.0f, 385.0f, 1881.0f, 11.0f, 385.0f, 1929.0f, 11.0f, 411.0f, 1881.0f, 11.0f, 437.0f, 1929.0f, 11.0f, 437.0f, 1964.0f, 11.0f, 463.0f, 1929.0f, 11.0f, 489.0f, 1881.0f, 11.0f, 489.0f, 1964.0f, 11.0f),
        )

    private fun vegas(): Map<String, FloatArray> = mapOf(
            "antenna" to floatArrayOf(80.0f, 1430.0f, 0.0f, 375.0f, 1470.0f, 0.0f, 600.0f, 1592.0f, 0.0f),
            "beam" to floatArrayOf(886.0f, 1710.0f, 0.0f),
            "bulb" to floatArrayOf(45.0f, 2032.0f, 16764746.0f, 62.9f, 2022.9f, 16774870.0f, 80.7f, 2013.7f, 16764746.0f, 98.6f, 2004.6f, 16774870.0f, 116.4f, 1995.4f, 16764746.0f, 134.3f, 1986.3f, 16774870.0f, 152.1f, 1977.1f, 16764746.0f, 170.0f, 1968.0f, 16774870.0f, 187.9f, 1977.1f, 16764746.0f, 205.7f, 1986.3f, 16774870.0f, 223.6f, 1995.4f, 16764746.0f, 241.4f, 2004.6f, 16774870.0f, 259.3f, 2013.7f, 16764746.0f, 277.1f, 2022.9f, 16774870.0f, 295.0f, 2032.0f, 16764746.0f, 277.1f, 2041.1f, 16774870.0f, 259.3f, 2050.3f, 16764746.0f, 241.4f, 2059.4f, 16774870.0f, 223.6f, 2068.6f, 16764746.0f, 205.7f, 2077.7f, 16774870.0f, 187.9f, 2086.9f, 16764746.0f, 170.0f, 2096.0f, 16774870.0f, 152.1f, 2086.9f, 16764746.0f, 134.3f, 2077.7f, 16774870.0f, 116.4f, 2068.6f, 16764746.0f, 98.6f, 2059.4f, 16774870.0f, 80.7f, 2050.3f, 16764746.0f, 62.9f, 2041.1f, 16774870.0f),
            "citywin" to floatArrayOf(2.0f, 1860.0f, 2758224.0f, 176.0f, 1859.0f, 2758224.0f, 146.0f, 1895.0f, 2758224.0f, 580.0f, 1812.0f, 2758224.0f, 610.0f, 1830.0f, 2758224.0f, 699.0f, 1926.0f, 2758224.0f, 763.0f, 1964.0f, 2758224.0f, 927.0f, 1882.0f, 2758224.0f),
            "fountain" to floatArrayOf(340.0f, 2092.0f, 140.0f, 407.5f, 2092.0f, 160.0f, 475.0f, 2092.0f, 180.0f, 542.5f, 2092.0f, 200.0f, 610.0f, 2092.0f, 220.0f, 677.5f, 2092.0f, 200.0f, 745.0f, 2092.0f, 180.0f, 812.5f, 2092.0f, 160.0f, 880.0f, 2092.0f, 140.0f),
            "neon" to floatArrayOf(80.0f, 1534.0f, 16732067.0f, 165.0f, 1566.0f, 16764746.0f, 220.0f, 1546.0f, 16764746.0f, 276.0f, 1550.0f, 16764746.0f, 322.0f, 1540.0f, 16732067.0f, 345.0f, 1514.0f, 16732067.0f, 390.0f, 1506.0f, 16732067.0f, 428.0f, 1530.0f, 16732067.0f, 494.0f, 1586.0f, 3860479.0f, 580.0f, 1616.0f, 3860479.0f, 647.3f, 1918.6f, 16732067.0f, 668.7f, 1918.6f, 16732067.0f, 690.0f, 1918.6f, 16732067.0f, 711.3f, 1918.6f, 16732067.0f, 732.7f, 1918.6f, 16732067.0f),
            "road" to floatArrayOf(0.0f, 2216.0f, 1.0f, 1032.0f, 2216.0f, 1.0f),
            "search" to floatArrayOf(220.0f, 1542.0f, 0.0f, 494.0f, 1584.0f, 0.0f, 668.0f, 1692.0f, 0.0f),
            "water" to floatArrayOf(0.0f, 2014.0f, 0.0f),
        )

    private fun rainy(): Map<String, FloatArray> = mapOf(
            "lamp" to floatArrayOf(190.0f, 1906.0f, 20.0f, 432.0f, 1906.0f, 20.0f, 1000.0f, 1906.0f, 20.0f),
            "neon" to floatArrayOf(546.0f, 1870.2f, 16732067.0f, 561.2f, 1870.2f, 16732067.0f, 576.3f, 1870.2f, 16732067.0f, 591.4f, 1870.2f, 16732067.0f, 606.6f, 1870.2f, 16732067.0f, 621.7f, 1870.2f, 16732067.0f, 636.9f, 1870.2f, 16732067.0f, 652.0f, 1870.2f, 16732067.0f, 720.8f, 1999.7f, 16734810.0f, 737.2f, 1999.7f, 16734810.0f, 861.2f, 1926.1f, 3860479.0f, 897.0f, 1926.1f, 3860479.0f, 932.8f, 1926.1f, 3860479.0f),
            "puddle" to floatArrayOf(160.0f, 2180.0f, 120.0f, 430.0f, 2216.0f, 150.0f, 700.0f, 2172.0f, 130.0f, 910.0f, 2214.0f, 140.0f, 560.0f, 2110.0f, 70.0f, 330.0f, 2114.0f, 56.0f),
            "walk" to floatArrayOf(0.0f, 2118.0f, 1.0f, 1032.0f, 2118.0f, 1.0f),
            "window" to floatArrayOf(35.0f, 1573.0f, 34.0f, 97.0f, 1573.0f, 34.0f, 159.0f, 1573.0f, 34.0f, 97.0f, 1651.0f, 34.0f, 159.0f, 1651.0f, 34.0f, 35.0f, 1729.0f, 34.0f, 35.0f, 1807.0f, 34.0f, 97.0f, 1807.0f, 34.0f, 159.0f, 1807.0f, 34.0f, 35.0f, 1885.0f, 34.0f, 246.0f, 1652.0f, 40.0f, 316.0f, 1728.0f, 40.0f, 386.0f, 1728.0f, 40.0f, 246.0f, 1804.0f, 40.0f, 316.0f, 1804.0f, 40.0f, 386.0f, 1880.0f, 40.0f, 561.0f, 1616.0f, 42.0f, 639.0f, 1616.0f, 42.0f, 483.0f, 1708.0f, 42.0f, 717.0f, 1708.0f, 42.0f, 483.0f, 1800.0f, 42.0f, 639.0f, 1800.0f, 42.0f, 717.0f, 1800.0f, 42.0f, 893.0f, 1582.0f, 38.0f, 969.0f, 1582.0f, 38.0f, 817.0f, 1658.0f, 38.0f, 893.0f, 1658.0f, 38.0f, 817.0f, 1734.0f, 38.0f, 969.0f, 1734.0f, 38.0f, 817.0f, 1810.0f, 38.0f, 893.0f, 1810.0f, 38.0f, 969.0f, 1810.0f, 38.0f),
        )

    private fun reef(): Map<String, FloatArray> = mapOf(
            "bubble" to floatArrayOf(668.0f, 2092.0f, 0.0f, 225.0f, 1800.0f, 0.0f, 430.0f, 1940.0f, 0.0f, 975.0f, 2020.0f, 0.0f, 840.0f, 2170.0f, 0.0f),
            "kelp" to floatArrayOf(12.0f, 2172.0f, 400.0f, 330.0f, 1928.0f, 260.0f, 468.0f, 1936.0f, 230.0f, 575.0f, 1920.0f, 190.0f, 724.0f, 1878.0f, 240.0f, 1020.0f, 2180.0f, 420.0f),
            "sand" to floatArrayOf(0.0f, 1980.0f, 0.0f),
        )

    private fun volcano(): Map<String, FloatArray> = mapOf(
            "crater" to floatArrayOf(490.0f, 1684.0f, 116.0f),
            "glow" to floatArrayOf(490.0f, 1678.0f, 160.0f),
            "lava1" to floatArrayOf(535.0f, 1696.0f, 8.0f, 556.0f, 1730.0f, 9.0f, 574.0f, 1770.0f, 10.0f, 590.0f, 1810.0f, 11.0f, 612.0f, 1848.0f, 12.0f, 636.0f, 1885.0f, 13.0f, 660.0f, 1920.0f, 14.0f, 684.0f, 1955.0f, 15.0f, 700.0f, 1988.0f, 16.0f, 712.0f, 2010.0f, 17.0f, 716.0f, 2026.0f, 18.0f),
            "lava2" to floatArrayOf(492.0f, 1698.0f, 8.0f, 490.0f, 1740.0f, 9.0f, 496.0f, 1785.0f, 10.0f, 506.0f, 1830.0f, 11.0f, 520.0f, 1875.0f, 12.0f, 538.0f, 1920.0f, 13.0f, 558.0f, 1965.0f, 14.0f, 578.0f, 2010.0f, 15.0f, 598.0f, 2055.0f, 16.0f, 612.0f, 2098.0f, 17.0f, 624.0f, 2132.0f, 18.0f),
            "lava3" to floatArrayOf(452.0f, 1698.0f, 7.0f, 436.0f, 1732.0f, 8.0f, 414.0f, 1772.0f, 9.0f, 390.0f, 1810.0f, 10.0f, 364.0f, 1846.0f, 11.0f, 336.0f, 1880.0f, 12.0f, 306.0f, 1912.0f, 13.0f, 278.0f, 1940.0f, 14.0f, 256.0f, 1964.0f, 15.0f, 240.0f, 1982.0f, 16.0f, 232.0f, 1996.0f, 18.0f),
            "plume" to floatArrayOf(490.0f, 1664.0f, 260.0f),
            "steam" to floatArrayOf(716.0f, 2026.0f, 1.0f, 624.0f, 2132.0f, 1.0f),
            "water" to floatArrayOf(0.0f, 1936.0f, 0.0f),
        )

    private fun space(): Map<String, FloatArray> = mapOf(
            "antenna" to floatArrayOf(277.0f, 1784.0f, 0.0f),
            "astro" to floatArrayOf(470.0f, 1680.0f, 90.0f),
            "earth" to floatArrayOf(780.0f, 1710.0f, 150.0f),
        )

    private fun retro(): Map<String, FloatArray> = mapOf(
            "horizon" to floatArrayOf(516.0f, 1810.0f, 0.0f),
            "sky" to floatArrayOf(0.0f, 1380.0f, 4985190.0f, 0.0f, 1810.0f, 12727402.0f),
            "sun" to floatArrayOf(516.0f, 1680.0f, 200.0f),
        )
}
