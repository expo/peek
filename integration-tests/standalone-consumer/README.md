# Standalone Peek consumer

This fixture is an ordinary Android library with no Expo dependency or Expo Gradle integration. It
resolves the published Peek plugin and libraries exclusively through Maven coordinates.

From the Peek repository root, publish the local fork, plugin, and libraries, then compile the
fixture:

```shell
./scripts/publish-local.sh
./gradlew -p integration-tests/standalone-consumer assembleDebug verifyStandaloneResolution
```

`verifyStandaloneResolution` asserts that the published resolver replaces official
`androidx.glance:glance-appwidget` even when it enters transitively through Glance multiprocess.
The compiled notification uses both a standard Glance component and Peek's custom circular progress
component.
