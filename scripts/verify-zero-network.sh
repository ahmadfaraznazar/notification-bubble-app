#!/usr/bin/env bash
set -e

MANIFEST_FILE="app/src/main/AndroidManifest.xml"

if [ -f "$MANIFEST_FILE" ]; then
    if grep -E -q "<uses-permission[^>]*android.permission.(INTERNET|ACCESS_NETWORK_STATE)" "$MANIFEST_FILE" || grep -q "android.permission.INTERNET" "$MANIFEST_FILE"; then
        echo "❌ CRITICAL SECURITY VIOLATION: Unauthorized network permissions found in $MANIFEST_FILE!"
        exit 1
    else
        echo "✅ Verified: Zero-network policy intact. No INTERNET or ACCESS_NETWORK_STATE permissions in manifest."
    fi
else
    echo "⚠️ Warning: Manifest file ($MANIFEST_FILE) not found."
    exit 1
fi
