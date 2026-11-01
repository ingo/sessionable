---
name: brand-for-your-conference
description: Brand this Sessionable app for one conference and get it ready for the App Store and Google Play. Use when someone forks the repo and wants an app for their own Sessionize event: it interviews them, adds the event profile, theme, icon and names, builds and tests the app, captures screenshots, and walks the store checklist.
---

# Brand Sessionable for your conference

Turn this repository into an app for one Sessionize event. Read `docs/FORKING.md` first; this skill is the guided
version of it. Work through the steps in order, make each change yourself, and check each one before moving on.
Ask the person only for what you cannot find out: the items in step 1.

## Rules that must hold at the end

- **No network except Sessionize.** No analytics, trackers, crash reporters or push services.
- **Keep `storageScope` stable** once an app has shipped. Changing it wipes installed users' bookmarks and notes.
- **Do not change `ApplicationStorageImpl.LATEST_STORAGE_VERSION`** unless you write a migration.
- **Every user-facing string has an entry in every language** (English, German, Spanish, French, Brazilian Portuguese in
  `composeResources/values*/strings.xml`); `StringsParityTest` checks it. To add a language, copy the English files to
  `values-<code>/`, translate them, and add the language to `CFBundleLocalizations` in `Info.plist` and `knownRegions` in
  the Xcode project. Ask the person whether their event needs another language, such as Japanese or Dutch.
- **Keep the credits:** the JetBrains credit, the Apache-2.0 licence and the "unofficial, not affiliated" wording. Do
  not use the trademarks of the parts this builds on (KotlinConf, Kodee, Sessionize) as the app's name or icon.
- **Versions agree** in Android `build.gradle.kts`, the iOS `project.pbxproj` and `Info.plist`, and
  `values/version.xml`; `./gradlew prepareRelease` bumps them together.
- JDK 21. On macOS with Homebrew: `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`.

## 1. Gather the facts

Ask for, in one message:

1. The Sessionize **API link** (`https://sessionize.com/api/v2/<id>/view/All`) or the id. On the event's Sessionize
   dashboard it is under **API / Embedding**; the "All" endpoint must be enabled. A link like
   `https://<event>.sessionize.com` is the event's web app and does not contain the id.
2. The conference **name**, **dates**, **venue name and address**, and the venue's **IANA time zone**
   (for example `Europe/Berlin`).
3. The **website**, **code of conduct** URL and, if any, the event's Sessionize **web app** address
   (`https://<event>.sessionize.com`, which turns on bookmark sync).
4. A **logo**: a square, opaque PNG of at least 1024 px for the app icon, and ideally a single-color mark with
   transparency for the in-app header.
5. **Brand colors**: a dark primary, an accent, and whether the event uses a gradient.
6. The **bundle id** and **Android application id** the stores will use, and the display name.

Check the feed before going further:

```bash
curl -s https://sessionize.com/api/v2/<id>/view/All | head -c 400
```

It should be JSON with `sessions` and `speakers`. If it is empty or an error, stop and tell the person to enable the
endpoint and publish the schedule.

## 2. Add the event profile

In `app/shared/src/commonMain/kotlin/org/jetbrains/kotlinconf/event/EventProfiles.kt` add an `EventProfile`
next to `AgntconMcpconNa2026`, and add it to `all`. Use that profile as the model. Set at least `id` (lowercase,
dashes; it is the value of the `event` Gradle property), `name`, `storageScope` (a short, stable string, for example
the year), `timeZoneId` and `sessionizeId`. Add `sessionizeAppUrl`, `aboutDescription`, `websiteUrl`,
`codeOfConductUrl` and `venueAddress` from step 1.

If the event asks speakers where to upload slides or a poster, add `sessionizeRules = SessionizeRules(slidesQuestion =
"<the exact question text in Sessionize>")`. If one room hosts exhibitor talks, set `showcaseRoom` to part of its name.

## 3. Theme (optional)

