@file:Suppress("RestrictedApiAndroidX")

package io.github.expo.peek.glance

import androidx.compose.runtime.Composable
import androidx.glance.Emittable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceNode
import androidx.glance.appwidget.ProgressIndicatorDefaults
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp

/**
 * A determinate circular progress indicator for Glance surfaces.
 *
 * Finite progress values are clamped to `0f..1f`. Unlike Glance's indeterminate circular
 * indicator, this extension renders a platform determinate ProgressBar for RemoteViews.
 */
@Composable
public fun CircularProgressIndicator(
  progress: Float,
  modifier: GlanceModifier = GlanceModifier,
  color: ColorProvider = ProgressIndicatorDefaults.IndicatorColorProvider,
  trackColor: ColorProvider = ProgressIndicatorDefaults.BackgroundColorProvider,
) {
  val normalizedProgress = normalizeProgress(progress)
  val normalizedModifier = GlanceModifier.size(32.dp).then(modifier)
  GlanceNode(
    factory = ::EmittableDeterminateCircularProgressIndicator,
    update = {
      set(normalizedProgress) { this.progress = it }
      set(normalizedModifier) { this.modifier = it }
      set(color) { this.color = it }
      set(trackColor) { this.trackColor = it }
    },
  )
}

/** Creates the Peek extension node without rebuilding it through the composable DSL. */
public fun determinateCircularProgressIndicatorEmittable(
  progress: Float,
  modifier: GlanceModifier = GlanceModifier,
  color: ColorProvider = ProgressIndicatorDefaults.IndicatorColorProvider,
  trackColor: ColorProvider = ProgressIndicatorDefaults.BackgroundColorProvider,
): Emittable =
  EmittableDeterminateCircularProgressIndicator().also {
    it.progress = normalizeProgress(progress)
    it.modifier = GlanceModifier.size(32.dp).then(modifier)
    it.color = color
    it.trackColor = trackColor
  }

internal fun normalizeProgress(progress: Float): Float {
  require(progress.isFinite()) { "progress must be finite, but was $progress" }
  return progress.coerceIn(0f, 1f)
}
