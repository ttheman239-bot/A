#!/usr/bin/env bash
# Build pipeline: aapt2 -> javac -> dx -> zipalign -> apksigner.
# All tools come from Ubuntu's android-sdk packages — no network needed at build time.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$ROOT/app"
SRC_DIR="$APP_DIR/src/main/java"
RES_DIR="$APP_DIR/src/main/res"
MANIFEST="$APP_DIR/src/main/AndroidManifest.xml"
KEYSTORE_DIR="$APP_DIR/keystore"
BUILD="$ROOT/build"
DIST="$ROOT/dist"

ANDROID_SDK="${ANDROID_HOME:-/usr/lib/android-sdk}"
ANDROID_JAR="$ANDROID_SDK/platforms/android-23/android.jar"
AAPT2="${AAPT2:-/usr/bin/aapt2}"
DX="/usr/lib/android-sdk/build-tools/debian/dx"
ZIPALIGN="${ZIPALIGN:-/usr/bin/zipalign}"
APKSIGNER="${APKSIGNER:-/usr/bin/apksigner}"

PKG="com.asiaproxy.trader"
APP_LABEL="Asia-US-Trader"
KEY_ALIAS="androiddebugkey"
KEY_PASS="android"
KEY_STORE="$KEYSTORE_DIR/debug.keystore"

mkdir -p "$BUILD/compiled-res" "$BUILD/gen" "$BUILD/classes" "$BUILD/dex" "$DIST" "$KEYSTORE_DIR"

echo "==> Cleaning old artifacts"
rm -rf "$BUILD"/compiled-res/* "$BUILD"/gen/* "$BUILD"/classes/* "$BUILD"/dex/*
rm -f "$BUILD"/app-unsigned.apk "$BUILD"/app-aligned.apk
rm -f "$DIST"/AsiaUSTrader-debug.apk

echo "==> Verifying tools"
for t in "$ANDROID_JAR" "$AAPT2" "$DX" "$ZIPALIGN" "$APKSIGNER"; do
    [ -e "$t" ] || { echo "Missing tool: $t" >&2; exit 1; }
done
javac -version
echo "Using android.jar: $ANDROID_JAR"

echo "==> Compiling resources (aapt2)"
# Compile each resource file to a .flat file
find "$RES_DIR" -type f \( -name "*.xml" -o -name "*.png" -o -name "*.jpg" \) -print0 |
while IFS= read -r -d '' f; do
    "$AAPT2" compile -o "$BUILD/compiled-res" "$f"
done

echo "==> Linking resources (aapt2 link)"
mapfile -t flat_files < <(find "$BUILD/compiled-res" -name "*.flat" | sort)
"$AAPT2" link \
    -o "$BUILD/app-unsigned.apk" \
    -I "$ANDROID_JAR" \
    --manifest "$MANIFEST" \
    --java "$BUILD/gen" \
    --min-sdk-version 26 \
    --target-sdk-version 34 \
    --version-code 1 \
    --version-name 1.1 \
    --auto-add-overlay \
    "${flat_files[@]}"

echo "==> Preparing lambda stubs (java.lang.invoke.* missing from android.jar 23)"
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
# Eclipse compiler — Java 21's javac can't target 1.8 against android.jar 23
# (missing java.lang.invoke.MethodHandles). We supply that on the bootclasspath.
ecj \
    -1.8 \
    -bootclasspath "$ANDROID_JAR:$LAMBDA_STUBS" \
    -d "$BUILD/classes" \
    -proc:none \
    -nowarn \
    "${java_files[@]}"

echo "==> Converting classes to DEX (dx)"
"$DX" --dex --min-sdk-version=26 --output="$BUILD/dex/classes.dex" "$BUILD/classes"

echo "==> Adding classes.dex to APK"
( cd "$BUILD/dex" && zip -j -X "$BUILD/app-unsigned.apk" classes.dex >/dev/null )

echo "==> Aligning"
"$ZIPALIGN" -p -f 4 "$BUILD/app-unsigned.apk" "$BUILD/app-aligned.apk"

echo "==> Ensuring debug keystore"
if [ ! -f "$KEY_STORE" ]; then
    keytool -genkey -v -keystore "$KEY_STORE" \
        -alias "$KEY_ALIAS" \
        -storepass "$KEY_PASS" -keypass "$KEY_PASS" \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=Asia US Trader Debug, OU=Dev, O=Local, L=Bangkok, S=BKK, C=TH"
fi

echo "==> Signing (apksigner v1+v2)"
# minSdk 26 -> only v2/v3 needed; v1 (JAR signing) is unnecessary and conflicts
# with our late-added classes.dex (not in pre-existing MANIFEST.MF).
"$APKSIGNER" sign \
    --ks "$KEY_STORE" \
    --ks-key-alias "$KEY_ALIAS" \
    --ks-pass "pass:$KEY_PASS" \
    --key-pass "pass:$KEY_PASS" \
    --v1-signing-enabled false \
    --v2-signing-enabled true \
    --v3-signing-enabled true \
    --min-sdk-version 26 \
    --out "$DIST/AsiaUSTrader-debug.apk" \
    "$BUILD/app-aligned.apk"

echo "==> Verifying signature"
"$APKSIGNER" verify --verbose "$DIST/AsiaUSTrader-debug.apk" || true

echo
echo "===================================================================="
echo "  APK built: $DIST/AsiaUSTrader-debug.apk"
ls -lh "$DIST/AsiaUSTrader-debug.apk"
echo "===================================================================="