Add an `object <Name>Theme` in `EventTheme.kt` modelled on `AgntconTheme`: copy `LightColors` and `DarkColors` and
override only what the brand changes (`primaryBackground`, `accentText`, `toggleOn`, `headerGradient`, ...). Add the
single-color logo mark as `app/shared/src/commonMain/composeResources/drawable/event_<name>_mark.png` (or `.svg`) and
reference it as `mark`. Keep text readable: accent text on white needs about 4.5:1 contrast, and white text on a button
colour needs the same. `AgntconTheme` shows a darker "text" variant of the accent for exactly this reason.

## 4. Select it and name the app

- `gradle.properties`: `event=<profile id>`.
- Android: `appId` in `gradle.properties`; `app_name` in `app/androidApp/src/main/res/values/strings.xml`.
- iOS: `PRODUCT_BUNDLE_IDENTIFIER` and `PRODUCT_NAME` in `app/iosApp/Sessionable.xcodeproj/project.pbxproj`.
- Put the event's public repository in `URLs.kt` (used by "Report a problem").
- Web: copy `app/webApp/branding/generic`, edit `manifest.json` and `brand.env`, then point `BRAND` and `EVENT` in
  `.github/workflows/web-deploy.yml` at them.

## 5. Icon

Put the icon at `branding/icon.png`, set the web loading colours in `branding/brand.env`, run `scripts/make-icons.sh`.
Check a generated icon by opening it: it must be opaque and square, with the artwork away from the corners.

## 6. Build and test

```bash
./gradlew :core:jvmTest :app:shared:jvmTest -Pevent=<id>
./gradlew :app:androidApp:assembleDebug -Pevent=<id>
./gradlew :app:shared:compileKotlinIosSimulatorArm64 -Pevent=<id>
```

Tests that connect and leave conferences only run in the generic build; that is expected. Fix any failure before
continuing. Never ship with tests skipped for a reason you have not understood.

## 7. Look at it, then take screenshots

Run the app on an emulator or simulator and look at every tab at the default and at the largest text size. For iOS:

```bash
cd app/iosApp
ORG_GRADLE_PROJECT_event=<id> xcodebuild test -scheme SessionableScheme -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 17' -only-testing:SessionableUITests/ScreenshotTests \
  CODE_SIGNING_ALLOWED=NO
```

The PNGs land in `/private/tmp/sessionable-shots/`. Move the ones you want into `docs/screenshots/` and into your
store listings.

## 8. Accessibility check before you claim anything

Run the automated audit, then do a human pass:

```bash
cd app/iosApp
xcodebuild test -scheme SessionableScheme -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 17' -only-testing:SessionableUITests/AccessibilityAuditTests \
  CODE_SIGNING_ALLOWED=NO
```

Uninstall the app from the simulator first so it starts on the connect screen. Read the findings in the log; some are
false alarms (iOS keyboard buttons, contrast over images). The audit does not replace a person using VoiceOver and
TalkBack. Do not declare App Store Accessibility Nutrition Labels for anything you have not checked by hand.

## 9. Store checklist

Follow `docs/RELEASING.md`. In short:

- Host a privacy policy page and a support page (`docs/site/privacy.html` is a template; the app collects nothing).
  Both must return 200 over HTTPS before you enter them in App Store Connect and Play Console.
- App Privacy label: **Data Not Collected**. Play data safety: no data collected or shared.
- Name and trademark: do not use the Sessionize, KotlinConf or JetBrains names or marks. Say the app is unofficial.
- Screenshots for each required device size; the age rating questionnaire; export compliance (the app only uses the
  platform's HTTPS).
- CI: set the secrets listed in `docs/RELEASING.md`, then a tag or release uploads to TestFlight and Play.

## 10. Finish

Show the person what changed (`git diff --stat`), what you could not verify (usually: a real device, VoiceOver,
TalkBack, store accounts), and the steps only they can do (accounts, signing, privacy page hosting, store submission).
