package io.github.expo.peek.notification

import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews

/** Sizes used to compose the three notification surfaces. */
public data class PeekNotificationSizes(
  val collapsed: DpSize = DpSize(360.dp, 64.dp),
  val expanded: DpSize = DpSize(360.dp, 256.dp),
  val headsUp: DpSize = DpSize(360.dp, 88.dp),
)

/** A fully composed, atomic set of custom notification surfaces. */
public data class PeekNotificationViews(
  val collapsed: RemoteViews,
  val expanded: RemoteViews? = null,
  val headsUp: RemoteViews? = null,
)

internal interface NotificationComposer {
  suspend fun compose(
    context: Context,
    size: DpSize,
    state: Any?,
    options: Bundle,
    content: @Composable () -> Unit,
  ): RemoteViews
}

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
private class GlanceNotificationComposer : NotificationComposer {
  private val glanceRemoteViews = GlanceRemoteViews()

  override suspend fun compose(
    context: Context,
    size: DpSize,
    state: Any?,
    options: Bundle,
    content: @Composable () -> Unit,
  ): RemoteViews = glanceRemoteViews.compose(
    context = context,
    size = size,
    state = state,
    appWidgetOptions = options,
    content = content,
  ).remoteViews
}

/**
 * Composes every requested notification surface through Glance's public experimental RemoteViews
 * API. A single [GlanceRemoteViews] instance is reused and surfaces are composed sequentially.
 */
public suspend fun peekNotificationViews(
  context: Context,
  collapsed: @Composable () -> Unit,
  expanded: (@Composable () -> Unit)? = null,
  headsUp: (@Composable () -> Unit)? = null,
  state: Any? = null,
  appWidgetOptions: Bundle = Bundle(),
  sizes: PeekNotificationSizes = PeekNotificationSizes(),
): PeekNotificationViews = peekNotificationViews(
  context = context,
  collapsed = collapsed,
  expanded = expanded,
  headsUp = headsUp,
  state = state,
  appWidgetOptions = appWidgetOptions,
  sizes = sizes,
  composer = GlanceNotificationComposer(),
)

internal suspend fun peekNotificationViews(
  context: Context,
  collapsed: @Composable () -> Unit,
  expanded: (@Composable () -> Unit)? = null,
  headsUp: (@Composable () -> Unit)? = null,
  state: Any? = null,
  appWidgetOptions: Bundle = Bundle(),
  sizes: PeekNotificationSizes = PeekNotificationSizes(),
  composer: NotificationComposer,
): PeekNotificationViews {
  val collapsedView = composer.compose(
    context,
    sizes.collapsed,
    state,
    appWidgetOptions,
    collapsed,
  )
  val expandedView = expanded?.let {
    composer.compose(context, sizes.expanded, state, appWidgetOptions, it)
  }
  val headsUpView = headsUp?.let {
    composer.compose(context, sizes.headsUp, state, appWidgetOptions, it)
  }
  return PeekNotificationViews(collapsedView, expandedView, headsUpView)
}
