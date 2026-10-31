Snapshots of real Sessionize API v2 "All" feeds (`https://sessionize.com/api/v2/{id}/view/All`),
taken 2026-10-22, with speaker bios removed and session descriptions cut to 300 characters.
The AGNTCon file keeps the speakers' `links` and their "Company", "Speaker Title" and "Company URL"
answers, and the session answers to its two file-upload questions, "Presentation or Poster" (the
slides: 67 sessions) and "Event Logo" (2 sessions, never shown as slides). The KotlinConf file keeps
its YouTube `recordingUrl`s.
Tests use them to check parsing, time zones, schedule grouping, search and the personal calendar
against real event structures:

- `agntcon-mcpcon-na-2026.json` — id `asqam7rn`, times in event-local time (America/Los_Angeles)
- `kotlinconf-2026.json` — id `2r0suvtg`, times in UTC (Europe/Berlin)

To refresh: download the feed, strip the same fields, keep the file name.

- `full-featured-sample.json` and `full-featured-sample-utc.json` — a made-up conference (Europe/Berlin, three days,
  six rooms) that uses every option a Sessionize event can have: service and plenum sessions, an all-day item,
  parallel and overlapping talks, a split workshop, lightning talks, co-speakers, a session without speakers, one
  without a room and one not scheduled yet, every kind of speaker link, company and job-title answers (with spelling
  variants and a logo in the website answer), slides and logo uploads, live and recording links, unicode and very
  long text. The two files hold the same schedule with event-local and UTC times. Made by
  `scripts/make-sample-feed.py`; edit that script, not the JSON.
