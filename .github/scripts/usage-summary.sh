#!/usr/bin/env bash
# Usage: usage-summary.sh <dir> [table|total]
# Reads <dir>/*/usage.json (one per stage) and prints a markdown table, or just the grand total.
DIR="$1"; MODE="${2:-table}"
FILES=$(ls "$DIR"/*/usage.json 2>/dev/null)

if [ -z "$FILES" ]; then
  [ "$MODE" = "total" ] && echo 0 || echo "_No usage data._"
  exit 0
fi

if [ "$MODE" = "total" ]; then
  jq -s 'map(.total) | add // 0' $FILES
else
  jq -rs '
    "| Stage | Input | Output | Cache | Total |",
    "|---|---|---|---|---|",
    (.[] | "| \(.stage) | \(.input) | \(.output) | \(.cache_read + .cache_creation) | \(.total) |"),
    "| **Total** | \(map(.input)|add) | \(map(.output)|add) | \(map(.cache_read + .cache_creation)|add) | **\(map(.total)|add)** |"
  ' $FILES
fi
