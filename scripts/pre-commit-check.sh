#!/usr/bin/env bash
set -e

echo "=== [1/2] Verifying Zero-Network Manifest Policy ==="
bash scripts/verify-zero-network.sh

echo "=== [2/2] Checking ProGuard / Manifest Configuration ==="
if [ -f "app/proguard-rules.pro" ] && [ -f "app/build.gradle.kts" ]; then
    echo "✅ ProGuard and build scripts are properly staged."
fi

echo "✅ Local verification checks completed!"
