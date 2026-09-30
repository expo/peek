package io.github.expo.peek.sample

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class SampleNotificationActionReceiver : BroadcastReceiver() {
  @SuppressLint("MissingPermission")
  override fun onReceive(
    context: Context,
    intent: Intent,
  ) {
    if (intent.action != SampleNotifications.ActionTogglePause) return

    val progress = intent.getFloatExtra(SampleNotifications.ExtraProgress, 0f)
    val paused = !intent.getBooleanExtra(SampleNotifications.ExtraPaused, false)
    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.Default).launch {
      try {
        SampleNotifications.postOngoingUpdate(
          context = context,
          progress = progress,
          paused = paused,
        )
      } finally {
        pendingResult.finish()
      }
    }
  }
}
