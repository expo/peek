# Peek

Peek supplies the Glance pieces AndroidX does not yet provide: notification composition and
extensible custom components translated to RemoteViews.

## Modules

- `peek-glance` provides custom Glance components.
- `peek-notification` composes Glance content into `NotificationCompat` custom views.
- `peek-emittables` inserts prebuilt Glance emittable trees, provides a multiprocess AppWidget
  adapter for low-level tree producers, and composes such trees into notification views.

## Glance fork resolver

Peek is pinned to Glance `1.2.0`. The plugin substitutes only
`glance-appwidget` with `io.github.expo.peek.forks:glance-appwidget`, including transitive requests
from multiprocess and testing artifacts, and fails the build if another Glance version is present.

## License

Peek is released under the [MIT License](LICENSE).
