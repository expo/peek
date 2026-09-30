package io.github.expo.peek.integration.standalone

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.glance.layout.Row
import androidx.glance.text.Text
import io.github.expo.peek.glance.CircularProgressIndicator
import io.github.expo.peek.notification.setPeekContent

suspend fun createStandalonePeekNotification(
    context: Context,
    builder: NotificationCompat.Builder,
): NotificationCompat.Builder = builder.setPeekContent(
    context = context,
    collapsed = {
        Row {
            Text("Standalone Peek")
            CircularProgressIndicator(progress = 0.6f)
        }
    },
)
