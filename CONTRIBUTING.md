# Contributing to Smart Call Blocker

Thanks for considering a contribution. Smart Call Blocker is built and maintained by [Triple Minds](https://tripleminds.co/) and we welcome bug reports, feature requests, and pull requests from the community.

## Quick links

- 🐛 [Report a bug](https://github.com/itsashishpandey/ai-call-blocker/issues/new?labels=bug)
- 💡 [Request a feature](https://github.com/itsashishpandey/ai-call-blocker/issues/new?labels=enhancement)
- 💬 Email — `support@tripleminds.co`

## Ground rules

- **Be respectful.** This project is governed by basic decency.
- **One change per pull request.** Easier to review, easier to revert.
- **Match existing style.** Kotlin idioms, Compose conventions, Material 3.
- **Add or update tests** when behaviour changes.
- **Never commit secrets** — passwords, keystores, signing properties. See [SIGNING.md](./SIGNING.md).

## Reporting a bug

Use the issue template. Please include:

- **Device + Android version** (e.g. *Samsung Galaxy S23 · Android 14*)
- **App version** (Settings → About Us shows it)
- **Steps to reproduce**
- **What you expected** vs **what happened**
- **Logcat** if it's a crash: `adb logcat -s CallScreener`

For call-blocking failures, also include the rule type, the pattern, and an example incoming number (you can redact the last 4 digits if you prefer).

## Suggesting a feature

Open an issue with the `enhancement` label. Tell us:

- The problem you're trying to solve (not just the solution you imagined)
- Who else might benefit
- Any relevant Android API references

## Code contributions

### Setup

```bash
git clone https://github.com/itsashishpandey/ai-call-blocker.git
cd ai-call-blocker
# Open in Android Studio Koala (2024.1.1+) and let Gradle sync
# OR build from CLI:
./gradlew assembleDebug
```

You'll need:
- **JDK 17**
- **Android SDK 35** (compileSdk)
- **Android 10+** (API 29) device or emulator to test

### Project layout

```
app/src/main/java/com/smartcallblocker/app/
├── data/
│   ├── backup/         ← SAF-backed JSON backup
│   ├── db/             ← Room entities, DAOs, AppDatabase
│   ├── preferences/    ← DataStore settings
│   └── repository/     ← Repository pattern over DAOs
├── di/                 ← Hilt modules
├── domain/
│   ├── engine/         ← RuleEngine (the brain)
│   └── model/          ← CallDecision, IncomingCall, RuleType
├── service/            ← CallScreeningService, BootReceiver, QS Tile
├── ui/
│   ├── components/     ← Reusable Compose pieces
│   ├── navigation/     ← NavGraph + bottom bar scaffold
│   ├── screens/        ← One folder per screen
│   └── theme/          ← Material 3 Trust-Shield palette
├── util/               ← Number normaliser, masker, contacts lookup
└── widget/             ← Glance home-screen widget
```

### Style

- **Kotlin only.** No new Java files.
- **Compose for UI.** No XML layouts.
- **MVVM.** Screens get a `*ViewModel : ViewModel` injected via `hiltViewModel()`.
- **One responsibility per repository.**
- **Suspend functions for I/O.** Never block the call-screening thread.
- **No comments that restate code.** Only the *why* for non-obvious decisions.

### Testing

- Unit tests live in `app/src/test/java`
- Add tests for any change to `RuleEngine` or `PhoneNumberNormalizer` — these are load-bearing.
- For UI, add screenshot/instrumentation tests in `app/src/androidTest/java` where it makes sense.

### Pull request checklist

- [ ] `./gradlew assembleDebug` succeeds
- [ ] `./gradlew test` passes
- [ ] You bumped `versionCode` if this ships to users
- [ ] Updated [README.md](./README.md) if you added a user-visible feature
- [ ] You didn't commit `keystore.properties`, `keystore/`, or `local.properties`

## Code of conduct

Be kind. We're all here because we want fewer spam calls.

---

Maintainer: [@itsashishpandey](https://github.com/itsashishpandey) · Published by [Triple Minds](https://tripleminds.co/)
