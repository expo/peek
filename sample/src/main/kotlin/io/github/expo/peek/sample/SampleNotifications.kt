package io.github.expo.peek.sample

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.expo.peek.glance.CircularProgressIndicator
import io.github.expo.peek.notification.setPeekContent
import kotlin.math.roundToInt

internal object SampleNotifications {
  const val ChannelId = "peek_sample"
  const val NotificationId = 1001
  const val MatchNotificationId = 1002
  const val ActionTogglePause = "io.github.expo.peek.sample.action.TOGGLE_PAUSE"
  const val ExtraPaused = "io.github.expo.peek.sample.extra.PAUSED"
  const val ExtraProgress = "io.github.expo.peek.sample.extra.PROGRESS"

  @SuppressLint("MissingPermission")
  suspend fun postOngoingUpdate(context: Context, progress: Float, paused: Boolean = false) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val progressPercent = (clampedProgress * 100).roundToInt()
    val statusText = if (paused) "Backup paused" else "Uploading backup"
    val toggleAction = actionSendBroadcast(
      Intent(context, SampleNotificationActionReceiver::class.java).apply {
        action = ActionTogglePause
        putExtra(ExtraProgress, clampedProgress)
        putExtra(ExtraPaused, paused)
      }
    )

    val notification = NotificationCompat.Builder(context, ChannelId)
      .setSmallIcon(R.drawable.ic_peek_notification)
      .setContentTitle("Peek sample sync")
      .setOnlyAlertOnce(true)
      .setPeekContent(
        context = context,
        collapsed = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(progress = clampedProgress)
            Spacer(GlanceModifier.width(8.dp))
            Text(statusText, maxLines = 1)
          }
        },
        expanded = {
          Column(
            modifier = GlanceModifier
              .fillMaxWidth()
              .background(ColorProvider(Color(0xfff8f7fc)))
              .padding(12.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Image(ImageProvider(android.R.drawable.stat_sys_upload), contentDescription = null)
              Spacer(GlanceModifier.width(8.dp))
              Text(statusText, maxLines = 1)
            }
            Spacer(GlanceModifier.height(8.dp))
            LinearProgressIndicator(clampedProgress, GlanceModifier.fillMaxWidth())
            Spacer(GlanceModifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("$progressPercent% complete", maxLines = 1)
              Spacer(GlanceModifier.width(8.dp))
              Button(if (paused) "Resume" else "Pause", onClick = toggleAction)
            }
          }
        },
        headsUp = {
          Text(
            if (paused) "Backup is paused" else "Backup is still running",
            modifier = GlanceModifier.padding(12.dp),
            maxLines = 1,
          )
        },
      )
      .build()

    NotificationManagerCompat.from(context).notify(NotificationId, notification)
  }

  @SuppressLint("MissingPermission")
  suspend fun postMatchUpdate(context: Context) {
    val notification = NotificationCompat.Builder(context, ChannelId)
      .setSmallIcon(R.drawable.ic_peek_notification)
      .setContentTitle("Poland 2 : 1 Albania")
      .setContentText("73' live")
      .setCategory(NotificationCompat.CATEGORY_EVENT)
      .setOnlyAlertOnce(true)
      .setPeekContent(
        context = context,
        collapsed = { MatchCollapsedContent() },
        expanded = { MatchScoreboardContent() },
        headsUp = { MatchScoreboardContent() },
      )
      .build()

    NotificationManagerCompat.from(context).notify(MatchNotificationId, notification)
  }

  fun cancel(context: Context) {
    NotificationManagerCompat.from(context).run {
      cancel(NotificationId)
      cancel(MatchNotificationId)
    }
  }

  fun isOngoingNotificationActive(context: Context): Boolean =
    context.getSystemService(NotificationManager::class.java).activeNotifications.any {
      it.id == NotificationId
    }

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(ChannelId, "Peek sample", NotificationManager.IMPORTANCE_LOW)
      .apply { description = "Notifications composed by official Glance through Peek." }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
  }

  @Composable
  private fun MatchCollapsedContent() {
    Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text("🇵🇱 2 : 1 🇦🇱", style = TextStyle(fontSize = 28.sp), maxLines = 1)
      Spacer(GlanceModifier.width(24.dp))
      Text("73'", style = TextStyle(color = MatchMinute), maxLines = 1)
    }
  }

  @Composable
  private fun MatchScoreboardContent() {
    Row(
      modifier = GlanceModifier.fillMaxWidth().padding(vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TeamColumn("Poland", "🇵🇱")
      Spacer(GlanceModifier.width(16.dp))
      Column(GlanceModifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          "2 : 1",
          style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
          maxLines = 1,
        )
        Text(
          "73'",
          style = TextStyle(color = MatchMinute, fontSize = 12.sp, textAlign = TextAlign.Center),
          maxLines = 1,
        )
      }
      Spacer(GlanceModifier.width(16.dp))
      TeamColumn("Albania", "🇦🇱")
    }
  }

  @Composable
  private fun TeamColumn(name: String, flag: String) {
    Column(GlanceModifier.width(70.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      Text(flag, style = TextStyle(fontSize = 28.sp, textAlign = TextAlign.Center), maxLines = 1)
      Spacer(GlanceModifier.height(4.dp))
      Text(name, style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center), maxLines = 1)
    }
  }

  private val MatchMinute = ColorProvider(Color(0xff777777))
}
