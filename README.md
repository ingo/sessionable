# Sessionable

A conference app for any event that publishes its schedule on [Sessionize](https://sessionize.com): Android, iOS,
desktop and web from one Kotlin Multiplatform codebase. There is no server; the app reads the event's public
Sessionize feed, and everything you add stays on your device.

- **Use it:** open the app, paste the conference's Sessionize link, and you have its schedule, a Now view, your own
  timeline, notes, ratings, the people you met, calendar export and photos on sessions.
- **Brand it:** fork this repository, change a few files, and ship an app for your own conference.
  See [docs/FORKING.md](docs/FORKING.md).

## Credit

This is a fork of the [KotlinConf app](https://github.com/JetBrains/kotlinconf-app) by
[JetBrains](https://www.jetbrains.com/) and its [contributors](https://github.com/JetBrains/kotlinconf-app/graphs/contributors).
Its architecture, screens, design system and most of the code are theirs: the shared Compose Multiplatform UI,
navigation, schedule, speakers, search and filters, and session reminders. Thank you to the KotlinConf team for
building it so well and for publishing it under the Apache License 2.0.

This app is not affiliated with or endorsed by JetBrains, KotlinConf, Sessionize or the Linux Foundation. KotlinConf
and Kodee are trademarks of JetBrains. The AGNTCon + MCPCon name belongs to the Linux Foundation and is used here only
as a worked example of a branded build. The code is under the [Apache License 2.0](LICENSE), as the original; the
changes made in this fork are listed below and in the git history.

## What this fork adds

**Any Sessionize conference, with no backend**
- Conference profiles ([`EventProfiles.kt`](app/shared/src/commonMain/kotlin/org/jetbrains/kotlinconf/event/EventProfiles.kt)):
  name, time zone, links and theme per conference, picked at build time with `-Pevent=<id>`. Without it, the app
  asks for a Sessionize link at run time and connects to any conference ("Sessionable", the generic build).
- Reads a public Sessionize feed directly, so a conference needs no server of its own. Times work whether the feed sends
  UTC or event-local times; schedule filters (topic, level, format) come from the feed's categories.
- The KotlinConf server and the features that needed it (voting, feedback, Golden Kodee, partners, legal documents,
  push notifications) are gone; the app talks to nobody but Sessionize.
- Per-conference look: light and dark colors, logo, app icon and launch screen.

**Planning your conference**
- **My schedule**, a tab with two views:
  - **Day**: your bookmarked sessions on a timeline. Overlapping sessions sit side by side and are marked as clashes;
    all-day items (registration, exhibitions) stay out of the way; free time is split at the conference's own breaks
    (breakout rounds, lunch) with a button to browse the sessions starting then and bookmark them in place.
  - **Now**: during the conference, what you are in and how long is left, what's next and how soon, its room and floor
    (with a hint when you need to change floors), free time before it, and the rest of the day.
- **Session pages** open with how long until the session starts (or how long is left), its room and floor, highlighted
  in the last 15 minutes, so you can tell at a glance whether you are in the right room for the right session.
- **Sync with the Sessionize web app**: link with the 6-digit code from the event's web app ("Sync favorites") and
  bookmarks stay in step both ways. This uses the same calls as Sessionize's own web app; they are not a documented
  API and may change, in which case the app keeps working from its own bookmarks.

**At the conference: notes, people and sharing** (all of it stays on your device; nothing needs a server)

<p>
  <img src="docs/screenshots/session-top.png" width="190" alt="Session page with add to calendar, share and find on map">
  <img src="docs/screenshots/session-rating-notes.png" width="190" alt="A thumbs-up rating, a one-line takeaway and notes on a session">
  <img src="docs/screenshots/speaker-links.png" width="190" alt="A speaker page with LinkedIn and other links">
  <img src="docs/screenshots/people-i-met.png" width="190" alt="The people I met, with notes and LinkedIn">
</p>
<p>
  <img src="docs/screenshots/companies-showcase.png" width="190" alt="Companies with a talk in the Solutions Showcase">
  <img src="docs/screenshots/search-and-map-pin.png" width="190" alt="Search results with a map pin on each room">
  <img src="docs/screenshots/calendar-export.png" width="190" alt="Calendar tab with add to calendar, share and trip report">
  <img src="docs/screenshots/share-sheet.png" width="190" alt="The iOS share sheet with the schedule as text">
</p>

- **Notes** on any session or speaker, **People I met** (mark a speaker as met; the Met tab lists them with your note and
  their LinkedIn link) and **private ratings** (thumbs up or down and a one-line takeaway; shown for events without
  their own voting).
- **Add to calendar** for one session or all your bookmarks (an `.ics` file), **share** a session or your schedule as
  text, and a **trip report** in Markdown: your sessions in time order with speakers, rating, takeaway, notes, slides and
  recordings, plus the people you met. It goes through the platform's share sheet.
- **Companies**: speakers' companies from the feed, searchable, with a **Solutions Showcase** filter to browse exhibitor
  talks by company; a company filter in the schedule's search; speaker links (LinkedIn, blog, company site).
- **Better search**: the schedule search also matches session descriptions and speakers' companies, and **Find on map**
  on a session's room (and a pin on schedule rows) opens the venue map on that room.

**Fixes to the original**
- Sessions are grouped into their own time slots; a long session (such as all-day registration) no longer swallows the
  sessions inside it.
- Duplicate list keys that crashed the schedule's bookmarks view when time slots shared a start time.
- Short day labels when a conference has more days than fit on a phone.

**Testing**
- Snapshots of real Sessionize feeds ([`testdata/sessionize`](testdata/sessionize)) for one event-local and one UTC
  conference. Tests check parsing and time zones, schedule grouping, unique list keys and the personal calendar over many
  random sets of bookmarks; each was confirmed to fail with the bug it guards against.
- The Sessionize sync client is tested against a mock server with the requests and responses of the real web app.
- The GitHub Actions workflow runs the core and app tests and the iOS build for each conference profile.

## Building

See [docs/FORKING.md](docs/FORKING.md) for branding the app for your own conference. Requirements: JDK 21, the
Android SDK for Android, Xcode for iOS ([setup guide](https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-setup.html)).

```bash
./gradlew :core:jvmTest :app:shared:jvmTest          # tests
./gradlew :app:androidApp:assembleDebug              # Android
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun    # web
./gradlew :app:desktopApp:hotRun -DmainClass=org.jetbrains.kotlinconf.MainKt   # desktop
```

Add `-Pevent=<profile id>` for a build branded for one conference.
