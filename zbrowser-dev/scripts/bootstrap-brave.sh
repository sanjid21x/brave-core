#!/usr/bin/env bash
# Run on a supported Linux cloud workstation after installing Brave Android prerequisites.
set -euo pipefail
if [[ $# -lt 2 ]]; then echo "Usage: $0 WORKSPACE BRAVE_CORE_REF [YOUR_FORK_HTTPS_URL]" >&2; exit 2; fi
root=$(realpath -m "$1"); ref=$2; fork=${3:-https://github.com/brave/brave-core.git}
core="$root/src/brave"
[[ ! -e "$core" ]] || { echo "Refusing to overwrite $core" >&2; exit 1; }
mkdir -p "$root/src"
git clone "$fork" "$core"
cd "$core"
if [[ "$fork" != https://github.com/brave/brave-core.git ]]; then git remote add upstream https://github.com/brave/brave-core.git; git fetch upstream --tags; fi
git switch --detach "$ref"
git switch -c zbrowser/v0.1.0-torrent
printf 'Pinned Brave Core commit: '; git rev-parse HEAD
pnpm run init --target_os=android --target_arch=arm64
cat <<'MSG'
Initialization completed. Build the unmodified baseline FIRST:
  pnpm run build Debug --target_os=android --target_arch=arm64 --target_android_output_format=apk
Then follow docs/BRAVE_INTEGRATION.md from the ZIP. This script does not patch or brand Brave.
MSG
