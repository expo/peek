package io.github.expo.peek.notification

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationCompat

/**
 * Composes normal Glance content for every requested surface, then atomically attaches the complete
 * set to this notification builder.
 */
public suspend fun NotificationCompat.Builder.setPeekContent(
  context: Context,
  collapsed: @Composable () -> Unit,
  expanded: (@Composable () -> Unit)? = null,
  headsUp: (@Composable () -> Unit)? = null,
  state: Any? = null,
  appWidgetOptions: Bundle = Bundle(),
  sizes: PeekNotificationSizes = PeekNotificationSizes(),
  decorated: Boolean = true,
  ongoing: Boolean = true,
  actions: List<PeekNotificationAction> = emptyList(),
): NotificationCompat.Builder {
  val views = peekNotificationViews(
    context = context,
    collapsed = collapsed,
    expanded = expanded,
    headsUp = headsUp,
    state = state,
    appWidgetOptions = appWidgetOptions,
    sizes = sizes,
  )
  return setPeekContent(views, decorated, ongoing, actions)
}

internal suspend fun NotificationCompat.Builder.setPeekContent(
  context: Context,
  collapsed: @Composable () -> Unit,
  expanded: (@Composable () -> Unit)? = null,
  headsUp: (@Composable () -> Unit)? = null,
  state: Any? = null,
  appWidgetOptions: Bundle = Bundle(),
  sizes: PeekNotificationSizes = PeekNotificationSizes(),
  decorated: Boolean = true,
  ongoing: Boolean = true,
  actions: List<PeekNotificationAction> = emptyList(),
  composer: NotificationComposer,
): NotificationCompat.Builder {
  val views = peekNotificationViews(
    context = context,
    collapsed = collapsed,
    expanded = expanded,
    headsUp = headsUp,
    state = state,
    appWidgetOptions = appWidgetOptions,
    sizes = sizes,
    composer = composer,
  )
  return setPeekContent(views, decorated, ongoing, actions)
}

/**
 * Attaches already-composed [views] to the notification.
 *
 * Synchronous; performs no composition. Build [views] with [peekNotificationViews] from a coroutine.
 */
public fun NotificationCompat.Builder.setPeekContent(
  views: PeekNotificationViews,
  decorated: Boolean = true,
  ongoing: Boolean = true,
  actions: List<PeekNotificationAction> = emptyList(),
): NotificationCompat.Builder {
  setCustomContentView(views.collapsed)
  views.expanded?.let(::setCustomBigContentView)
  views.headsUp?.let(::setCustomHeadsUpContentView)
  if (decorated) {
    setStyle(NotificationCompat.DecoratedCustomViewStyle())
  }
  setOngoing(ongoing)
  addPeekActions(actions)
  return this
}
