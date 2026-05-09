# Asia → US Trader (Android)

Personal trading companion that answers **"what should I be watching right now,
and what should I do about it?"** for an Asia-proxy → US-stock workflow.

The home screen reads the Bangkok clock, figures out which Asia / US sessions
are live, surfaces the relevant proxy + US tickers, highlights catalysts that
are likely to print today, and outputs a verdict (Bullish hold / Pre-market
buy / Avoid / etc.) with reasoning.

## Modules

- **Home** — live Bangkok clock, market status pill, verdict card with
  reaction score, dynamic watchlist + US focus, today's catalysts, quick
  links. Auto-refreshes every 30 s.
- **Pairs** — proxy ↔ US ticker pairs grouped by sector (Semis, EV, China
  ADR, Gaming, China Consumer) with 5-star conviction. Detail screen shows
  catalyst, pre-trade checklist, and direct TradingView / Investing /
  Yahoo / Benzinga links per ticker.
- **Calendar** — recurring Asia catalysts (TSMC monthly revenue, BYD EV
  sales, Macau GGR, Foxconn revenue, China PMI, BOJ, PBOC LPR) with
  "today" highlighting based on day-of-month.
- **Rules** — when to trade / when to skip / post-event playbook / risk
  management checklists.
- **Journal** — local-only log of `(Asia signal → US reaction → verdict)`
  entries persisted in `SharedPreferences`.

## APK

The signed debug APK lives at `dist/AsiaUSTrader-debug.apk` (~42 KB).

```
package: com.asiaproxy.trader
versionName: 1.1
minSdkVersion: 26   (Android 8.0+)
targetSdkVersion: 23
permissions: INTERNET
```

Install on a device:

```
adb install -r dist/AsiaUSTrader-debug.apk
```

## Build

The project does **not** use Gradle / AGP — those need Google's Maven server
which is not reachable from this build environment. Instead `build.sh`
drives the same toolchain Android Studio uses internally:

```
aapt2 compile  →  aapt2 link  →  ecj  →  dx  →  zip  →  zipalign  →  apksigner
```

All tools come from Ubuntu packages:

| Tool       | Package                               |
|------------|---------------------------------------|
| aapt2      | `aapt`                                |
| ecj        | `ecj`                                 |
| dx         | `dalvik-exchange`                     |
| android.jar| `libandroid-23-java`                  |
| zipalign   | `zipalign`                            |
| apksigner  | `apksigner`                           |

Then:

```
./build.sh
```

That produces `dist/AsiaUSTrader-debug.apk`, signed with a self-generated
debug keystore in `app/keystore/debug.keystore` (created on first run).

### Why ecj instead of javac?

JDK 21's `javac` cannot target Java 1.8 against a bootclasspath that lacks
`java.lang.invoke.MethodHandles`, which is true for `android.jar` API 23.
The build script extracts the JDK's `java.lang.invoke` package into
`build/lambda-stubs.jar` and passes it to ecj's bootclasspath alongside
`android.jar`. The resulting bytecode uses `invokedynamic` for lambdas, so
`dx` is invoked with `--min-sdk-version=26` and the manifest pins
`minSdkVersion=26`.

## Project layout

```
app/
  src/main/
    AndroidManifest.xml
    java/com/asiaproxy/trader/
      model/        # MarketSession, Pair, Catalyst, QuickLink, Verdict, JournalEntry
      engine/       # Catalog (static data), RecommendationEngine, JournalStore
      ui/           # HomeActivity, PairsActivity, PairDetailActivity, CalendarActivity, RulesActivity, JournalActivity, WebActivity
      util/         # Views (helper for programmatic Views, dp, openUrl)
    res/
      layout/       # activity_*.xml
      drawable/     # cards, pills, button states, launcher icon
      values/       # strings, colors
build.sh
dist/AsiaUSTrader-debug.apk
```

## Notes

- All ticker data, catalyst dates, and pair definitions are static lookups
  in `Catalog.java` — no network calls during runtime aside from links the
  user taps to open external sites.
- The journal is stored locally with `SharedPreferences` using ASCII
  separators; the file is per-app private storage.
- The reaction score and verdict text are deterministic functions of the
  current Bangkok time, day-of-week, day-of-month, and active session list.
  No live price data is fetched — this is intentional for v1.1; live
  price hooks can be added in a follow-up.
