# Peek Glance AppWidget fork

Peek uses the released Glance `1.2.0` dependency graph and replaces only
`androidx.glance:glance-appwidget` with this artifact:

```text
io.github.expo.peek.forks:glance-appwidget
```

The fork adds two internal translation hooks:

- custom emittable to `RemoteViews`
- custom `GlanceModifier` to `RemoteViews`

Everything else remains the official AndroidX implementation, including state,
previews, testing, multiprocess widgets, and the public
`GlanceRemoteViews.compose()` notification entry point.

## Source and rebuilding

Peek's AndroidX fork is included at `glance-fork/androidx` as a shallow Git submodule. Its gitlink
pins the exact fork commit, which contains the translation hooks and standalone build configuration
directly. Clone Peek with submodules, or initialize it after cloning:

```bash
git submodule update --init --depth=1
```

Then run:

```bash
./scripts/publish-local.sh
```

The script initializes the submodule when needed. The fork uses an installed Android SDK and JDK
21 from `JAVA_HOME` or `ANDROIDX_JDK21`. It downloads the pinned Gradle distribution and build
dependencies from public repositories, so no AOSP `repo` checkout or prebuilts directory is
required. The script publishes the fork, resolver plugin, and Peek libraries to Maven local. Set
`PEEK_MAVEN_LOCAL_REPOSITORY` to override the default `$HOME/.m2/repository` destination.

Only the `:glance` project prefix and its dependency graph are configured during publication;
unrelated external AOSP projects are allowed to be absent from the standalone checkout.

## Consumer resolution

Consumers apply the project plugin and keep declaring official Glance modules.

The plugin replaces every direct or transitive AppWidget request with the fork.
It rejects any other `androidx.glance` version so the fork cannot be combined
with a binary-incompatible Glance release. Expo Widgets registers a small,
self-contained autolinked project plugin with the same resolution rule, so Expo
applications need no app-level Gradle edits.
