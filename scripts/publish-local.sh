#!/usr/bin/env bash
# Builds the pinned Glance fork and publishes every Peek artifact locally.
#
# Usage:
#   ./scripts/publish-local.sh

set -euo pipefail

if [[ $# -ne 0 ]]; then
  echo "Usage: $0" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PEEK_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ANDROIDX_SUPPORT_ROOT="$PEEK_ROOT/glance-fork/androidx"
MAVEN_LOCAL_REPOSITORY="${PEEK_MAVEN_LOCAL_REPOSITORY:-$HOME/.m2/repository}"
ANDROIDX_JDK21="${ANDROIDX_JDK21:-${JAVA_HOME:-}}"

if [[ ! -x "$ANDROIDX_JDK21/bin/java" ]]; then
  echo "Set JAVA_HOME or ANDROIDX_JDK21 to a JDK 21 installation." >&2
  exit 1
fi

if ! git -C "$ANDROIDX_SUPPORT_ROOT" rev-parse --git-dir >/dev/null 2>&1; then
  echo "Initializing the pinned AndroidX submodule..."
  git -C "$PEEK_ROOT" submodule update --init --depth=1 -- glance-fork/androidx
fi

if [[ ! -x "$ANDROIDX_SUPPORT_ROOT/gradlew" ]]; then
  echo "AndroidX submodule is incomplete at: $ANDROIDX_SUPPORT_ROOT" >&2
  echo "Run: git submodule update --init --depth=1 glance-fork/androidx" >&2
  exit 1
fi

echo ""
echo "Publishing the pinned Glance AppWidget fork..."
echo ""

(
  cd "$ANDROIDX_SUPPORT_ROOT"
  env -u SNAPSHOT \
    ANDROIDX_JDK21="$ANDROIDX_JDK21" \
    ALLOW_MISSING_PROJECTS=1 \
    ALLOW_PUBLIC_REPOS=1 \
    PROJECT_PREFIX=:glance \
    ./gradlew \
    :glance:glance-appwidget:bundleReleaseAar \
    :glance:glance-appwidget:sourceJarRelease \
    :glance:glance-appwidget:generatePomFileForMavenPublication \
    -Pandroidx.validateProjectStructure=false
)

"$PEEK_ROOT/gradlew" \
  -p "$PEEK_ROOT/glance-fork/publisher" \
  publishToMavenLocal \
  -Dmaven.repo.local="$MAVEN_LOCAL_REPOSITORY" \
  --no-configuration-cache

echo ""
echo "Publishing the Peek Glance resolver plugin..."
echo ""

"$PEEK_ROOT/gradlew" \
  -p "$PEEK_ROOT/peek-glance-gradle-plugin" \
  publishToMavenLocal \
  -Dmaven.repo.local="$MAVEN_LOCAL_REPOSITORY"

echo ""
echo "Publishing Peek libraries to local Maven..."
echo ""

"$PEEK_ROOT/gradlew" \
  -p "$PEEK_ROOT" \
  :peek-glance:publishToMavenLocal \
  :peek-notification:publishToMavenLocal \
  :peek-emittables:publishToMavenLocal \
  -Dmaven.repo.local="$MAVEN_LOCAL_REPOSITORY" \
  --no-configuration-cache

echo ""
echo "Done. Peek artifacts and the Glance fork were published to $MAVEN_LOCAL_REPOSITORY."
