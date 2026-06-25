# Signing & Release Build

This document describes how Smart Call Blocker is signed and how to verify a release was built with the official key. **Passwords and the keystore itself are not stored in this repository** — see [Security Notes](#security-notes) at the bottom.

---

## Release certificate (public)

The release AABs and APKs on the [Releases](https://github.com/itsashishpandey/ai-call-blocker/releases) page are signed by Triple Minds with the following certificate:

| Field | Value |
|---|---|
| Owner / Issuer | `CN=Triple Minds, OU=Mobile, O=Triple Minds, L=Unknown, ST=Unknown, C=US` |
| Algorithm | SHA-256 with RSA |
| Key size | 2048 bit |
| Validity | 10 000 days from 2026-05-28 (≈ 27 years) |
| Alias | `scb-upload` |

### Fingerprints

```
SHA-256: DD:71:D8:06:E6:6F:D4:B9:41:48:4B:30:78:20:61:1E:29:19:08:2D:CF:06:F4:2F:EA:82:36:D6:A0:7A:39:BF
SHA-1:   00:B1:7E:6F:EA:B9:97:9A:8E:85:7F:04:6C:B3:ED:0F:64:DD:3C:8E
```

These fingerprints are **public information** and safe to publish — they let anyone verify a release without revealing any secret. They are also what Google Play and Android use to recognise updates as coming from the same publisher.

---

## How to verify a downloaded build

Once you've downloaded `app-release.apk` (or `app-release.aab`) from the GitHub Releases page:

```bash
# Print the certificate the file was signed with
keytool -printcert -jarfile app-release.apk
```

Look for the SHA-256 line and compare it to the fingerprint above. If they match, the file is genuine. If they don't, the file has been tampered with — discard it.

You can also confirm the certificate of an installed copy of the app on a connected device:

```bash
adb shell pm dump com.smartcallblocker | grep -A1 "Signing"
```

---

## How releases are built locally

The release pipeline is plain Gradle:

```bash
./gradlew bundleRelease       # produces app/build/outputs/bundle/release/app-release.aab
./gradlew assembleRelease     # produces app/build/outputs/apk/release/app-release.apk
```

R8 minification, resource shrinking, and signing are wired into the `release` build type in [`app/build.gradle.kts`](./app/build.gradle.kts). The signing config reads a local `keystore.properties` file (gitignored) at the project root. The build will still succeed if `keystore.properties` is missing — it just produces an unsigned bundle that you'd have to sign yourself.

### keystore.properties (local only — NEVER commit)

If you are reproducing the release build locally with your own keystore, create a file at the project root called `keystore.properties` with these keys:

```properties
storeFile=keystore/release.keystore
storePassword=…       # your local store password
keyAlias=…            # your local key alias
keyPassword=…         # your local key password
```

The file is listed in [.gitignore](./.gitignore). Do not paste your own keystore credentials into commits, issues, pull requests, or comments.

---

## Generating a fresh keystore (for forks)

If you are forking this project and need your own signing key:

```bash
# Create the keystore directory
mkdir keystore

# Generate a 2048-bit RSA upload key valid for ~27 years
keytool -genkeypair -v \
  -keystore keystore/release.keystore \
  -alias my-upload \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Your Name, O=Your Org, C=US"
# You will be prompted for a store password and key password — choose strong values.

# Point app/build.gradle.kts at your keystore by creating keystore.properties:
cat > keystore.properties <<EOF
storeFile=keystore/release.keystore
storePassword=<your-store-password>
keyAlias=my-upload
keyPassword=<your-key-password>
EOF
```

Then `./gradlew bundleRelease` will pick it up automatically.

---

## Play App Signing

For the production listing on Google Play we use **Play App Signing**: Triple Minds owns the **upload key** described above, while Google holds and manages the actual app-signing key. This is the official Google recommendation for new apps because:

- If our upload key is ever lost, Google can reset it (no lost-keystore catastrophe)
- The app-signing key is rotated separately by Google's infrastructure
- Devices verify installs against the Google-managed key, not the upload key

The certificate fingerprint above is the **upload key** — Play Console will show a different certificate for the app-signing key once Play App Signing is enrolled.

---

## Security Notes

- The `.keystore` file, `keystore.properties`, and `local.properties` are listed in [.gitignore](./.gitignore) and have never been committed to this repository.
- The fingerprints above are derived from the public certificate stored inside every signed APK/AAB — they are designed to be publishable and tell an attacker nothing about the private key.
- If you believe the release key has been compromised, please email `support@tripleminds.co` immediately.
- For ordinary users who just want to install the app, the simplest verification is to compare the **SHA-256 fingerprint above** against what `keytool -printcert -jarfile` reports for the file you downloaded.

---

Maintainer: [@itsashishpandey](https://github.com/itsashishpandey) · Published by [Triple Minds](https://tripleminds.co/)
