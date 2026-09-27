#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${PROPIERA_DB_USER:?Definir PROPIERA_DB_USER}"
: "${PROPIERA_DB_PASSWORD:?Definir PROPIERA_DB_PASSWORD}"
: "${PROPIERA_ADMIN_PASSWORD:?Definir PROPIERA_ADMIN_PASSWORD}"
: "${PROPIERA_MARTILLERO_PASSWORD:?Definir PROPIERA_MARTILLERO_PASSWORD}"
bash compilar.sh
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}"
if [ -z "$java_bin" ] && [ -x /opt/homebrew/opt/openjdk/bin/java ]; then
  java_bin=/opt/homebrew/opt/openjdk/bin/
fi
exec "${java_bin}java" -cp 'build/classes:lib/*' propiera.PropieraApp
