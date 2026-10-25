#!/usr/bin/env bash
# Builds this repository's iOS app ("Sessionize Schedule" in App Store Connect, bundle id com.muschenetz.sessionize)
# and uploads it to TestFlight. The AGNTCon app (com.muschenetz.agntcon) is published from ingo/agntcon-app instead.
#
#   scripts/testflight.sh            archive only
#   scripts/testflight.sh --upload   archive and upload to TestFlight
#
# Needs ASC_KEY_ID and ASC_ISSUER_ID for the upload (App Store Connect API key, see ios-testflight.sh).
set -euo pipefail
exec "$(dirname "$0")/ios-testflight.sh" agntcon-mcpcon-na-2026 com.muschenetz.sessionize Sessionize 4M7MX9FB66 "$@"
