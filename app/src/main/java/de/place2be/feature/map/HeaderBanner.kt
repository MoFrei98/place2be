package de.place2be.feature.map

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.place2be.ui.theme.DarkInk
import de.place2be.ui.theme.LeafAccent
import de.place2be.ui.theme.LeafSurface
import kotlinx.coroutines.delay

@Composable
internal fun HeaderBanner(
    places: List<MapPlaceUiState>,
    onItemClick: (HeaderBannerItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember(places) { buildHeaderBannerItems(places) }
    val reducedMotion = reducedMotionRequested()
    val density = LocalDensity.current.density
    val flipRotation = remember { Animatable(0f) }
    var targetIndex by rememberSaveable { mutableIntStateOf(0) }
    var displayedIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(items.size, reducedMotion) {
        targetIndex = targetIndex.coerceIn(items.indices)
        displayedIndex = displayedIndex.coerceIn(items.indices)
        if (reducedMotion) {
            targetIndex = 0
            displayedIndex = 0
            flipRotation.snapTo(0f)
            return@LaunchedEffect
        }
        if (items.size <= 1) return@LaunchedEffect

        while (true) {
            delay(HEADER_BANNER_ROTATION_MILLIS)
            targetIndex = nextHeaderBannerIndex(
                currentIndex = targetIndex,
                itemCount = items.size,
                rotationEnabled = true,
            )
        }
    }

    LaunchedEffect(targetIndex, reducedMotion) {
        if (reducedMotion) {
            displayedIndex = 0
            flipRotation.snapTo(0f)
            return@LaunchedEffect
        }
        if (displayedIndex == targetIndex) return@LaunchedEffect

        flipRotation.animateTo(
            targetValue = -FLIP_HALF_TURN_DEGREES,
            animationSpec = tween(
                durationMillis = FLIP_HALF_DURATION_MILLIS,
                easing = FastOutLinearInEasing,
            ),
        )
        displayedIndex = targetIndex
        flipRotation.snapTo(FLIP_HALF_TURN_DEGREES)
        flipRotation.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = FLIP_HALF_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        )
    }

    val item = items[displayedIndex.coerceIn(items.indices)]
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationX = flipRotation.value
                cameraDistance = FLIP_CAMERA_DISTANCE_DP * density
                transformOrigin = TransformOrigin.Center
            }
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "${item.label}: ${item.message} ${item.actionDescription()}."
            },
        onClick = { onItemClick(item) },
        shape = RoundedCornerShape(13.dp),
        color = LeafSurface.copy(alpha = 0.78f),
        contentColor = DarkInk,
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label.uppercase(),
                    color = LeafAccent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                )
                Text(
                    text = item.message,
                    color = DarkInk,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "›",
                color = LeafAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun reducedMotionRequested(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                DEFAULT_ANIMATOR_DURATION_SCALE,
            ) == 0f
        }.getOrDefault(false)
    }
}

private const val HEADER_BANNER_ROTATION_MILLIS = 7_000L
private const val FLIP_HALF_DURATION_MILLIS = 600
private const val FLIP_HALF_TURN_DEGREES = 90f
private const val FLIP_CAMERA_DISTANCE_DP = 18f
private const val DEFAULT_ANIMATOR_DURATION_SCALE = 1f
