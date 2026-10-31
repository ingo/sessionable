# CLAUDE.md

Guidance for Claude Code (claude.ai/code) when working in this repository.

## What this is

Sessionable: a conference app for any event that publishes its schedule on Sessionize, a fork of JetBrains' open-source
[KotlinConf app](https://github.com/JetBrains/kotlinconf-app) (Apache-2.0). Kotlin Multiplatform with Compose
Multiplatform for iOS, Android, JVM desktop and web. No backend: it reads the public Sessionize `view/All` feed and
everything the attendee adds stays on the device. Two modes: the generic build (no `-Pevent`) connects to any
conference by a pasted Sessionize link; a branded build (`-Pevent=<id>`) is fixed to one profile. Read `docs/FORKING.md`.

## Commands

```bash
./gradlew :core:jvmTest :app:shared:jvmTest          # tests (snapshots of real feeds in testdata/)
./gradlew :app:androidApp:assembleDebug              # Android (needs the SDK; JDK 21)
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun    # web
scripts/make-icons.sh                                # icons for all platforms from branding/*.svg
```

## Layout

```
:core                 shared data model and the Sessionize feed parser. No UI.
:app:ui-components    reusable Compose components and theme; own resource class UiRes.
:app:shared           screens, ViewModels, navigation, ConferenceService, DI graph (Metro).
:app:androidApp / :app:desktopApp / :app:webApp / app/iosApp   platform entry points
branding/             icon.svg, glyph.svg, brand.env: the source of every platform's icons
scripts/              icons, iOS upload, web branding, web smoke test
```

`event/EventProfiles.kt` holds what is specific to a conference. `EventSelection` is the conference being shown
(Compose state); `ConferenceService` opens, switches and leaves conferences and is the central singleton. The generic
build keeps connected conferences (`SavedEvent`) in `ApplicationStorage`. Routes are in `navigation/Routes.kt`.

## Conventions

- **No network except Sessionize.** No analytics, trackers, crash reporters or push services.
- **Storage:** keep `ApplicationStorageImpl.LATEST_STORAGE_VERSION` unchanged unless you add a migration; a different
  value wipes installed users' data. Keep a shipped profile's `storageScope` stable for the same reason.
- **Strings:** every user-facing string needs an English and a German entry.
- **Photos and videos** are attached only from a session's own page (`MediaSection`), never imported in bulk.
- **Versions:** keep Android `build.gradle.kts`, iOS `project.pbxproj`/`Info.plist` and `values/version.xml` in step.
- **Credit:** keep the JetBrains credit, the Apache-2.0 licence and the "unofficial, not affiliated" wording.
- JDK 21 is required; on this machine point Gradle at Homebrew's openjdk@21.
