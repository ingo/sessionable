# Conference notebook (the "larger Sessionize app") - Validation Report

## Executive Summary

**Recommendation: PIVOT** (build it as a free, local-first tool; do not plan on it as a revenue business)

Nothing found does what this app does end to end: a personal, on-device record of a conference that takes the agenda
from *anywhere*, lets you attach cleaned-up photos, slide text, notes and ratings to each session, and hands you a
structured trip report to process later. The pieces exist separately (organizer apps, agenda-to-calendar converters,
photo-to-note tools), and no multi-conference personal planner has replaced Lanyrd. But the people who would use it are
served for free by the organizer's own app, willingness to pay is unproven, and the market reports are about
organizers paying, not attendees. So the case is strong as a product people love and weak as a business.

The pivot: drop the "Sessionize app" framing. Sessionize becomes one *source* among several (feed, URL, PDF, screenshot),
and the product becomes **"any agenda in, trip report out"**.

## The Idea

**Problem:** At a conference you collect schedule, photos of slides, notes and contacts across several apps and your
camera roll, and afterwards you have to piece the trip together by hand. The organizer's app is per-event, often
locked to their data, and gone by next month.
**Users:** Developers, architects, analysts and speakers who attend several conferences a year and write trip reports,
or feed their notes to an LLM.
**Solution:** One app, on the phone, no account. Import the agenda (Sessionize feed, a URL, a PDF, a screenshot of the
agenda). Attach artifacts to sessions (photos straightened and colour-corrected, text read from them, slides, notes,
ratings, people met). Export a trip report (markdown, html, zip with photos and `.txt` text files) for later processing.

Status: already built for AGNTCon on the open-source KotlinConf base; the generalisation is `ingo/sessionize-app`.

## Market Analysis

### Market Size
| Metric | Value | Notes |
|--------|-------|-------|
| TAM (market reports) | **$1.1B to $14.6B** (2025) | Event-app reports disagree by 13x because they define the market differently. Narrow "mobile event app software": $1.14B in 2025, 7% CAGR (Report Prime). Broad "event app": $14.62B in 2025, 14.1% CAGR (Emergen Research). Both measure what **organizers** pay, not attendees. |
| SAM (attendee-paid, Apple, professional conferences) | **not sourced** | No data found on what attendees spend on personal conference tools. Illustrative assumption only: 1 to 3 million frequent professional attendees reachable on iOS x $20 a year = $20M to $60M. Treat as unverified. |
| SOM (3 years, one developer) | **$15K to $75K a year** realistic; $0.2M to $3M if 1 to 5% of the assumed SAM | The low figure is the one to plan on: attendees get an organizer app for free, so they must be persuaded to pay for a second one. |

