@file:Suppress("RestrictedApiAndroidX")

package io.github.expo.peek.emittables

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationCompat
import androidx.glance.Emittable
import io.github.expo.peek.notification.PeekNotificationAction
import io.github.expo.peek.notification.PeekNotificationSizes
import io.github.expo.peek.notification.PeekNotificationViews
import io.github.expo.peek.notification.peekNotificationViews
import io.github.expo.peek.notification.setPeekContent

/**
 * Composes notification surfaces from prebuilt Glance emittable trees. Each tree goes through
 * [EmittableTree], so the same ownership and supported-node rules apply.
 */
public suspend fun peekEmittableNotificationViews(
  context: Context,
  collapsed: Emittable,
  expanded: Emittable? = null,
  headsUp: Emittable? = null,
  state: Any? = null,
  appWidgetOptions: Bundle = Bundle(),
  sizes: PeekNotificationSizes = PeekNotificationSizes(),
): PeekNotificationViews = peekNotificationViews(
  context = context,
  collapsed = { EmittableTree(collapsed) },
  expanded = expanded?.asContent(),
  headsUp = headsUp?.asContent(),
  state = state,
  appWidgetOptions = appWidgetOptions,
  sizes = sizes,
)

/** Composes the surfaces from emittable trees and attaches them like [setPeekContent]. */
public suspend fun NotificationCompat.Builder.setPeekEmittableContent(
  context: Context,
  collapsed: Emittable,
  expanded: Emittable? = null,
  headsUp: Emittable? = null,
  state: Any? = null,
  appWidgetOptions: Bundle = Bundle(),
  sizes: PeekNotificationSizes = PeekNotificationSizes(),
  decorated: Boolean = true,
  ongoing: Boolean = true,
  actions: List<PeekNotificationAction> = emptyList(),
): NotificationCompat.Builder {
  val views = peekEmittableNotificationViews(
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

private fun Emittable.asContent(): @Composable () -> Unit = { EmittableTree(this) }
