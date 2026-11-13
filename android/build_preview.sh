#!/usr/bin/env bash
# Builds "Namaz 100 Preview": behaves like the Google Play version (locked challenge, ₹199 unlock)
# with a simulated purchase. Separate package, so it installs next to the personal app.
set -euo pipefail
cd "$(dirname "$0")"
unset JAVA_TOOL_OPTIONS
SDK=/usr/lib/android-sdk/platforms/android-23/android.jar
OUT=build-preview
rm -rf "$OUT" gen-assets && mkdir -p "$OUT/gen" "$OUT/classes"
python3 prepare_assets.py
python3 - <<'PY'
p='gen-assets/www/namaz-tracker.html'; s=open(p,encoding='utf-8').read()
s=s.replace('<script>\n(() => {', '<script>window.__BUYER_PREVIEW=true;</script>\n<script>\n(() => {',1)
assert '__BUYER_PREVIEW=true' in s
open(p,'w',encoding='utf-8').write(s)
PY
sed 's/android:label="Namaz 100"/android:label="Namaz 100 Preview"/' AndroidManifest.xml > "$OUT/AndroidManifest.xml"
aapt package -f -m -J "$OUT/gen" -M "$OUT/AndroidManifest.xml" -S res -I "$SDK"
javac -nowarn -Xlint:-options -source 8 -target 8 -bootclasspath "$SDK" -d "$OUT/classes" src/com/namaz100/app/*.java "$OUT/gen/com/namaz100/app/R.java"
dalvik-exchange --dex --min-sdk-version=24 --output="$OUT/classes.dex" "$OUT/classes"
aapt package -f -M "$OUT/AndroidManifest.xml" -S res -A gen-assets -I "$SDK" --rename-manifest-package com.namaz100.preview -F "$OUT/unsigned.apk"
(cd "$OUT" && aapt add -f unsigned.apk classes.dex >/dev/null)
zipalign -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
apksigner sign --ks namaz100.keystore --ks-pass pass:namaz100 --ks-key-alias namaz100 --out "$OUT/Namaz100-Preview.apk" "$OUT/aligned.apk"
ls -la "$OUT/Namaz100-Preview.apk"
