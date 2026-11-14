#!/usr/bin/env bash
# Builds android/build/Namaz100.apk without Gradle.
# Needs (Ubuntu): apt-get install aapt apksigner zipalign dalvik-exchange android-sdk-platform-23 default-jdk
set -euo pipefail
unset JAVA_TOOL_OPTIONS  # proxy settings not needed for building
cd "$(dirname "$0")"
SDK=/usr/lib/android-sdk/platforms/android-23/android.jar
rm -rf build gen-assets && mkdir -p build/gen build/classes

python3 prepare_assets.py
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -I "$SDK"
javac -nowarn -Xlint:-options -source 8 -target 8 -bootclasspath "$SDK" -d build/classes \
  src/com/namaz100/app/*.java build/gen/com/namaz100/app/R.java
dalvik-exchange --dex --min-sdk-version=24 --output=build/classes.dex build/classes
aapt package -f -M AndroidManifest.xml -S res -A gen-assets -I "$SDK" -F build/unsigned.apk
(cd build && aapt add -f unsigned.apk classes.dex >/dev/null)
zipalign -f -p 4 build/unsigned.apk build/aligned.apk

if [ ! -f namaz100.keystore ]; then
  keytool -genkeypair -keystore namaz100.keystore -storepass namaz100 -keypass namaz100 -alias namaz100 \
    -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Namaz 100, O=Personal" >/dev/null 2>&1
fi
apksigner sign --ks namaz100.keystore --ks-pass pass:namaz100 --ks-key-alias namaz100 \
  --out build/Namaz100.apk build/aligned.apk
apksigner verify --print-certs build/Namaz100.apk | head -1
ls -la build/Namaz100.apk
