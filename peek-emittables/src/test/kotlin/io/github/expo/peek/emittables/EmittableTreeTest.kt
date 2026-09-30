@file:Suppress("RestrictedApiAndroidX")

package io.github.expo.peek.emittables

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Emittable
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.glance.appwidget.multiprocess.MultiProcessGlanceAppWidget
import androidx.glance.layout.EmittableColumn
import androidx.glance.text.EmittableText
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class EmittableTreeTest {
  private val context = ApplicationProvider.getApplicationContext<Context>()

  @OptIn(ExperimentalGlanceRemoteViewsApi::class)
  @Test
  fun directOfficialEmittableTreeComposesWithoutReconstructingComposableNodes() = runBlocking {
    val root = EmittableColumn().also { column ->
      column.children += EmittableText().also { it.text = "direct tree" }
    }

    val remoteViews = GlanceRemoteViews().compose(context, DpSize(120.dp, 80.dp)) {
      EmittableTree(root)
    }.remoteViews
    val applied = remoteViews.apply(context, FrameLayout(context))

    assertEquals("direct tree", applied.findText("direct tree")?.text)
  }

  @Test
  fun sharedNodeIdentityIsRejectedBeforeComposition() {
    val shared = EmittableText().also { it.text = "shared" }
    val root = EmittableColumn().also { column ->
      column.children += shared
      column.children += shared
    }

    val error = assertThrows(IllegalArgumentException::class.java) {
      validateEmittableTree(root)
    }

    assertTrue(error.message.orEmpty().contains("shared", ignoreCase = true))
  }

  @Test
  fun appWidgetBridgeUsesOfficialMultiprocessGlanceLifecycle() {
    val widget: MultiProcessGlanceAppWidget = object : PeekEmittableAppWidget() {
      override fun provideRoot(
        context: Context,
        id: AppWidgetId,
        options: android.os.Bundle,
        size: DpSize,
      ): Emittable = EmittableText().also { it.text = id.appWidgetId.toString() }
    }

    assertTrue(widget is PeekEmittableAppWidget)
  }

  private fun View.findText(text: String): TextView? {
    if (this is TextView && this.text.toString() == text) return this
    if (this !is ViewGroup) return null
    repeat(childCount) { index ->
      getChildAt(index).findText(text)?.let { return it }
    }
    return null
  }
}
