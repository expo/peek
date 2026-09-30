package io.github.expo.peek.gradle

public object PeekGlanceForkCoordinates {
  public const val peekVersion: String = "0.3.1"
  public const val supportedGlanceVersion: String = "1.2.0"
  public const val forkVersion: String = "$supportedGlanceVersion-peek-$peekVersion"

  public const val officialGroup: String = "androidx.glance"
  public const val appWidgetModule: String = "glance-appwidget"
  public const val officialAppWidgetModule: String =
    "$officialGroup:$appWidgetModule"

  public const val forkGroup: String = "io.github.expo.peek.forks"
  public const val forkModule: String = "glance-appwidget"
  public const val forkGroupAndModule: String = "$forkGroup:$forkModule"
  public const val forkCoordinate: String = "$forkGroupAndModule:$forkVersion"
}
