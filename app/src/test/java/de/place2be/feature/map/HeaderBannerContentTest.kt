package de.place2be.feature.map

import de.place2be.domain.model.PlaceCategory
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeaderBannerContentTest {
    @Test
    fun `popular hint uses highest rated reviewed place`() {
        val mainufer = mapPlace(name = "Mainufer", score = 4.4, reviewCount = 10)
        val bethmannpark = mapPlace(name = "Bethmannpark", score = 4.8, reviewCount = 5)
        val unreviewedPlace = mapPlace(name = "Ohne Reviews", score = 5.0, reviewCount = 0)
        val items = buildHeaderBannerItems(
            listOf(mainufer, bethmannpark, unreviewedPlace),
        )

        assertEquals("Gerade beliebt", items.first().label)
        assertEquals("Bethmannpark · 4,8 ★", items.first().message)
        assertEquals(HeaderBannerDestination.PLACE, items.first().destination)
        assertEquals(bethmannpark.uuid, items.first().placeUuid)
    }

    @Test
    fun `bookmark hint reflects local bookmark count`() {
        val items = buildHeaderBannerItems(
            listOf(
                mapPlace(name = "Mainufer", bookmarkedAtMillis = 1L),
                mapPlace(name = "Bethmannpark", bookmarkedAtMillis = 2L),
                mapPlace(name = "Goetheplatz"),
            ),
        )

        assertTrue(
            items.any { item ->
                item.label == "Gespeichert" &&
                    item.message == "2 gespeicherte Orte warten auf dich."
            },
        )
    }

    @Test
    fun `feature and saved hints link to their matching panels`() {
        val items = buildHeaderBannerItems(listOf(mapPlace(name = "Mainufer")))

        assertEquals(
            HeaderBannerDestination.FILTER,
            items.first { it.label == "Feature-Tipp" }.destination,
        )
        assertEquals(
            HeaderBannerDestination.SAVED,
            items.first { it.label == "Gespeichert" }.destination,
        )
    }

    @Test
    fun `community hint links to place with fewest reviews`() {
        val manyReviews = mapPlace(name = "Mainufer", reviewCount = 8)
        val fewReviews = mapPlace(name = "Goetheplatz", reviewCount = 1)
        val item = buildHeaderBannerItems(listOf(manyReviews, fewReviews))
            .first { it.label == "Community-Tipp" }

        assertEquals(HeaderBannerDestination.PLACE, item.destination)
        assertEquals(fewReviews.uuid, item.placeUuid)
        assertEquals("Goetheplatz braucht aktuelle Eindrücke.", item.message)
    }

    @Test
    fun `disabled rotation keeps current hint static`() {
        assertEquals(
            2,
            nextHeaderBannerIndex(
                currentIndex = 2,
                itemCount = 4,
                rotationEnabled = false,
            ),
        )
    }

    @Test
    fun `rotation wraps after last hint`() {
        assertEquals(
            0,
            nextHeaderBannerIndex(
                currentIndex = 3,
                itemCount = 4,
                rotationEnabled = true,
            ),
        )
    }

    private fun mapPlace(
        name: String,
        score: Double = 4.0,
        reviewCount: Int = 1,
        bookmarkedAtMillis: Long? = null,
    ) = MapPlaceUiState(
        uuid = UUID.randomUUID(),
        name = name,
        description = "$name Beschreibung",
        category = PlaceCategory.PARK,
        categoryLabel = "Park",
        locationHint = "Frankfurt am Main",
        attributes = emptySet(),
        currentScore = score,
        vibeScore = score,
        safetyScore = score,
        accessibilityScore = score,
        reviewCount = reviewCount,
        recentReviewCount = reviewCount,
        mapXFraction = 0.5f,
        mapYFraction = 0.5f,
        bookmarkedAtMillis = bookmarkedAtMillis,
        canRate = true,
        ratingEligibilityMessage = "",
    )

}
