#!/usr/bin/env bash
# Regenerates the app icons of every platform from two SVGs, so branding an app means replacing them:
#
#   branding/icon.svg    the app icon: square, fully opaque (the stores round the corners)
#   branding/glyph.svg   the mark alone, white on transparent, inside the middle two thirds
#                        (Android's adaptive icon and the web splash screen are drawn from it)
#   branding/brand.env   the colors that go with them
#
#   scripts/make-icons.sh
#
# Writes the iOS asset catalog, the iOS Icon Composer icon, the Android launcher icons and Play Store
# icon, and the icons and splash screens of each web branding folder (app/webApp/branding/<name>).
# Needs rsvg-convert (brew install librsvg) and Python with Pillow (pip install pillow).
set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
BRANDING="$ROOT/branding"
command -v rsvg-convert >/dev/null || { echo "rsvg-convert is needed: brew install librsvg" >&2; exit 1; }
python3 -c "import PIL" 2>/dev/null || { echo "Pillow is needed: pip install pillow" >&2; exit 1; }
[[ -f "$BRANDING/icon.svg" && -f "$BRANDING/glyph.svg" ]] || { echo "Missing branding/icon.svg or branding/glyph.svg" >&2; exit 1; }
source "$BRANDING/brand.env"

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

# render <pixels> <svg> <png>
render() { rsvg-convert -w "$1" -h "$1" "$2" -o "$3"; }

# The glyph in another color: black for Android's themed icon, accent colors for the web splash screens
recolor() { sed "s/#ffffff/$2/g" "$1" > "$3"; }

# iOS asset catalog: every size the catalog asks for. Icons must not have an alpha channel.
IOS="$ROOT/app/iosApp/iosApp"
python3 - "$IOS/Assets.xcassets/AppIcon.appiconset" "$BRANDING/icon.svg" <<'PY'
import json, subprocess, sys, io
from PIL import Image
catalog, svg = sys.argv[1:3]
for image in json.load(open(f"{catalog}/Contents.json"))["images"]:
    name = image.get("filename")
    if not name:
        continue
    px = round(float(image["size"].split("x")[0]) * int(image["scale"].rstrip("x")))
    png = subprocess.run(["rsvg-convert", "-w", str(px), "-h", str(px), svg], check=True, capture_output=True).stdout
    Image.open(io.BytesIO(png)).convert("RGB").save(f"{catalog}/{name}")
PY

# iOS Icon Composer icon: the glyph on the brand gradient
if [[ -d "$IOS/AppIcon.icon" ]]; then
  rm -f "$IOS"/AppIcon.icon/Assets/*.svg
  cp "$BRANDING/glyph.svg" "$IOS/AppIcon.icon/Assets/Glyph.svg"
  cat > "$IOS/AppIcon.icon/icon.json" <<JSON
{
  "fill" : {
    "automatic-gradient" : "display-p3:0.14510,0.38824,0.92157,1.00000"
  },
  "groups" : [
    {
      "layers" : [
        {
          "image-name" : "Glyph.svg",
          "name" : "Glyph"
        }
      ],
      "shadow" : {
        "kind" : "neutral",
        "opacity" : 0.5
      },
      "translucency" : {
        "enabled" : true,
        "value" : 0.5
      }
    }
  ],
  "supported-platforms" : {
    "circles" : [
      "watchOS"
    ],
    "squares" : "shared"
  }
}
JSON
fi

# Android: adaptive icon layers (108 dp at 4x), themed icon, Play Store icon
RES="$ROOT/app/androidApp/src/main/res"
mkdir -p "$RES/drawable-nodpi"
render 432 "$BRANDING/glyph.svg" "$RES/drawable-nodpi/ic_launcher_foreground.png"
recolor "$BRANDING/glyph.svg" "#000000" "$TMP/glyph-mono.svg"
render 432 "$TMP/glyph-mono.svg" "$RES/drawable-nodpi/ic_launcher_mono.png"
render 512 "$BRANDING/icon.svg" "$ROOT/app/androidApp/src/main/ic_launcher-playstore.png"
cat > "$RES/values/ic_launcher_background.xml" <<XML
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">$ICON_BACKGROUND</color>
</resources>
XML

# Web: icons and splash screens for each branding folder
python3 - "$BRANDING/glyph.svg" "$TMP" <<'PY'
import re, sys
glyph = open(sys.argv[1]).read()
inner = re.search(r"<svg[^>]*>(.*)</svg>", glyph, re.S).group(1)
def splash(background, color):
    body = inner.replace("#ffffff", color)
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">'
            f'<rect width="512" height="512" fill="{background}"/>'
            f'<g transform="translate(128 128) scale(0.25)">{body}</g></svg>\n')
open(f"{sys.argv[2]}/splash-light.svg", "w").write(splash("#f8fafc", "#2563eb"))
open(f"{sys.argv[2]}/splash-dark.svg", "w").write(splash("#0f172a", "#60a5fa"))
PY
for dir in "$ROOT"/app/webApp/branding/*/; do
  [[ "$(basename "$dir")" == test ]] && continue
  # A folder with its own artwork (no generated marker) is left alone
  [[ -f "$dir/.generated-icons" ]] || continue
  mkdir -p "$dir/images"
  render 192 "$BRANDING/icon.svg" "$dir/images/icon-192.png"
  render 360 "$BRANDING/icon.svg" "$dir/images/icon-360.png"
  render 512 "$BRANDING/icon.svg" "$dir/images/icon-512.png"
  render 180 "$BRANDING/icon.svg" "$dir/images/apple-touch-icon.png"
  cp "$TMP/splash-light.svg" "$dir/images/splash-light.svg"
  cp "$TMP/splash-dark.svg" "$dir/images/splash-dark.svg"
done
echo "Icons written from branding/icon.svg and branding/glyph.svg"
