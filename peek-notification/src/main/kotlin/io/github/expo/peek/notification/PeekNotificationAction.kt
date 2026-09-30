package io.github.expo.peek.notification

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationCompat.Action.SemanticAction
import androidx.core.app.RemoteInput

data class PeekNotificationAction(
  @param:DrawableRes
  val iconResId: Int,
  val title: CharSequence,
  val pendingIntent: PendingIntent,
  val remoteInputs: List<PeekNotificationRemoteInput> = emptyList(),
  val allowGeneratedReplies: Boolean = true,
  @param:SemanticAction
  val semanticAction: Int = Action.SEMANTIC_ACTION_NONE,
  val showsUserInterface: Boolean = true,
  val authenticationRequired: Boolean = false,
  val extras: Bundle = Bundle(),
)

data class PeekNotificationRemoteInput(
  val resultKey: String,
  val label: CharSequence,
  val choices: List<CharSequence> = emptyList(),
  val allowFreeFormInput: Boolean = true,
  val allowedDataTypes: Set<String> = emptySet(),
  val extras: Bundle = Bundle(),
) {
  companion object {
    fun getResultsFromIntent(intent: Intent): Bundle? =
      RemoteInput.getResultsFromIntent(intent)

    fun addResultsToIntent(
      remoteInputs: List<PeekNotificationRemoteInput>,
      intent: Intent,
      results: Bundle,
    ) {
      RemoteInput.addResultsToIntent(
        remoteInputs.map { it.toCompatRemoteInput() }.toTypedArray(),
        intent,
        results,
      )
    }
  }
}

fun NotificationCompat.Builder.addPeekAction(action: PeekNotificationAction): NotificationCompat.Builder =
  addAction(action.toCompatAction())

fun NotificationCompat.Builder.addPeekActions(
  actions: Iterable<PeekNotificationAction>,
): NotificationCompat.Builder =
  apply {
    actions.forEach { addPeekAction(it) }
  }

internal fun PeekNotificationAction.toCompatAction(): Action {
  val builder = Action.Builder(
    iconResId,
    title,
    pendingIntent,
  )

  return with(builder) {
    remoteInputs.forEach { addRemoteInput(it.toCompatRemoteInput()) }
    setAllowGeneratedReplies(allowGeneratedReplies)
    setSemanticAction(semanticAction)
    setShowsUserInterface(showsUserInterface)
    setAuthenticationRequired(authenticationRequired)
    addExtras(extras)
    build()
  }
}

internal fun PeekNotificationRemoteInput.toCompatRemoteInput(): RemoteInput {
  val builder = RemoteInput.Builder(resultKey)
    .setLabel(label)
    .setAllowFreeFormInput(allowFreeFormInput)
    .addExtras(extras)
  if (choices.isNotEmpty()) {
    builder.setChoices(choices.toTypedArray())
  }
  allowedDataTypes.forEach { mimeType ->
    builder.setAllowDataType(mimeType, true)
  }
  return builder.build()
}
