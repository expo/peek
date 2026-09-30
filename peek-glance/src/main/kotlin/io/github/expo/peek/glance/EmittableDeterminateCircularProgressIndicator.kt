@file:Suppress(
  "INVISIBLE_MEMBER",
  "INVISIBLE_REFERENCE",
  "RestrictedApiAndroidX",
)

package io.github.expo.peek.glance

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.glance.Emittable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.ProgressIndicatorDefaults
import androidx.glance.appwidget.RemoteViewsTranslatable
import androidx.glance.unit.ColorProvider
import kotlin.math.roundToInt

/** Marker for Peek-owned nodes accepted by the direct-emittable bridge. */
public interface PeekGlanceEmittable : Emittable

internal class EmittableDeterminateCircularProgressIndicator :
  PeekGlanceEmittable,
  RemoteViewsTranslatable {

  override var modifier: GlanceModifier = GlanceModifier
  public var progress: Float = 0f
  public var color: ColorProvider = ProgressIndicatorDefaults.IndicatorColorProvider
  public var trackColor: ColorProvider = ProgressIndicatorDefaults.BackgroundColorProvider

  override fun createRemoteViews(context: Context): RemoteViews =
    RemoteViews(context.packageName, R.layout.peek_determinate_circular_progress).apply {
      setProgressBar(
        R.id.peek_progress,
        ProgressMax,
        (normalizeProgress(progress) * ProgressMax).roundToInt(),
        false,
      )
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setColorStateList(
          R.id.peek_progress,
          "setProgressTintList",
          ColorStateList.valueOf(color.getColor(context).toArgb()),
        )
        setColorStateList(
          R.id.peek_progress,
          "setProgressBackgroundTintList",
          ColorStateList.valueOf(trackColor.getColor(context).toArgb()),
        )
      }
    }

  override fun copy(): Emittable =
    EmittableDeterminateCircularProgressIndicator().also {
      it.modifier = modifier
      it.progress = progress
      it.color = color
      it.trackColor = trackColor
    }

  override fun toString(): String =
    "EmittableDeterminateCircularProgressIndicator(" +
      "modifier=$modifier, progress=$progress, color=$color, trackColor=$trackColor)"

  private companion object {
    const val ProgressMax = 100
  }
}
