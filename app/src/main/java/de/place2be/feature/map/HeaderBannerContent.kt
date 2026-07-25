package de.place2be.feature.map

import java.util.Locale
import java.util.UUID

internal data class HeaderBannerItem(
    val label: String,
    val message: String,
    val destination: HeaderBannerDestination,
    val placeUuid: UUID? = null,
)

internal enum class HeaderBannerDestination {
    PLACE,
    FILTER,
    SAVED,
}

/**
 * Erzeugt die lokale, nicht kommerzielle Banner-Rotation fuer den MVP.
 *
 * Alle Inhalte entstehen aus bereits geladenen Kartendaten oder statischen
 * Produkthinweisen. Es gibt weder einen Netzwerkabruf noch Impression- oder
 * Klick-Tracking.
 */
internal fun buildHeaderBannerItems(places: List<MapPlaceUiState>): List<HeaderBannerItem> {
    val mostPopularPlace = places
        .filter(MapPlaceUiState::hasReviews)
        .maxWithOrNull(
            compareBy<MapPlaceUiState>(MapPlaceUiState::currentScore)
                .thenBy(MapPlaceUiState::reviewCount),
        )
    val bookmarkedPlaceCount = places.count(MapPlaceUiState::isBookmarked)
    val communityPlace = places.minWithOrNull(
        compareBy<MapPlaceUiState>(MapPlaceUiState::reviewCount)
            .thenBy(MapPlaceUiState::name),
    )

    return listOf(
        HeaderBannerItem(
            label = "Gerade beliebt",
            message = mostPopularPlace?.let { place ->
                "${place.name} · ${String.format(Locale.GERMANY, "%.1f", place.currentScore)} ★"
            } ?: "Entdecke den ersten Ort auf der Karte.",
            destination = if (mostPopularPlace != null) {
                HeaderBannerDestination.PLACE
            } else {
                HeaderBannerDestination.FILTER
            },
            placeUuid = mostPopularPlace?.uuid,
        ),
        HeaderBannerItem(
            label = "Feature-Tipp",
            message = "Finde passende Orte mit den Filtern.",
            destination = HeaderBannerDestination.FILTER,
        ),
        HeaderBannerItem(
            label = "Gespeichert",
            message = when (bookmarkedPlaceCount) {
                0 -> "Lieblingsorte mit dem Herz speichern."
                1 -> "Ein gespeicherter Ort wartet auf dich."
                else -> "$bookmarkedPlaceCount gespeicherte Orte warten auf dich."
            },
            destination = HeaderBannerDestination.SAVED,
        ),
        HeaderBannerItem(
            label = "Community-Tipp",
            message = communityPlace?.let { place ->
                "${place.name} braucht aktuelle Eindrücke."
            } ?: "Aktuelle Eindrücke halten Orts-Scores relevant.",
            destination = if (communityPlace != null) {
                HeaderBannerDestination.PLACE
            } else {
                HeaderBannerDestination.FILTER
            },
            placeUuid = communityPlace?.uuid,
        ),
    )
}

internal fun HeaderBannerItem.actionDescription(): String = when (destination) {
    HeaderBannerDestination.PLACE -> "Ort öffnen"
    HeaderBannerDestination.FILTER -> "Filter öffnen"
    HeaderBannerDestination.SAVED -> "Gespeicherte Orte öffnen"
}

internal fun nextHeaderBannerIndex(
    currentIndex: Int,
    itemCount: Int,
    rotationEnabled: Boolean,
): Int {
    if (!rotationEnabled || itemCount <= 1) return currentIndex.coerceAtLeast(0)
    return (currentIndex + 1).mod(itemCount)
}
