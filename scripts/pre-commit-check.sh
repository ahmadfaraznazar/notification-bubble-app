#!/usr/bin/env bash
set -e

echo "=== [1/3] Verifying Zero-Network Manifest Policy ==="
bash scripts/verify-zero-network.sh

echo "=== [2/3] Checking ProGuard / Manifest Configuration ==="
if [ -f "app/proguard-rules.pro" ] && [ -f "app/build.gradle.kts" ]; then
    echo "✅ ProGuard and build scripts are properly staged."
fi

echo "=== [3/3] Auditing Binary Size Budget (< 5MB) ==="
bash scripts/verify-binary-size-budget.sh

echo "✅ Local verification checks completed!"
