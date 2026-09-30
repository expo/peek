@file:Suppress("RestrictedApiAndroidX")

package io.github.expo.peek.emittables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.glance.Emittable
import androidx.glance.EmittableWithChildren
import androidx.glance.GlanceComposable
import androidx.glance.GlanceNode
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

    require(node.javaClass.name.startsWith("androidx.glance.") || node is PeekGlanceEmittable) {
      "Unsupported emittable ${node.javaClass.name} at $path. " +
        "Custom nodes must implement PeekGlanceEmittable."
    }

    if (node is EmittableWithChildren) {
      node.children.forEachIndexed { index, child -> visit(child, "$path.children[$index]") }
    }
  }

  visit(root, "root")
}
