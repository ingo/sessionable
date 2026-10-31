# Fork it, brand it, ship it

Sessionable is a conference app for any event that publishes its schedule on
[Sessionize](https://sessionize.com). There is no server: the app reads the event's public Sessionize feed, and
everything the attendee adds (bookmarks, notes, ratings, people met, photos) stays on the device.

There are two ways to use this repository.

1. **The generic app.** Build it as it is. On first launch it asks for a Sessionize link, checks it, and opens that
   conference. Attendees can connect to several conferences and switch between them. This is what the stores get.
2. **A branded app for one conference.** Fork it, change the few files below, and the app opens straight on your
   conference with your name, colors and icon, and no connect screen.

## What a Sessionize link is

On the event's Sessionize dashboard, under API / embed, enable the **All** endpoint. Its link looks like
`https://sessionize.com/api/v2/abcd1234/view/All`; the id is `abcd1234`. The app accepts that link, any other
`/api/v2/<id>/...` link of the event, or the bare id.

## A branded app in five steps

1. **Describe the conference.** In `app/shared/src/commonMain/kotlin/org/jetbrains/kotlinconf/event/EventProfiles.kt`
   add an `EventProfile`: the Sessionize id, name, time zone, links, and optionally a theme (`EventTheme`), a
   venue floor plan (`VenueMap`) and `SessionizeRules` for questions your event asks speakers (for example where
   slides are uploaded). `AgntconMcpconNa2026` is a complete example. Keep `storageScope` stable once the app has
   shipped: changing it makes installed apps lose their data.
2. **Select it.** Set `event=<your profile id>` in `gradle.properties` (or pass `-Pevent=<id>`; Xcode builds read
   `ORG_GRADLE_PROJECT_event`). With a value there is no connect screen and no "Switch conference".
3. **Name the app.**
   - Android: `appId` in `gradle.properties`, and `app_name` in `app/androidApp/src/main/res/values/strings.xml`.
   - iOS: `PRODUCT_BUNDLE_IDENTIFIER` and `PRODUCT_NAME` in `app/iosApp/KotlinConf.xcodeproj/project.pbxproj`,
     or pass them to `scripts/ios-testflight.sh <event-id> <bundle-id> <display-name> <team-id>`.
   - Versions live in four places that must agree (Android `build.gradle.kts`, the iOS project and `Info.plist`,
     `values/version.xml`); `./gradlew prepareRelease` bumps them together.
4. **Replace the icon.** Edit `branding/icon.svg` (square, opaque) and `branding/glyph.svg` (the mark alone, white
   on transparent, inside the middle two thirds), set the colors in `branding/brand.env`, and run
   `scripts/make-icons.sh`. It writes the iOS, Android and web icons.
5. **Fix the credits and links.** `URLs.kt` has the project's GitHub repository (used by "Report a problem").
   Keep the JetBrains credit, the Apache-2.0 licence and the "unofficial, not affiliated" wording, which the
   licence and the trademarks of the parts this builds on require.

For the web build, copy `app/webApp/branding/generic` to a folder named for your event, edit `manifest.json` and
`brand.env`, and point `BRAND` and `EVENT` in `.github/workflows/web-deploy.yml` at them.

## What is in the box

- Schedule, search and filters (topic, level, format, company), speakers and companies, a venue map where the
  profile has one.
- **Now** and **My schedule** tabs: what is on now and next, your bookmarks on a timeline with clashes marked.
- Notes on sessions and speakers, private ratings, people met, add to calendar (`.ics`), share, a Markdown trip report.
- Photos and videos, attached from a session's own page (camera, library or files; not in the web build).
- Optional sync of bookmarks with the event's Sessionize web app, for profiles that set `sessionizeAppUrl`.
- English and German.

## Build and test

```bash
./gradlew :core:jvmTest :app:shared:jvmTest          # tests (snapshots of real feeds in testdata/)
./gradlew :app:androidApp:assembleDebug              # Android (needs the SDK; JDK 21)
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun    # web
scripts/ios-testflight.sh <event-id> <bundle-id> <display-name> <team-id> --upload
```

Pass `-Pevent=<id>` to any of them to build a branded app instead of the generic one.
