# Z Browser — v0.1.0 torrent foundation

**Status:** source-only development kit, not a compiled APK or actual Brave fork. The upstream repository could not be cloned in this environment (DNS/network restriction), and Android SDK/Gradle are unavailable. The actual browser integration is not performed. The downloadable project is an independent Android Studio lab plus a host integration contract, source-discovery scripts and an engineering plan. No simulated transfer data.

## A. Test the Android torrent engine

1. Open `android-lab/` in Android Studio as a Gradle project. The application ID is deliberately `com.sanjid.browser.lab`, to prevent collisions with the eventual browser ID `com.sanjid.browser`. Java namespace: `com.sanjid.browser.torrent`.
2. Use JDK 17, Android SDK 36 and Gradle 8.13 (AGP 8.11.1). There is no Gradle wrapper JAR in this archive; select a local Gradle distribution in Android Studio or install Gradle 8.13 and use `gradle wrapper --gradle-version 8.13` locally. Internet access is required to resolve dependencies.
3. Sync, then Run on an ARM64 Android 10+ phone or x86_64 emulator. AAR pinned: `com.frostwire:jlibtorrent-android:2.1.2.1@aar` from FrostWire's published Maven repo. It provides the Java wrapper and native libraries; confirm ABI inclusion, packaging and 16KB page compatibility on your target devices.
4. Use a small authorized test torrent (prefer a local seeded file or an official Linux ISO). Paste a magnet or import `.torrent` using the document picker; confirm the P2P disclosure. Try pause and resume. One job at a time. Stop after completion and export a completed file using Android's save dialog. Files remain in app-private internal storage until explicitly exported; uninstalling may remove them. Stopping an incomplete job keeps partial bytes but this version cannot restore the stopped job.
5. Check `adb logcat -s ZTorrent` for redacted engine exceptions. No full diagnostic ZIP in this version.

## B. Build the actual Brave baseline on Linux cloud

Follow https://github.com/brave/brave-core and https://github.com/brave/brave-browser/wiki/Android-Development-Environment first. Install prerequisites, including pnpm and the supported Node/Python versions. Create your own fork at https://github.com/brave/brave-core/fork if desired. Choose a known stable Brave Core tag or full commit SHA from that repo; do not blindly use rolling master for production.

From this archive on the cloud host:

```bash
bash scripts/bootstrap-brave.sh "$HOME/zbrowser" YOUR_VERIFIED_BRAVE_CORE_REF https://github.com/YOUR_ACCOUNT/brave-core.git
cd "$HOME/zbrowser/src/brave"
pnpm run build Debug --target_os=android --target_arch=arm64 --target_android_output_format=apk
```

Omit the fork URL to clone upstream directly. This is a local development branch, not a hosted fork. Verify the build flags with the selected revision's README if they change. Locate APKs under `src/out/` and verify package identity before installing; a baseline is still Brave, not Z Browser. Do not upload or distribute it as Z Browser. Use `scripts/inspect-brave.sh "$HOME/zbrowser"` to discover the selected revision's integration points. Follow `docs/BRAVE_INTEGRATION.md` to integrate the native module and rebrand the real target.

For Android Studio indexing, follow the checked-out Chromium `docs/android_studio.md` to generate a Gradle IDE project from the actual GN output directory. GN/Ninja remains authoritative for compiling Chromium. No fabricated `BUILD.gn` patch is supplied because the actual upstream tree could not be inspected here.

## Validation performed here

`bash scripts/test-contract.sh` compiled and ran 12 pure-Java assertions: transfer merging, progress/ETA, input validation, metadata limit and path validation. No native or Android build/test was possible. Dependency resolution, libtorrent API compatibility, background behavior, real peer transfers, and Android ABI packaging remain unverified. Do not distribute this lab as a release.

See `docs/BRAVE_INTEGRATION.md` for phased implementation and test gates; `docs/FEATURES.md` for status; `docs/AGENT_TASK.md` for a coding-agent handoff.
