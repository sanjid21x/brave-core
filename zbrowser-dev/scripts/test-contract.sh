#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT=$(mktemp -d); trap 'rm -rf "$OUT"' EXIT
SRC="$ROOT/android-lab/app/src/main/java/com/sanjid/browser/torrent"
javac -d "$OUT" "$SRC/Transfer.java" "$SRC/TransferIndex.java" "$SRC/InputRules.java" "$ROOT/tests/ContractTest.java"
java -cp "$OUT" com.sanjid.browser.torrent.ContractTest
