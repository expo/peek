@file:Suppress(
  "RestrictedApiAndroidX",
  "INVISIBLE_MEMBER",
  "INVISIBLE_REFERENCE",
)

package io.github.expo.peek.emittables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.glance.Emittable
import androidx.glance.EmittableButton
import androidx.glance.EmittableImage
import androidx.glance.EmittableWithChildren
import androidx.glance.GlanceComposable
import androidx.glance.GlanceNode
import androidx.glance.appwidget.EmittableAndroidRemoteViews
import androidx.glance.appwidget.EmittableCheckBox
import androidx.glance.appwidget.EmittableCircularProgressIndicator
import androidx.glance.appwidget.EmittableIgnoreResult
import androidx.glance.appwidget.EmittableLinearProgressIndicator
import androidx.glance.appwidget.EmittableRadioButton
import androidx.glance.appwidget.EmittableSizeBox
import androidx.glance.appwidget.EmittableSwitch
import androidx.glance.appwidget.RemoteViewsRoot
import androidx.glance.appwidget.lazy.EmittableLazyColumn
import androidx.glance.appwidget.lazy.EmittableLazyListItem
import androidx.glance.appwidget.lazy.EmittableLazyVerticalGrid
import androidx.glance.appwidget.lazy.EmittableLazyVerticalGridListItem
import androidx.glance.layout.EmittableBox
import androidx.glance.layout.EmittableColumn
import androidx.glance.layout.EmittableRow
import androidx.glance.layout.EmittableSpacer
import androidx.glance.text.EmittableText
import io.github.expo.peek.glance.PeekGlanceEmittable
import java.util.IdentityHashMap

/**
 * Inserts an already-built Glance emittable subtree into the current official Glance composition.
 *
 * This is the low-level bridge used by tree producers such as Expo Widgets. The supplied tree is
 * copied before insertion, so Glance remains the sole owner of the tree that it normalizes and
 * translates.
 */
@Composable
@GlanceComposable
public fun EmittableTree(root: Emittable) {
  validateEmittableTree(root)
  key(root) {
    GlanceNode(
      factory = { root.copy() },
      update = {},
    )
  }
}

/** Validates the ownership and supported-node invariants required by [EmittableTree]. */
public fun validateEmittableTree(root: Emittable) {
  val visited = IdentityHashMap<Emittable, String>()

  fun visit(node: Emittable, path: String) {
    val previousPath = visited.put(node, path)
    require(previousPath == null) {
      "A Glance emittable is shared or cyclic: $path already appeared at $previousPath"
    }

    require(node.isSupportedEmittable()) {
      "Unsupported emittable ${node.javaClass.name} at $path. " +
        "Custom nodes must implement PeekGlanceEmittable."
    }

    if (node is EmittableWithChildren) {
      node.children.forEachIndexed { index, child -> visit(child, "$path.children[$index]") }
    }
  }

  visit(root, "root")
}

// Check types rather than class names, which R8 can change in release builds.
private fun Emittable.isSupportedEmittable(): Boolean = when (this) {
  is EmittableBox,
  is EmittableColumn,
  is EmittableRow,
  is EmittableSpacer,
  is EmittableText,
  is EmittableButton,
  is EmittableImage,
  is EmittableCheckBox,
  is EmittableRadioButton,
  is EmittableSwitch,
  is EmittableCircularProgressIndicator,
  is EmittableLinearProgressIndicator,
  is EmittableLazyColumn,
  is EmittableLazyListItem,
  is EmittableLazyVerticalGrid,
  is EmittableLazyVerticalGridListItem,
  is EmittableAndroidRemoteViews,
  is EmittableSizeBox,
  is EmittableIgnoreResult,
  is RemoteViewsRoot,
  is PeekGlanceEmittable -> true
  else -> false
}
