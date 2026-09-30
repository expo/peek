@file:Suppress("RestrictedApiAndroidX")

package io.github.expo.peek.emittables

import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.compose.ui.unit.DpSize
import androidx.glance.Emittable
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.LocalAppWidgetOptions
import androidx.glance.appwidget.R as GlanceAppWidgetR
import androidx.glance.appwidget.multiprocess.MultiProcessGlanceAppWidget
import androidx.glance.appwidget.provideContent

/**
 * Official multiprocess Glance AppWidget whose content is supplied as a direct emittable tree.
 *
 * State, sizing, previews, sessions, and testing remain owned by Glance.
 */
public abstract class PeekEmittableAppWidget(
  @LayoutRes errorUiLayout: Int = GlanceAppWidgetR.layout.glance_error_layout,
) : MultiProcessGlanceAppWidget(errorUiLayout) {

  public abstract fun provideRoot(
    context: Context,
    id: AppWidgetId,
    options: Bundle,
    size: DpSize,
  ): Emittable

  final override suspend fun provideGlance(context: Context, id: GlanceId) {
    require(id is AppWidgetId) { "PeekEmittableAppWidget requires an AppWidgetId, got $id" }
    provideContent {
      EmittableTree(
        provideRoot(
          context = context,
          id = id,
          options = LocalAppWidgetOptions.current,
          size = LocalSize.current,
        )
      )
    }
  }
}
