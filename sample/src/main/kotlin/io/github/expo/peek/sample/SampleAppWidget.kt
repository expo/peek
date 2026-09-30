package io.github.expo.peek.sample

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.multiprocess.MultiProcessGlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.expo.peek.glance.CircularProgressIndicator

class SampleAppWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = SampleAppWidget
}

internal object SampleAppWidget : MultiProcessGlanceAppWidget() {
  private val StatusKey = stringPreferencesKey("status")

  override val stateDefinition = PreferencesGlanceStateDefinition

  override val sizeMode: SizeMode = SizeMode.Responsive(
    setOf(
      DpSize(150.dp, 96.dp),
      DpSize(260.dp, 120.dp),
    )
  )

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent { WidgetContent(context) }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    require(widgetCategory and AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN != 0)
    provideContent { WidgetContent(context, previewStatus = "Official Glance preview") }
  }

  @Composable
  private fun WidgetContent(context: Context, previewStatus: String? = null) {
    val size = LocalSize.current
    val wide = size.width >= 220.dp
    val status = previewStatus ?: currentState(StatusKey) ?: "Official Preferences state"

    GlanceTheme {
      Column(
        modifier = GlanceModifier
          .fillMaxSize()
          .background(ColorProvider(Color(0xfff4f2fa)))
          .padding(12.dp),
      ) {
        Text("Peek + Glance", style = TextStyle(fontWeight = FontWeight.Bold), maxLines = 1)
        Spacer(GlanceModifier.height(4.dp))
        Text(status, maxLines = 1)
        Spacer(GlanceModifier.height(6.dp))
        Row(
          modifier = GlanceModifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(
            text = "Open",
            onClick = actionStartActivity(Intent(context, MainActivity::class.java)),
          )
          if (wide) {
            Spacer(GlanceModifier.width(8.dp))
            Text("${size.width.value.toInt()} × ${size.height.value.toInt()} dp", maxLines = 1)
          }
        }
        Spacer(GlanceModifier.height(6.dp))
        LinearProgressIndicator(progress = .3f, modifier = GlanceModifier.fillMaxWidth())
        CircularProgressIndicator(
          progress = .5f,
          color = ColorProvider(Color(0xff6750a4)),
          trackColor = ColorProvider(Color(0x336750a4)),
        )
      }
    }
  }
}
