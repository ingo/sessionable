#!/usr/bin/env bash
# Regenerates the app icons of every platform from one picture, so branding an app means replacing it:
#
#   branding/icon.png    the app icon: square, at least 1024 px, fully opaque (the stores round the corners)
#   branding/brand.env   the colors of the web app's loading page
#
#   scripts/make-icons.sh
#
# Writes the iOS asset catalog and Icon Composer icon, the iOS launch image, the Android launcher icons and
# Play Store icon, and the icons and splash screens of the web app and of each web branding folder marked with
# a .generated-icons file (app/webApp/branding/<name>). Needs Python with Pillow and NumPy
# (pip install pillow numpy).
set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
python3 -c "import PIL, numpy" 2>/dev/null || { echo "Pillow and NumPy are needed: pip install pillow numpy" >&2; exit 1; }
[[ -f "$ROOT/branding/icon.png" ]] || { echo "Missing branding/icon.png" >&2; exit 1; }

python3 - "$ROOT" <<'PY'
import base64, io, json, os, pathlib, sys
import numpy as np
from PIL import Image, ImageFilter

root = pathlib.Path(sys.argv[1])
source = Image.open(root / "branding/icon.png").convert("RGB")
if source.width != source.height or source.width < 1024:
    sys.exit("branding/icon.png must be square and at least 1024 px")

def size(px):
    return source.resize((px, px), Image.LANCZOS)

# The launch screens are one solid color with the badge on it: the white part of the icon (without the
# lanyard, which runs off the top edge), on transparent
LAUNCH = "#00a99d"
rgb = np.asarray(source, dtype=np.float32)
white = np.clip((rgb.min(axis=2) - 170) / 85, 0, 1)
white[: int(source.height * 0.265)] = 0
rows, cols = np.where(white > 0.05)
box = (cols.min(), rows.min(), cols.max() + 1, rows.max() + 1)
cut = np.zeros((source.height, source.width, 4), dtype=np.uint8)
cut[..., :3] = 255
cut[..., 3] = (white * 255).astype(np.uint8)
badge_full = Image.fromarray(cut, "RGBA").crop(box)
side = max(badge_full.size)
badge_square = Image.new("RGBA", (side, side), (255, 255, 255, 0))
badge_square.paste(badge_full, ((side - badge_full.width) // 2, (side - badge_full.height) // 2))

def badge(px):
    return badge_square.resize((px, px), Image.LANCZOS)

def save(image, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)

# iOS asset catalog: every size the catalog asks for (no alpha channel allowed)
ios = root / "app/iosApp/iosApp"
catalog = ios / "Assets.xcassets/AppIcon.appiconset"
for image in json.load(open(catalog / "Contents.json"))["images"]:
    if image.get("filename"):
        px = round(float(image["size"].split("x")[0]) * int(image["scale"].rstrip("x")))
        save(size(px), catalog / image["filename"])

# iOS Icon Composer icon: the picture as one layer
composer = ios / "AppIcon.icon"
if composer.exists():
    for old in (composer / "Assets").glob("*"):
        old.unlink()
    save(size(1024), composer / "Assets/Icon.png")
    (composer / "icon.json").write_text(json.dumps({
        "groups": [{"layers": [{"image-name": "Icon.png", "name": "Icon"}]}],
        "supported-platforms": {"circles": ["watchOS"], "squares": "shared"},
    }, indent=2) + "\n")

# iOS launch image shown while the app starts
launch = ios / "Assets.xcassets/launch-glyph.imageset"
for scale in (1, 2, 3):
    save(badge(120 * scale), launch / f"launch-glyph@{scale}x.png")
(launch / "launch-glyph.svg").unlink(missing_ok=True)
(launch / "Contents.json").write_text(json.dumps({
    "images": [{"filename": f"launch-glyph@{s}x.png", "idiom": "universal", "scale": f"{s}x"} for s in (1, 2, 3)],
    "info": {"author": "xcode", "version": 1},
}, indent=2) + "\n")

background_set = ios / "Assets.xcassets/launch-background.colorset"
background_set.mkdir(parents=True, exist_ok=True)
r, g, b = (int(LAUNCH[i:i + 2], 16) / 255 for i in (1, 3, 5))
(background_set / "Contents.json").write_text(json.dumps({
    "colors": [{"idiom": "universal", "color": {"color-space": "srgb", "components": {
        "red": f"{r:.3f}", "green": f"{g:.3f}", "blue": f"{b:.3f}", "alpha": "1.000"}}}],
    "info": {"author": "xcode", "version": 1},
}, indent=2) + "\n")

# Android adaptive icon: 108 dp at 4x. Only the middle two thirds show through the mask, so the
# picture is shrunk into that area and the gradient around it is continued outwards.
res = root / "app/androidApp/src/main/res"
canvas, inner = 432, 324
art = np.asarray(size(inner), dtype=np.float32)
pad = (canvas - inner) // 2
extended = np.pad(art, ((pad, canvas - inner - pad), (pad, canvas - inner - pad), (0, 0)), mode="edge")
blurred = Image.fromarray(extended.astype(np.uint8)).filter(ImageFilter.GaussianBlur(24))
background = np.asarray(blurred).copy()
background[pad:pad + inner, pad:pad + inner] = art.astype(np.uint8)
save(Image.fromarray(background), res / "drawable-nodpi/ic_launcher_background.png")
# Themed icon: the white badge as a silhouette
silhouette = (art.min(axis=2) > 235).astype(np.uint8) * 255
mono = np.zeros((canvas, canvas, 4), dtype=np.uint8)
mono[pad:pad + inner, pad:pad + inner, 3] = silhouette
save(Image.fromarray(mono, "RGBA"), res / "drawable-nodpi/ic_launcher_mono.png")
(res / "drawable-nodpi/ic_launcher_foreground.png").unlink(missing_ok=True)
save(badge(432), res / "drawable-nodpi/splash_icon.png")
(res / "values/colors.xml").write_text('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n    <color name="splash_background">%s</color>\n</resources>\n' % LAUNCH.upper())
(res / "values-night/colors.xml").write_text((res / "values/colors.xml").read_text())
(res / "drawable/ic_launcher.xml").write_text(
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
    '    <background android:drawable="@drawable/ic_launcher_background"/>\n'
    '    <foreground android:drawable="@android:color/transparent"/>\n'
    '    <monochrome android:drawable="@drawable/ic_launcher_mono"/>\n'
    '</adaptive-icon>\n')
(res / "values/ic_launcher_background.xml").unlink(missing_ok=True)
save(size(512), root / "app/androidApp/src/main/ic_launcher-playstore.png")

# Web: icons and splash screens
def splash():
    buffer = io.BytesIO()
    badge(256).save(buffer, "PNG")
    data = base64.b64encode(buffer.getvalue()).decode()
    return (f'<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512" width="512" height="512">'
            f'<rect width="512" height="512" fill="{LAUNCH}"/>'
            f'<image x="128" y="128" width="256" height="256" href="data:image/png;base64,{data}"/></svg>\n')

targets = [root / "app/webApp/src/webMain/resources"]
branding = root / "app/webApp/branding"
targets += [d for d in sorted(branding.iterdir()) if d.is_dir() and (d / ".generated-icons").exists()]
for target in targets:
    images = target / "images"
    for name, px in (("icon-192", 192), ("icon-360", 360), ("icon-512", 512), ("apple-touch-icon", 180)):
        save(size(px), images / f"{name}.png")
    (images / "splash-light.svg").write_text(splash())
    (images / "splash-dark.svg").write_text(splash())
print("Icons written from branding/icon.png")
PY
