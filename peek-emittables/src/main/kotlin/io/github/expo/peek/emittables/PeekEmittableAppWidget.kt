@file:Suppress("RestrictedApiAndroidX")

package io.github.expo.peek.emittables

import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.unit.DpSize
import androidx.glance.Emittable
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.LocalAppWidgetOptions
import androidx.glance.appwidget.R as GlanceAppWidgetR
import androidx.glance.appwidget.multiprocess.MultiProcessGlanceAppWidget
import androidx.glance.appwidget.provideContent
import java.util.concurrent.ConcurrentHashMap

private val revisions = ConcurrentHashMap<Int, MutableIntState>()
internal fun widgetRevision(appWidgetId: Int): MutableIntState =
  revisions.getOrPut(appWidgetId) { mutableIntStateOf(0) }

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

  /**
   * Invalidates [provideRoot] after its external data has been updated, then requests an update.
   * Adapter instances share an in-memory revision per widget ID.
   */
  public suspend fun refresh(context: Context, id: AppWidgetId) {
    require(id.appWidgetId > 0) { "Invalid app widget ID: ${id.appWidgetId}" }
    val revision = widgetRevision(id.appWidgetId)
    synchronized(revision) { revision.intValue++ }
    update(context, id)
  }

  final override suspend fun provideGlance(context: Context, id: GlanceId) {
    require(id is AppWidgetId) { "PeekEmittableAppWidget requires an AppWidgetId, got $id" }
    provideContent {
      key(widgetRevision(id.appWidgetId).intValue) {
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

  @CallSuper
  override suspend fun onDelete(context: Context, glanceId: GlanceId) {
    if (glanceId is AppWidgetId) revisions.remove(glanceId.appWidgetId)
    super.onDelete(context, glanceId)
  }
}
