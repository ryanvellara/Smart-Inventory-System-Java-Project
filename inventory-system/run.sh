#!/bin/bash
# ─────────────────────────────────────────────
#  Smart Inventory System — Build & Run Script
# ─────────────────────────────────────────────

SRC="src/main/java"
OUT="out"
MAIN="com.inventory.ui.MainFrame"

echo "🔨 Compiling..."
mkdir -p "$OUT"

find "$SRC" -name "*.java" > sources.txt
javac -d "$OUT" @sources.txt

if [ $? -ne 0 ]; then
  echo "❌ Compilation failed."
  exit 1
fi

echo "✅ Build successful."
echo "🚀 Launching Inventory System..."
java -cp "$OUT" "$MAIN"
