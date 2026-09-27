#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p build/classes lib
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}"
if [ -z "$java_bin" ] && [ -x /opt/homebrew/opt/openjdk/bin/javac ]; then
  java_bin=/opt/homebrew/opt/openjdk/bin/
fi
jar="lib/mysql-connector-j-9.4.0.jar"
if [ ! -f "$jar" ]; then
  curl --fail --location --output "$jar" https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/9.4.0/mysql-connector-j-9.4.0.jar
fi
fuentes=()
while IFS= read -r -d '' archivo; do
  fuentes+=("$archivo")
done < <(find src tests -name '*.java' -print0)
"${java_bin}javac" --release 21 -encoding UTF-8 -d build/classes "${fuentes[@]}"
echo "Compilación correcta. Ver README.md para ejecutar."
