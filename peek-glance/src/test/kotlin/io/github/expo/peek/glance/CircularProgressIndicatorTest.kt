@file:Suppress(
  "INVISIBLE_MEMBER",
  "INVISIBLE_REFERENCE",
  "RestrictedApiAndroidX",
)

package io.github.expo.peek.glance

import android.content.Context
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.unit.ColorProvider
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class CircularProgressIndicatorTest {
  private val context = ApplicationProvider.getApplicationContext<Context>()

  @Test
  fun progressIsClampedAndPreservedByCopy() {
    val node = determinateCircularProgressIndicatorEmittable(
      progress = 1.5f,
      modifier = GlanceModifier,
      color = ColorProvider(Color.Red),
      trackColor = ColorProvider(Color.Blue),
    ) as EmittableDeterminateCircularProgressIndicator

    val copy = node.copy() as EmittableDeterminateCircularProgressIndicator

    assertEquals(1f, node.progress)
    assertEquals(node.progress, copy.progress)
    assertEquals(node.color, copy.color)
    assertEquals(node.trackColor, copy.trackColor)
    assertNotSame(node, copy)
  }

  @Test
  fun nonFiniteProgressIsRejected() {
    listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { progress ->
      assertThrows(IllegalArgumentException::class.java) {
        determinateCircularProgressIndicatorEmittable(progress)
      }
    }
  }

  @Test
  fun remoteViewsUsesDeterminatePlatformProgressBar() {
    val node = determinateCircularProgressIndicatorEmittable(0.376f)
      as EmittableDeterminateCircularProgressIndicator

    val view = node.createRemoteViews(context).apply(context, FrameLayout(context)) as ProgressBar

    assertEquals(false, view.isIndeterminate)
    assertEquals(100, view.max)
    assertEquals(38, view.progress)
  }
}