### Market Maturity
**Stage:** Mature for organizer-paid event apps (Whova, Sched, Swapcard, Guidebook, EventMobi, Cvent); **unserved** for
personal, cross-conference tools (Lanyrd shut down; AlternativeTo's replacements are Meetup-style directories).

For a new entrant the mature side is a trap (organizers buy, you would compete with funded platforms). The unserved
side is open but small and unmonetised.

### Growth Trends
- Event-app spend grows 7% to 14% a year (reports above), driven by hybrid events and organizers wanting data.
- AI note and capture tools are growing fast but are built for meetings and audio (Otter, Granola), not for slides
  photographed in a room.
- On-device AI (Vision text reading today; Apple's on-device language models where available) makes private capture and
  parsing possible without a backend. Verify model availability per iOS version before relying on it.

## Competitive Landscape

### What exists, by what it does

| Group | Examples | What they do | What they don't |
|-------|----------|--------------|-----------------|
| **Organizer event apps** (per event, organizer pays) | Whova, Sched, Swapcard, Guidebook, EventMobi, Cvent, eventScribe and EventPilot (Cadmium) | Schedule, networking, maps, polls, some session notes and slide annotation | Work only for the events that bought them; no multi-event history; no export of a trip report found; Whova reviewers complain about notification overload and navigation |
| **Free attendee apps from tooling vendors** | Sessionize attendee app (a free PWA, per-event, favorites synced by a 6-digit code); Conference Attendee by Engineerica (4.8 stars, 4.7K ratings, free) | Personal schedule; Engineerica adds notes, ratings, a social feed and an attendance log | Sessionize's documentation lists no notes, offline mode or data export, and it covers Sessionize events only; Engineerica is a social, credit-tracking app for member-association events and is cloud based |
| **Agenda-to-calendar converters** | PDF to Calendar Converter (iPhone, 3.0 stars from 6 ratings, ICS export behind a $39.99 a year subscription, reviewers say it doesn't work), Agenda Hero (Chrome extension, screenshot to calendar entry), Taskade, CalendarBridge | Turn a photo, PDF or web page into dated calendar entries | Produce calendar events, not sessions you can attach things to; no capture; no report |
| **Capture and knowledge tools** | photes.io (photo to note, Notion and Obsidian export), Obsidian plugins such as TEGAKI (slide screenshots to notes via a cloud vision API), Apple Notes and Google Keep scans, Otter (captures slide images in virtual meetings), Granola (audio plus your notes, in person) | Turn images or audio into text notes | No schedule, so no session structure; cloud processing of slides (TEGAKI itself warns about this); built for meetings, not rooms |
| **Multi-conference personal planner** | Lanyrd (gone) | Followed conferences across events | Nothing current replaces it |

### Feature Matrix
Legend: ✓ found in a source, ✗ source says not offered or none found, ? not verified.

| Feature | Whova / Sched | Conference Attendee | Sessionize app | PDF-to-Calendar / Agenda Hero | photes / TEGAKI / Notes | This app |
|---------|---------------|---------------------|----------------|-------------------------------|--------------------------|----------|
| Works for any conference | ✗ (per event) | ✗ (per event) | ✗ (Sessionize events) | ✓ | ✓ | ✓ (goal) |
| Agenda from a feed, URL, PDF, screenshot | ✗ | ✗ | feed only | PDF, photo, web page | ✗ | feed today; URL, PDF, screenshot are the work to do |
| Personal schedule | ✓ | ✓ | ✓ | calendar only | ✗ | ✓ |
| Notes and ratings per session | ✓ (notes, polls) | ✓ | ? (none documented) | ✗ | notes, no sessions | ✓ |
| Photos on a session | ? | ✓ (social feed) | ✗ | ✗ | ✗ (no sessions) | ✓ |
| Photo of a screen straightened and colour-corrected | ✗ | ✗ | ✗ | ✗ | scan apps only | ✓ |
| Text read from photos and kept with them | ✗ | ✗ | ✗ | ✗ | ✓ (often cloud) | ✓ (on device, in the PR queue) |
| Trip report export (md, html, zip, text) | ✗ | ✗ | ✗ | ICS | notes export | ✓ |
| No account, no cloud, no tracking | ✗ | ✗ (cloud service) | ✓ (no login) | ✗ | ✗ (cloud OCR) | ✓ |
| People met, with notes | ✓ (networking) | ✓ | ✗ | ✗ | ✗ | ✓ |
| History across several conferences | ✗ | ✗ | ✗ | ✗ | ✗ | goal (needs events as data) |

### Differentiation Opportunities
1. **Any agenda in.** Import from a URL (Sessionize, schema.org event markup, iCal), a PDF, or a screenshot, with a
   review screen like the photo-matching one the app already has. Nobody turns an agenda into sessions you can attach
   things to; the converters stop at calendar entries.
2. **Capture that stays on the phone.** Straighten, colour-correct and read slide photos on the device and keep the text
   next to the photo. Competing capture tools send slides to cloud APIs, which matters for conference material under NDA.
3. **A trip report that is data, not a screenshot of an app.** A stable folder or JSON of sessions, photos, text and notes
   that an LLM can digest. No competitor found exports one.
4. **Own history across events.** The Lanyrd gap: one place for every conference you went to.

## Revenue Potential

Not a strong business on these numbers. Estimates below are assumptions, not sourced.

### Projections
| Timeframe | Users | ARPU | Revenue |
|-----------|-------|------|---------|
| Year 1 | 1K to 5K | $0 to $1 (free; a few buy a pro tier) | $0 to $5K |
| Year 3 | 10K to 50K if word of mouth works | $1 to $3 | $10K to $150K |

An optional pro tier at $20 a year with 3% conversion gives about $600 per 1,000 users.

### Monetization Strategy
**Recommended model:** free and local-first, open source. Three side paths worth testing, in order of effort:
1. Branded builds for community conferences that want a native, offline app on top of their Sessionize feed
   ($300 to $1,000 an event is a guess; Sessionize's own app is free, so the pitch is native offline plus notes plus
   photos, not the schedule).
2. An optional paid "trip report processing" tier, only if it can run on the device or with the user's own key.
3. Team and speaker plans (shared reports); not started.

**Reasoning:** the free organizer apps set the price attendees expect at zero, and the best differentiators (privacy,
no account) are hard to charge for.

## Risk Assessment

| Risk | Level | Mitigation |
|------|-------|------------|
| Technical (agenda parsing from screenshots and PDFs) | **High** | The one existing PDF converter has 3.0 stars and reviews saying it fails. Parse with on-device text reading plus layout rules, always show a review screen, and test on 10 real agendas before committing. |
| Technical (generalising from a hard-coded event) | Medium | `EventProfiles.kt` holds all event specifics; move events to stored data. Keep the storage version unchanged unless a migration is written. |
| Competition | Low to Medium | Organizer apps are not aimed at this job; Sessionize could add notes and export to its free app. Stay ahead on capture and the report. |
| User acquisition | **High** | No store audience searches for this; reach is by community and word of mouth (conference Slack channels, speakers). |
| Monetization | **High** | Free alternatives; unproven willingness to pay. |
| Platform dependency | Medium | App Store review of on-device AI claims; Vision and Photos permissions already handled. Keep "No network except the agenda" true and say so. |
| Privacy and rights | Medium | Photos of slides may carry the speaker's copyright; the report is personal use. Keep everything on the device and say so in the app. |

## Final Recommendation

**PIVOT**

### Suggested pivot
From "the larger Sessionize app" to **"Conference notebook: any agenda in, trip report out"**, free and open source,
with Sessionize as one importer.

### Why this is better
- It matches the one gap nobody fills (personal, multi-conference, capture to report), instead of competing with
  organizer apps and Sessionize's free PWA on the schedule.
- Most of the hard capture work is already built (matching, de-duplication, clean-up, text read, report zip).
- The risk that remains is agenda import quality, and that is cheap to test before building.

### Next steps (in order)
1. **Spike agenda import on 10 real agendas** (a Sessionize page, a Sched page, a PDF programme, a screenshot of a
   table, an iCal file). Success: at least 90% of sessions correct and under two minutes per conference, with the
   review screen fixing the rest.
2. **Make events data, not code:** an event record on the device with a name, time zone, venue and sessions, replacing the
   hard-coded profile. Keep the storage version unless a migration is added.
3. **Import by URL or file first** (Sessionize feed, iCal, schema.org markup): deterministic and cheap. Then PDF
   (text layer first), then screenshot (on-device text reading plus layout).
4. **Write down the trip report format** (a `report.json` next to `report.md`) so other tools and LLMs can rely on it.
5. **Show it to 5 attendees** at the next conference before building a pro tier.

### If you would rather go for revenue
It is a NO-GO as a standalone subscription app. The only revenue path with some evidence is branded builds for
community conferences, which needs a pitch and a pilot, not more features.

## Sources
- Event-app market size: Emergen Research ($14.62B in 2025, 14.12% CAGR) and Report Prime ($1.14B in 2025, 7%), as reported by search results; the estimates differ by 13x, so they are directional only.
- Sessionize attendee app: sessionize.com/playbook/mobile-and-web-app and /playbook/embedding.
- Conference Attendee (Engineerica) and PDF to Calendar Converter App Store pages, fetched 26 October 2026.
- Whova reviews: Capterra and G2 summaries; Whova and Sched pricing: aggregator sites that disagree, so no figure is relied on.
- Agenda Hero, Taskade, CalendarBridge, photes.io, TEGAKI (Obsidian), Otter and Granola comparisons: vendor and review pages found by search.
- Lanyrd: AlternativeTo listing (shut down).

---
*Generated by SwiftShip /apple:validate*
*Date: 2026-10-26*
