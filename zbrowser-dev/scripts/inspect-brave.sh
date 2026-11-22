#!/usr/bin/env bash
# Read-only source discovery for the selected upstream revision.
set -euo pipefail
root=$(realpath "$1"); cd "$root/src"
[[ -d brave && -d chrome ]] || { echo 'Initialize Brave Core first'; exit 1; }
printf '\n=== Android download UI and provider ===\n'
find chrome brave -iname '*DownloadManagerCoordinator*' -o -iname '*OfflineContentProvider*' -o -iname '*DownloadHome*'
printf '\n=== External protocol handling and application ID candidates ===\n'
git grep -n -E 'class ExternalNavigationHandler|class ExternalNavigationDelegate|chrome_public_apk|chrome_public_manifest_package' -- chrome/android chrome/browser 2>/dev/null | head -100 || true
printf '\n=== Brave download overrides ===\n'
git -C brave ls-files | grep -Ei '(download|android.*branding|android.*manifest)' | head -120 || true
printf '\n=== AAR GN integration examples ===\n'
git grep -n 'android_aar_prebuilt(' -- third_party build 2>/dev/null | head -25 || true
