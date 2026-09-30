package io.github.expo.peek.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.glance.text.Text
import androidx.test.core.app.ApplicationProvider
import io.github.expo.peek.glance.CircularProgressIndicator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class PeekNotificationBuilderTest {
  private val context = ApplicationProvider.getApplicationContext<Context>()

  @Test
  fun allSurfacesUseOneComposerSequentiallyWithExpectedInputs() = runBlocking {
    val state = Any()
    val options = Bundle().apply { putString("source", "test") }
    val sizes = PeekNotificationSizes(
      collapsed = DpSize(101.dp, 41.dp),
      expanded = DpSize(202.dp, 142.dp),
      headsUp = DpSize(303.dp, 83.dp),
    )
    val composer = RecordingComposer(context)

    val views = peekNotificationViews(
      context = context,
      collapsed = { Text("collapsed") },
      expanded = { Text("expanded") },
      headsUp = { Text("heads-up") },
      state = state,
      appWidgetOptions = options,
      sizes = sizes,
      composer = composer,
    )

    assertEquals(listOf(sizes.collapsed, sizes.expanded, sizes.headsUp), composer.calls.map { it.size })
    composer.calls.forEach {
      assertSame(state, it.state)
      assertSame(options, it.options)
    }
    assertSame(composer.results[0], views.collapsed)
    assertSame(composer.results[1], views.expanded)
    assertSame(composer.results[2], views.headsUp)
  }

  @Test
  fun compositionFailureDoesNotPartiallyMutateBuilder() {
    val builder = NotificationCompat.Builder(context, "sync")
      .setSmallIcon(android.R.drawable.stat_sys_upload)
    val composer = RecordingComposer(context, failAtCall = 2)

    assertThrows(IllegalStateException::class.java) {
      runBlocking {
        builder.setPeekContent(
          context = context,
          collapsed = { Text("collapsed") },
          expanded = { Text("expanded") },
          headsUp = { Text("heads-up") },
          composer = composer,
        )
      }
    }

    val notification = builder.build()
    assertNotEquals(android.R.layout.simple_list_item_1, notification.contentView.layoutId)
  }

  @Test
  fun publicComposerRendersPeekExtendedViewThroughGlanceRemoteViews() = runBlocking {
    val views = peekNotificationViews(
      context = context,
      collapsed = { CircularProgressIndicator(progress = 0.42f) },
    )

    val applied = views.collapsed.apply(context, FrameLayout(context))
    val progress = applied.findProgressBar()

    assertNotNull(progress)
    assertEquals(42, progress?.progress)
  }

  @Test
  fun setPeekContentAttachesAllCustomViewsAndOngoingFlag() = runBlocking {
    val notification = NotificationCompat.Builder(context, "sync")
      .setSmallIcon(android.R.drawable.stat_sys_upload)
      .setPeekContent(
        context = context,
        collapsed = { Text("Uploading") },
        expanded = { Text("Uploading details") },
        headsUp = { Text("Upload running") },
      )
      .build()

    assertNotNull(notification.contentView)
    assertNotNull(notification.bigContentView)
    assertNotNull(notification.headsUpContentView)
    assertEquals(Notification.FLAG_ONGOING_EVENT, notification.flags and Notification.FLAG_ONGOING_EVENT)
  }

  @Test
  fun addPeekActionAttachesDirectReplyRemoteInput() {
    val notification = NotificationCompat.Builder(context, "sync")
      .setSmallIcon(android.R.drawable.stat_sys_upload)
      .addPeekAction(
        PeekNotificationAction(
          iconResId = android.R.drawable.ic_dialog_email,
          title = "Reply",
          pendingIntent = pendingBroadcast("reply"),
          remoteInputs = listOf(
            PeekNotificationRemoteInput(
              resultKey = "peek_reply",
              label = "Status update",
              choices = listOf("Done", "Blocked"),
            )
          ),
          semanticAction = NotificationCompat.Action.SEMANTIC_ACTION_REPLY,
        )
      )
      .build()

    val action = notification.actions.single()
    val remoteInput = action.remoteInputs.single()
    assertEquals("Reply", action.title.toString())
    assertEquals(Notification.Action.SEMANTIC_ACTION_REPLY, action.semanticAction)
    assertEquals("peek_reply", remoteInput.resultKey)
    assertEquals(listOf("Done", "Blocked"), remoteInput.choices.map { it.toString() })
  }

  @Test
  fun peekRemoteInputReadsDirectReplyResults() {
    val input = PeekNotificationRemoteInput("peek_reply", "Status update")
    val intent = Intent("reply")
    val results = Bundle().apply { putCharSequence("peek_reply", "Done") }

    PeekNotificationRemoteInput.addResultsToIntent(listOf(input), intent, results)

    assertEquals(
      "Done",
      PeekNotificationRemoteInput.getResultsFromIntent(intent)?.getCharSequence("peek_reply").toString(),
    )
  }

  private fun pendingBroadcast(action: String): PendingIntent = PendingIntent.getBroadcast(
    context,
    action.hashCode(),
    Intent(action).setPackage(context.packageName),
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
  )

  private fun android.view.View.findProgressBar(): ProgressBar? {
    if (this is ProgressBar) return this
    if (this !is android.view.ViewGroup) return null
    repeat(childCount) { index -> getChildAt(index).findProgressBar()?.let { return it } }
    return null
  }

  private class RecordingComposer(
    private val context: Context,
    private val failAtCall: Int? = null,
  ) : NotificationComposer {
    val calls = mutableListOf<Call>()
    val results = mutableListOf<RemoteViews>()

    override suspend fun compose(
      context: Context,
      size: DpSize,
      state: Any?,
      options: Bundle,
      content: @Composable () -> Unit,
    ): RemoteViews {
      calls += Call(size, state, options)
      if (calls.size == failAtCall) error("composition failed")
      return RemoteViews(this.context.packageName, android.R.layout.simple_list_item_1)
        .also(results::add)
    }
  }

  private data class Call(val size: DpSize, val state: Any?, val options: Bundle)
}
