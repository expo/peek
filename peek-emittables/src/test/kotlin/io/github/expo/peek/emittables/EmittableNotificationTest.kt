@file:Suppress("RestrictedApiAndroidX")

package io.github.expo.peek.emittables

import android.app.Notification
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.glance.layout.EmittableColumn
import androidx.glance.text.EmittableText
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class EmittableNotificationTest {
  private val context = ApplicationProvider.getApplicationContext<Context>()

  private fun text(value: String) = EmittableColumn().also { column ->
    column.children += EmittableText().also { it.text = value }
  }

  @Test
  fun composesEverySurfaceFromPrebuiltEmittables() = runBlocking {
    val views = peekEmittableNotificationViews(
      context = context,
      collapsed = text("collapsed"),
      expanded = text("expanded"),
      headsUp = text("heads-up"),
    )

    assertEquals("collapsed", views.collapsed.apply(context, FrameLayout(context)).findText("collapsed")?.text)
    assertEquals("expanded", views.expanded!!.apply(context, FrameLayout(context)).findText("expanded")?.text)
    assertEquals("heads-up", views.headsUp!!.apply(context, FrameLayout(context)).findText("heads-up")?.text)
  }

  @Test
  fun omittedSurfacesStayNull() = runBlocking {
    val views = peekEmittableNotificationViews(context = context, collapsed = text("collapsed"))

    assertNull(views.expanded)
    assertNull(views.headsUp)
  }

  @Test
  fun setPeekEmittableContentAttachesCustomViewsAndOngoingFlag() = runBlocking {
    val notification = NotificationCompat.Builder(context, "sync")
      .setSmallIcon(android.R.drawable.stat_sys_upload)
      .setPeekEmittableContent(
        context = context,
        collapsed = text("collapsed"),
        expanded = text("expanded"),
        ongoing = true,
      )
      .build()

    @Suppress("DEPRECATION")
    assertNotNull(notification.contentView)
    @Suppress("DEPRECATION")
    assertNotNull(notification.bigContentView)
    assertEquals(Notification.FLAG_ONGOING_EVENT, notification.flags and Notification.FLAG_ONGOING_EVENT)
  }

  @Test
  fun invalidTreesAreRejected() {
    val shared = EmittableText().also { it.text = "shared" }
    val root = EmittableColumn().also { column ->
      column.children += shared
      column.children += shared
    }

    assertThrows(IllegalArgumentException::class.java) {
      runBlocking { peekEmittableNotificationViews(context = context, collapsed = root) }
    }
  }

  private fun View.findText(value: String): TextView? {
    if (this is TextView && text?.toString() == value) return this
    if (this !is ViewGroup) return null
    repeat(childCount) { index -> getChildAt(index).findText(value)?.let { return it } }
    return null
  }
}
