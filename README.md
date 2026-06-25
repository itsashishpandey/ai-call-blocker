<div align="center">

<img src="play_assets/play_store_icon_512.png" alt="AI Spam Call Blocker icon" width="128" height="128" />

# AI Spam Call Blocker

### Intelligent, offline-first Android spam call blocker — block spam calls, robocalls, and scammers without giving up your privacy

[![Latest Release](https://img.shields.io/github/v/release/itsashishpandey/ai-call-blocker?label=Latest%20release&color=1E40AF)](https://github.com/itsashishpandey/ai-call-blocker/releases/latest)
[![Min SDK](https://img.shields.io/badge/Android-10%2B-3B82F6)](https://developer.android.com/about/versions/10)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-10B981)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
[![Built by Triple Minds](https://img.shields.io/badge/Built%20by-Triple%20Minds-1E3A8A)](https://tripleminds.co/)

<br />

### 📱 [⬇️ Download APK (latest release)](https://github.com/itsashishpandey/ai-call-blocker/releases/latest)

*Direct APK install. No Play Store account required. Production AAB is awaiting Play Store review.*

<br />

</div>

---

## Why AI Spam Call Blocker?

Spam calls, robocalls, scammers, and persistent telemarketers waste your time, drain your battery, and erode trust in every unknown number. AI Spam Call Blocker is a **free, open-source, ad-free Android call blocker** that silences every unwanted call while making sure the calls that matter — family, doctors, delivery, work — still get through.

Built by [Triple Minds](https://tripleminds.co/) with **one strict rule: nothing leaves your device.** No tracking, no analytics, no third-party SDKs phoning home. Your call history, contacts, blacklist, and rules are processed locally and stay locally.

**Looking for one of these?**

- An **Android call blocker app** that works without an internet connection? ✅
- A **spam call blocker** that doesn't sell your contact list to a "spam database"? ✅
- A **rule-based call screener** with regex, prefix, suffix, country-code, and schedule blocking? ✅
- A **privacy-first call blocker** for Pixel, Samsung, OnePlus, Xiaomi, Realme, Vivo, OPPO, Motorola, Sony? ✅
- A **Kotlin + Jetpack Compose** sample of a production Android call screening app? ✅

You're in the right place.

---

## Table of contents

- [Features](#features)
- [Screenshots](#screenshots)
- [Download & install](#download--install)
- [How it works](#how-it-works)
- [Permissions](#permissions)
- [Privacy](#privacy)
- [Tech stack](#tech-stack)
- [Project architecture](#project-architecture)
- [Build from source](#build-from-source)
- [Backup & restore](#backup--restore)
- [FAQ](#faq)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [About](#about)

---

## Features

### 🛡️ 24 blocking rule types — match exactly how you want

| Category | Rule types |
|---|---|
| **Pattern matching** | Starts with, Ends with, Contains, Exact number, Regex pattern |
| **Number metadata** | Unknown numbers, Known contacts (strict), Private / hidden, Empty caller ID |
| **Length** | Fewer than N digits, More than N digits |
| **Origin** | Country code, Area code (NDC), Toll-free, Premium-rate |
| **Behavioural** | Repeated calls within window, Allow-only-contacts, Schedule (time + day), Spam score |
| **Carrier** | STIR/SHAKEN verification failure (carrier-flagged spam) |
| **Personal lists** | Whitelist (always allow), Blacklist (always block) |

All patterns accept the digits you actually type — `9876` matches `+919876543210` whether you wrote it with the country code or without. Spaces, dashes, parens, and dots in the pattern are stripped automatically.

### ⚡ Smart repeated-call detection

A telemarketer calling **3 times in 5 minutes**? Auto-blocked for **30 minutes**. Every threshold is customisable. Auto-unblock happens silently when the window expires.

### ✅ Whitelist priority — important calls always come through

Add family, work, doctors, schools, or delivery numbers to the whitelist. They always ring, no matter what blocking rules you've created. Whitelist beats every rule, including carrier-spam flags.

### ⏱️ Safe mode timer

Expecting an important call from an unknown number? **One tap allows every call for 15, 30, or 60 minutes**, then snaps back to full protection automatically. Useful for cab pickups, deliveries, hospital callbacks, job interviews.

### 🚨 Carrier-flagged spam blocking *(new in 1.0.3)*

Auto-reject calls your phone's dialer marks with the red "Suspected Spam" banner. Reads Android's STIR/SHAKEN verification signal — the same signal Samsung Phone, Google Phone, OnePlus Phone, and Xiaomi Phone use to flag unverified callers. Works best on Pixel, Samsung, OnePlus, and recent Xiaomi devices with their built-in caller-ID protection turned on.

### 🔒 Privacy-first by design

- **100% offline** — no servers, no internet required to function
- **Zero tracking** — no analytics, no ads, no third-party SDKs
- **Privacy Mode** — mask numbers in the blocked-call log (`+91 ******3210`)
- **App lock** — biometric or device PIN gate on every open
- **Privacy Policy & Fair Use Policy** included in-app

### 📊 Insights dashboard & statistics

- Today / week / month / all-time blocked counts
- 30-day trend chart (rendered with Compose `Canvas`)
- Top blocked rules, top countries, top numbers — all sortable
- Live last-blocked card on the home screen

### 📱 Native Android integrations

- **Quick Settings tile** — toggle protection from the notification shade with one tap
- **Home-screen widget** (Glance-based, 2×2 resizable) — shows live blocked-today count and shield status
- **Material You dynamic colours** on Android 12+
- **Light, dark, and follow-system** themes
- **Adaptive launcher icon** with the Trust Shield brand mark

### 💾 Backup & Restore — survives uninstall

Pick **any folder** on your phone (Documents, Google Drive sync folder, an SD-card folder, anywhere). Every time you add a rule, block a number, edit a setting, or get a new blocked-call entry, the app writes a single human-readable `smart_call_blocker_backup.json` file to that folder within ~1 second. The write is atomic (`.tmp` + rename) so a power cut mid-write cannot corrupt the backup.

After reinstalling the app — even on a new device — point at the same folder and the app detects the backup, asks "Restore previous data?", and replays every rule, whitelist entry, blacklist entry, blocked-call entry, and setting in a single Room transaction.

### 🚨 Emergency-safe by design

Emergency numbers (911, 112, 100, 101, 108, 999, etc.) are always allowed, regardless of any rule you set. The app uses Android's `TelephonyManager.isEmergencyNumber()` API plus a hard-coded fallback list. **AI Spam Call Blocker cannot be used to block emergency services.**

---

## Screenshots

> _Sideload the APK and grab fresh screenshots — these placeholders will be replaced with real screens before the Play Store launch._

| Dashboard | Rules editor | Statistics |
|---|---|---|
| _coming soon_ | _coming soon_ | _coming soon_ |

| Blocked log | Backup & Restore | Onboarding |
|---|---|---|
| _coming soon_ | _coming soon_ | _coming soon_ |

---

## Download & install

### Option 1 — Sideload the APK (available today)

1. Go to [Releases → latest](https://github.com/itsashishpandey/ai-call-blocker/releases/latest)
2. Download `app-release.apk` (~5 MB)
3. On your Android phone, tap the file in your downloads folder
4. Allow **"Install unknown apps"** for your file manager when prompted
5. Tap **Install**
6. Open the app → walk through onboarding → tap **"Set as Default Screening App"** when prompted

### Option 2 — Google Play Store

The production AAB is currently in Play Store review. Watch [the Releases page](https://github.com/itsashishpandey/ai-call-blocker/releases) for the Play Store link.

### Verify the download

Before installing, you can confirm the APK is genuine. See [SIGNING.md](./SIGNING.md) for the full process. TL;DR:

```bash
keytool -printcert -jarfile app-release.apk
# Look for: SHA-256 fingerprint
# Must match: DD:71:D8:06:E6:6F:D4:B9:41:48:4B:30:78:20:61:1E:29:19:08:2D:CF:06:F4:2F:EA:82:36:D6:A0:7A:39:BF
```

---

## How it works

AI Spam Call Blocker is built on Android's official [`CallScreeningService`](https://developer.android.com/reference/android/telecom/CallScreeningService) API — the same hook used by Google Phone and other major call-blocking apps.

```
1. User makes AI Spam Call Blocker the default screening app
        ↓
2. Phone rings → Android tells our CallScreeningService first, BEFORE the dialer
        ↓
3. Service builds an IncomingCall (number, isPrivate, isKnownContact, isCarrierFlaggedSpam, …)
        ↓
4. RuleEngine evaluates against priority order:
        Emergency → Whitelist → Safe Mode → Carrier Spam → Temp Block
        → Blacklist → User rules → Repeated-call detection → Default Allow
        ↓
5. Engine returns a CallDecision (Allow / Block with action: REJECT | SILENCE | DISALLOW)
        ↓
6. Service translates to CallResponse and tells Android — within a 1.5-second budget
        ↓
7. If blocked, the call is logged asynchronously to the Blocked list (Room DB)
```

### Engine priority (highest first)

| Priority | Check | Outcome |
|---|---|---|
| 1 | Emergency number | **Always allow** |
| 2 | Whitelist match | **Always allow** |
| 3 | Safe mode active | **Always allow** (timer-based bypass) |
| 4 | Carrier-flagged spam (if toggle on) | **Block** |
| 5 | Active temporary block | **Block** |
| 6 | Blacklist match | **Block** |
| 7 | User rule match | **Block** with rule's chosen action |
| 8 | Repeated-call threshold exceeded | **Block** + create temp block |
| 9 | None of the above | **Allow** |

---

## Permissions

We ask for the **bare minimum**. We do **NOT** request `READ_CALL_LOG`, `ANSWER_PHONE_CALLS`, `INTERNET`, `POST_NOTIFICATIONS`, or `MANAGE_EXTERNAL_STORAGE`.

| Permission | Used for |
|---|---|
| `READ_PHONE_STATE` | Required by Android to register as the default Call Screening app |
| `READ_CONTACTS` | Recognise calls from people in your address book so they aren't accidentally blocked |
| `RECEIVE_BOOT_COMPLETED` | Re-arm temporary blocks after device reboot |
| `USE_BIOMETRIC` (optional) | App-lock feature only — not requested unless you turn the lock on |

These permissions are never used for advertising, profiling, marketing, analytics, or any purpose other than call screening as described above.

---

## Privacy

AI Spam Call Blocker is **offline-first**. Your data never leaves your device.

- ❌ No analytics
- ❌ No advertising SDKs
- ❌ No crash reporters
- ❌ No third-party telemetry
- ❌ No "community spam database" upload
- ❌ No cloud sync (you control where backups go via SAF)
- ✅ Open source — read every line of code in this repo
- ✅ Privacy Policy + Fair Use Policy included in-app under Settings → About & Legal
- ✅ App lock with biometrics for added local security
- ✅ Privacy Mode masks numbers in the blocked log

For the full Privacy Policy text, see Settings → About & Legal → Privacy Policy inside the app, or read it at [tripleminds.co](https://tripleminds.co/).

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | **Kotlin 2.0** |
| UI | **Jetpack Compose** + Material 3 |
| Architecture | **MVVM** + Clean Architecture |
| DI | **Hilt** |
| Database | **Room** |
| Settings | **DataStore Preferences** |
| Async | **Kotlin Coroutines + Flow** |
| Number parsing | **Google libphonenumber** |
| Background work | **WorkManager** |
| Widget | **Glance** (Compose for AppWidgets) |
| Biometric | **AndroidX Biometric** |
| File backup | **Storage Access Framework + DocumentFile** |
| Splash | **AndroidX Core SplashScreen** |
| Build | **AGP 8.7.3** + Gradle 8.11.1 + KSP |

| Target | Value |
|---|---|
| `minSdk` | 29 (Android 10) |
| `targetSdk` | 35 (Android 15) |
| `compileSdk` | 35 |

---

## Project architecture

```
app/src/main/java/com/smartcallblocker/app/
├── data/
│   ├── backup/         JSON backup writer + restore + SAF helpers
│   ├── db/             Room entities, DAOs, AppDatabase
│   ├── preferences/    DataStore-backed SettingsRepository
│   └── repository/     Repos over DAOs (BlockedCalls, Rules, Whitelist, Blacklist, TemporaryBlock)
├── di/                 Hilt modules
├── domain/
│   ├── engine/         RuleEngine — the decision logic
│   └── model/          CallDecision, IncomingCall, RuleType
├── service/
│   ├── CallBlockerScreeningService.kt   The OS hook
│   ├── BootCompletedReceiver.kt          Re-arm temp blocks
│   ├── ProtectionTileService.kt          Quick Settings tile
│   └── ScreeningRoleManager.kt           Helper for default-screener prompt
├── ui/
│   ├── components/     StatCard, EmptyState, ScreenTopBar, LegalArticle
│   ├── navigation/     NavGraph + bottom-bar scaffold
│   ├── screens/
│   │   ├── backup/         Backup & Restore screen
│   │   ├── blacklist/
│   │   ├── blocked/        Blocked-call history
│   │   ├── dashboard/      Home
│   │   ├── legal/          About, Privacy, Fair Use
│   │   ├── onboarding/     4-page intro
│   │   ├── rules/          List + Add/Edit
│   │   ├── settings/
│   │   ├── splash/         Animated Compose splash
│   │   ├── statistics/     30-day chart + top lists
│   │   └── whitelist/
│   └── theme/          Trust-Shield Material 3 palette
├── util/               PhoneNumberNormalizer, ContactsLookup, EmergencyNumbers,
│                       PhoneNumberMasker, BiometricGate
└── widget/             Glance home-screen widget
```

---

## Build from source

### Prerequisites

- **JDK 17**
- **Android SDK 35** (install via Android Studio's SDK Manager)
- A device or emulator running **Android 10 (API 29) or newer**

### Clone and build

```bash
git clone https://github.com/itsashishpandey/ai-call-blocker.git
cd ai-call-blocker

# Tell Gradle where your Android SDK lives
echo "sdk.dir=$ANDROID_HOME" > local.properties
# Windows PowerShell:
# echo "sdk.dir=$env:LOCALAPPDATA\Android\Sdk" > local.properties

# Build a debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Install on a connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Build a release AAB (your own signing key)

See [SIGNING.md](./SIGNING.md) for the full process. Quickstart:

```bash
mkdir keystore
keytool -genkeypair -v \
  -keystore keystore/release.keystore \
  -alias my-upload \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Your Name, O=Your Org, C=US"

cat > keystore.properties <<EOF
storeFile=keystore/release.keystore
storePassword=<your-store-password>
keyAlias=my-upload
keyPassword=<your-key-password>
EOF

./gradlew bundleRelease assembleRelease
# Outputs:
#   app/build/outputs/bundle/release/app-release.aab
#   app/build/outputs/apk/release/app-release.apk
```

---

## Backup & restore

The app writes one JSON file to a folder you pick via Storage Access Framework:

```json
{
  "schemaVersion": 1,
  "appVersion": "1.0.3",
  "publisher": "Triple Minds",
  "timestamp": 1717182000000,
  "settings": {
    "protectionEnabled": true,
    "defaultAction": "REJECT",
    "blockCarrierSpam": true,
    "privacyMode": false,
    "repeatedLimit": 3,
    "...": "..."
  },
  "rules": [ /* every BlockingRuleEntity */ ],
  "whitelist": [ /* every WhitelistEntity */ ],
  "blacklist": [ /* every BlacklistEntity */ ],
  "blockedCalls": [ /* every BlockedCallEntity */ ],
  "temporaryBlocks": [ /* every TemporaryBlockEntity */ ]
}
```

This file is:

- **Human-readable** — open it in any text editor
- **Auto-written** within ~1.2 seconds of any change (debounced)
- **Atomic** — `.tmp` + rename, so power cut mid-write cannot corrupt it
- **Portable** — moves between devices, survives uninstall, can be edited by hand

To restore on a fresh install: **Settings → Backup & Restore → Pick backup folder** → pick the same folder you chose previously → confirm "Restore" on the prompt. Done.

---

## FAQ

### Will this app block Truecaller / Google Phone's own spam tags?

Partially. Android only exposes a **standard verification signal** (STIR/SHAKEN) to third-party screening apps — not the proprietary spam databases that Truecaller, Google, Samsung, or Hiya maintain. With **Carrier Spam Detection** enabled (Settings → Carrier spam detection), AI Spam Call Blocker reads that standard signal and blocks any call your network marks as unverified. For best coverage, keep your dialer's own "Caller ID & spam protection" turned on alongside AI Spam Call Blocker. See [the in-app disclaimer](#screenshots) for full details.

### Does it work without an internet connection?

Yes. The app is **100% offline**. No feature requires internet. The only thing you'd need internet for is the *initial APK download* and *optional Play Store updates*.

### Will it slow down my phone?

No. The screening service runs only when a call comes in, returns a decision in under 200 ms on a typical Android 12+ device, and consumes negligible battery. The DataStore + Room layer is fully native and free of reflection.

### What happens to my data if I uninstall?

By default, all data is removed when you uninstall (Android's standard scoped-storage behaviour). If you've enabled **Backup & Restore** to a folder of your choice, the JSON backup file in that folder survives uninstall — you can restore it on the next install.

### Does it work on Samsung / Xiaomi / OnePlus / OPPO / Vivo?

Yes — the app uses Android's standard CallScreeningService API which all major OEMs support. A few OEM dialers may need their built-in spam database turned on for the new **Carrier Spam Detection** feature to surface signals to Android. Aggressive battery managers on Xiaomi MIUI, Realme, Vivo, and OPPO sometimes kill background services — whitelist AI Spam Call Blocker in your device's Autostart / Battery saver settings if you notice calls slipping through.

### Can I block international country codes?

Yes. Settings → Rules → ＋ → "Country code" → type `+92` (or whatever code) to block every call from that country.

### Can I block all calls from numbers shorter than 8 digits (likely spam)?

Yes. Settings → Rules → ＋ → "Fewer than X digits" → enter `8`.

### Can I block based on a regex?

Yes. Settings → Rules → ＋ → "Regex pattern" → e.g. `^\+1(800|888|877)` to block US toll-free.

### Can I have different rules at different times of day?

Yes — Schedule fields exist on every rule. UI for the schedule editor is rolling out in a future release; meanwhile schedule data persists if you set it via the API.

---

## Roadmap

Planned features (community input welcome — [open an issue](https://github.com/itsashishpandey/ai-call-blocker/issues)):

- [ ] Schedule rule editor (time + day picker in Add/Edit Rule)
- [ ] CSV import/export for whitelist & blacklist
- [ ] Quick "block the last unknown caller" action
- [ ] Per-SIM rules for dual-SIM devices
- [ ] Auto-suggest rules from missed-call patterns
- [ ] Localization — Hindi, Spanish, Portuguese, Arabic, Indonesian
- [ ] Wear OS companion app
- [ ] CSV / JSON export of blocked-call statistics

---

## Contributing

Pull requests welcome. See [CONTRIBUTING.md](./CONTRIBUTING.md) for setup, style, and how to file a useful bug report.

For security issues, please email `support@tripleminds.co` instead of opening a public issue.

---

## License

[MIT](./LICENSE) © 2026 Triple Minds.

You are free to fork, modify, and redistribute. If you publish a fork on Google Play, please rebrand it sufficiently (different name, different package, different icon) to avoid user confusion.

---

## About

**AI Spam Call Blocker** is built and maintained by **Triple Minds** — privacy-respecting mobile tools. Website: [tripleminds.co](https://tripleminds.co/). Contact: [support@tripleminds.co](mailto:support@tripleminds.co).

Maintainer: [@itsashishpandey](https://github.com/itsashishpandey)

If this app saved you from a robocall, ⭐ the repo. If it didn't, [tell us why](https://github.com/itsashishpandey/ai-call-blocker/issues).

---

<details>
<summary>Keywords (helps people find this app)</summary>

AI spam call blocker, Android spam call blocker, AI call blocker app Android, free spam call blocker, open source spam call blocker, offline spam call blocker, privacy-first spam call blocker, block robocalls Android, block telemarketers Android, block spam calls Android, block unknown numbers Android, default call screening app, STIR SHAKEN spam blocker, Samsung spam call blocker, Pixel spam call blocker, OnePlus spam call blocker, Xiaomi spam call blocker, OPPO Vivo Realme spam call blocker, regex call blocker, prefix call blocker, country code call blocker, scheduled call blocker, Kotlin spam call blocker app, Jetpack Compose Android app, Material 3 Android app, CallScreeningService example, AI Spam Call Blocker by Triple Minds.

</details>
