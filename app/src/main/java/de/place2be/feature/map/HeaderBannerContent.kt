package de.place2be.feature.map

internal data class HeaderBannerItem(
    val label: String,
    val message: String,
    val palette: HeaderBannerPalette,
)

internal enum class HeaderBannerPalette {
    CYAN,
    PINK,
}

/**
 * Lokale Mock-Anzeigen fuer die Banner-Demo.
 *
 * Die Marken sind frei erfunden. Es gibt weder Netzwerkabrufe noch Tracking,
 * externe Links oder Verweise auf Funktionen innerhalb der App.
 */
internal fun buildHeaderBannerItems(): List<HeaderBannerItem> = listOf(
    HeaderBannerItem(
        label = "Anzeige",
        message = "CityCycle – urbane Fahrräder für jeden Weg.",
        palette = HeaderBannerPalette.CYAN,
    ),
    HeaderBannerItem(
        label = "Anzeige",
        message = "PulseFuel – Protein für deinen nächsten Schritt.",
        palette = HeaderBannerPalette.PINK,
    ),
    HeaderBannerItem(
        label = "Anzeige",
        message = "TrailNest – Rucksäcke für Stadt und Natur.",
        palette = HeaderBannerPalette.CYAN,
    ),
    HeaderBannerItem(
        label = "Werbeplatz",
        message = "Hier könnte Ihre Werbung stehen!",
        palette = HeaderBannerPalette.PINK,
    ),
)

internal fun nextHeaderBannerIndex(
    currentIndex: Int,
    itemCount: Int,
    rotationEnabled: Boolean,
): Int {
    if (!rotationEnabled || itemCount <= 1) return currentIndex.coerceAtLeast(0)
    return (currentIndex + 1).mod(itemCount)
}
