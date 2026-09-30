package io.github.expo.peek.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : android.app.Activity() {
  private lateinit var notificationActionButton: Button
  private val scope = MainScope()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    SampleNotifications.createNotificationChannel(this)

    val layout =
      LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(48, 48, 48, 48)
      }

    layout.addView(
      TextView(this).apply {
        text = "Peek sample"
        textSize = 22f
        gravity = Gravity.CENTER
      },
    )
    notificationActionButton =
      Button(this).apply {
        setOnClickListener {
          if (ensureNotificationPermission()) {
            postOrUpdateNotification()
          }
        }
      }
    refreshNotificationActionButton()
    layout.addView(notificationActionButton)
    layout.addView(
      Button(this).apply {
        text = "Show match notification"
        setOnClickListener {
          if (ensureNotificationPermission()) {
            scope.launch { SampleNotifications.postMatchUpdate(this@MainActivity) }
          }
        }
      },
    )
    layout.addView(
      Button(this).apply {
        text = "Cancel notification"
        setOnClickListener {
          SampleNotifications.cancel(this@MainActivity)
          refreshNotificationActionButton()
        }
      },
    )

    setContentView(layout)
  }

  override fun onResume() {
    super.onResume()
    if (::notificationActionButton.isInitialized) {
      refreshNotificationActionButton()
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }

  private fun ensureNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    if (
      ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
      PackageManager.PERMISSION_GRANTED
    ) {
      return true
    }
    ActivityCompat.requestPermissions(
      this,
      arrayOf(Manifest.permission.POST_NOTIFICATIONS),
      NotificationPermissionRequest,
    )
    return false
  }

  private fun postOrUpdateNotification() {
    val progress =
      if (SampleNotifications.isOngoingNotificationActive(this)) {
        UpdatedProgress
      } else {
        InitialProgress
      }
    scope.launch {
      SampleNotifications.postOngoingUpdate(this@MainActivity, progress)
      refreshNotificationActionButton()
    }
  }

  private fun refreshNotificationActionButton() {
    notificationActionButton.text =
      if (SampleNotifications.isOngoingNotificationActive(this)) {
        "Update notification"
      } else {
        "Show ongoing notification"
      }
  }

  private companion object {
    const val NotificationPermissionRequest = 10
    const val InitialProgress = 0.64f
    const val UpdatedProgress = 0.82f
  }
}
