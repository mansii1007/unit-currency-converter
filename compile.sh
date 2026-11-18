#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
  JAVAC="$JAVA_HOME/bin/javac"
  JAVA="$JAVA_HOME/bin/java"
elif command -v javac >/dev/null 2>&1; then
  JAVAC=javac
  JAVA=java
else
  echo "JDK not found. Please install Java 11 or later and retry."
  exit 1
fi

mkdir -p out
find src -name "*.java" > /tmp/unit-converter-sources.txt
"$JAVAC" -encoding UTF-8 -d out @/tmp/unit-converter-sources.txt
# Copy the bundled offline rates next to the classes so the app can find them
mkdir -p out
cp -f resources/currency_rates.txt out/currency_rates.txt
mkdir -p data
echo "Compiled successfully to out/"
echo
echo "Run the app with:"
echo "  $JAVA -cp out converter.ui.ConverterApp"
echo "Run tests with:"
echo "  $JAVA -cp out converter.ConversionLogicTest"
