#!/bin/sh
# minimal wrapper
set -e
GRADLE_URL="https://services.gradle.org/distributions/gradle-8.7-bin.zip"
DIR="$HOME/.gradle/wrapper/dists/gradle-8.7-bin"
if [ ! -f "$DIR/gradle-8.7/bin/gradle" ]; then mkdir -p "$DIR"; curl -sL "$GRADLE_URL" -o /tmp/gradle.zip; unzip -q /tmp/gradle.zip -d "$DIR/.."; fi
exec "$DIR/gradle-8.7/bin/gradle" "$@"
