#!/usr/bin/env bash
# Build the AvatarTalk APK. Uses the same Ubuntu-apt tooling as build.sh.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$ROOT/avatar"
SRC_DIR="$APP_DIR/src/main/java"
RES_DIR="$APP_DIR/src/main/res"
MANIFEST="$APP_DIR/src/main/AndroidManifest.xml"
KEYSTORE_DIR="$APP_DIR/keystore"
BUILD="$ROOT/build-avatar"
DIST="$ROOT/dist"

ANDROID_SDK="${ANDROID_HOME:-/usr/lib/android-sdk}"
ANDROID_JAR="$ANDROID_SDK/platforms/android-23/android.jar"
AAPT2="${AAPT2:-/usr/bin/aapt2}"
DX="/usr/lib/android-sdk/build-tools/debian/dx"
ZIPALIGN="${ZIPALIGN:-/usr/bin/zipalign}"
APKSIGNER="${APKSIGNER:-/usr/bin/apksigner}"

KEY_ALIAS="androiddebugkey"
KEY_PASS="android"
KEY_STORE="$KEYSTORE_DIR/debug.keystore"

mkdir -p "$BUILD/compiled-res" "$BUILD/gen" "$BUILD/classes" "$BUILD/dex" "$DIST" "$KEYSTORE_DIR"

echo "==> Cleaning"
rm -rf "$BUILD"/compiled-res/* "$BUILD"/gen/* "$BUILD"/classes/* "$BUILD"/dex/*
rm -f "$BUILD"/app-unsigned.apk "$BUILD"/app-aligned.apk
rm -f "$DIST"/AvatarTalk-debug.apk "$DIST"/AvatarTalk-debug.apk.idsig

echo "==> Verifying tools"
for t in "$ANDROID_JAR" "$AAPT2" "$DX" "$ZIPALIGN" "$APKSIGNER"; do
    [ -e "$t" ] || { echo "Missing tool: $t" >&2; exit 1; }
done

echo "==> Compiling resources (aapt2)"
find "$RES_DIR" -type f \( -name "*.xml" -o -name "*.png" -o -name "*.jpg" \) -print0 |
while IFS= read -r -d '' f; do
    "$AAPT2" compile -o "$BUILD/compiled-res" "$f"
done

echo "==> Linking resources"
mapfile -t flat_files < <(find "$BUILD/compiled-res" -name "*.flat" | sort)
"$AAPT2" link \
    -o "$BUILD/app-unsigned.apk" \
    -I "$ANDROID_JAR" \
    --manifest "$MANIFEST" \
    --java "$BUILD/gen" \
    --min-sdk-version 21 \
    --target-sdk-version 34 \
    --version-code 1 \
    --version-name 1.0.0 \
    --auto-add-overlay \
    "${flat_files[@]}"

echo "==> Preparing lambda stubs"
LAMBDA_STUBS="$BUILD/lambda-stubs.jar"
if [ ! -f "$LAMBDA_STUBS" ]; then
    JAVA_LANG_INVOKE_TMP="$BUILD/_invoke"
    rm -rf "$JAVA_LANG_INVOKE_TMP"
    mkdir -p "$JAVA_LANG_INVOKE_TMP"
    EXTRACT_SRC="$BUILD/_extract"
    mkdir -p "$EXTRACT_SRC"
    cat > "$EXTRACT_SRC/Extract.java" << 'JAVA'
import java.nio.file.*;
public class Extract {
    public static void main(String[] args) throws Exception {
        FileSystem fs = FileSystems.getFileSystem(java.net.URI.create("jrt:/"));
        Path src = fs.getPath("modules/java.base/java/lang/invoke");
        Path dst = Paths.get(args[0], "java/lang/invoke");
        Files.createDirectories(dst);
        Files.walk(src).forEach(p -> {
            try {
                Path d = dst.resolve(src.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(d);
                else Files.copy(p, d, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
}
JAVA
    ( cd "$EXTRACT_SRC" && javac Extract.java && java -cp . Extract "$JAVA_LANG_INVOKE_TMP" )
    ( cd "$JAVA_LANG_INVOKE_TMP" && jar cf "$LAMBDA_STUBS" java/ )
fi

echo "==> Compiling Java sources (ecj)"
mapfile -t java_files < <(find "$SRC_DIR" "$BUILD/gen" -name "*.java" | sort)
ecj \
    -1.8 \
    -bootclasspath "$ANDROID_JAR:$LAMBDA_STUBS" \
    -d "$BUILD/classes" \
    -proc:none \
    -nowarn \
    "${java_files[@]}"

echo "==> Converting classes to DEX (dx)"
"$DX" --dex --min-sdk-version=21 --output="$BUILD/dex/classes.dex" "$BUILD/classes"

echo "==> Adding classes.dex"
( cd "$BUILD/dex" && zip -j -X "$BUILD/app-unsigned.apk" classes.dex >/dev/null )

echo "==> Aligning"
"$ZIPALIGN" -p -f 4 "$BUILD/app-unsigned.apk" "$BUILD/app-aligned.apk"

echo "==> Ensuring keystore"
if [ ! -f "$KEY_STORE" ]; then
    keytool -genkey -v -keystore "$KEY_STORE" \
        -alias "$KEY_ALIAS" \
        -storepass "$KEY_PASS" -keypass "$KEY_PASS" \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=AvatarTalk Debug, OU=Dev, O=Local, L=Bangkok, S=BKK, C=TH"
fi

echo "==> Signing"
"$APKSIGNER" sign \
    --ks "$KEY_STORE" \
    --ks-key-alias "$KEY_ALIAS" \
    --ks-pass "pass:$KEY_PASS" \
    --key-pass "pass:$KEY_PASS" \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    --v3-signing-enabled true \
    --min-sdk-version 21 \
    --out "$DIST/AvatarTalk-debug.apk" \
    "$BUILD/app-aligned.apk"

echo "==> Verifying"
"$APKSIGNER" verify --verbose "$DIST/AvatarTalk-debug.apk" || true

echo
echo "===================================================================="
echo "  APK built: $DIST/AvatarTalk-debug.apk"
ls -lh "$DIST/AvatarTalk-debug.apk"
echo "===================================================================="
