#!/usr/bin/env python3
"""Writes testdata/sessionize/full-featured-sample.json (event-local times) and ...-utc.json (UTC times):
a made-up conference in the exact shape of Sessionize's API v2 "All" view, using every option an event can have
(see testdata/sessionize/README.md). The app's tests read them; edit this script, not the JSON.

    python3 scripts/make-sample-feed.py
"""
import json, pathlib
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

ZONE = ZoneInfo("Europe/Berlin")
OUT = pathlib.Path(__file__).resolve().parent.parent / "testdata" / "sessionize"
DAYS = ["2027-03-09", "2027-03-10", "2027-03-11"]

rooms = [
    {"id": 1, "name": "Main Hall", "sort": 0}, {"id": 2, "name": "Room Aurora", "sort": 1},
    {"id": 3, "name": "Room Borealis", "sort": 2}, {"id": 4, "name": "Workshop Studio", "sort": 3},
    {"id": 5, "name": "Lightning Stage (Lightning talks)", "sort": 4}, {"id": 6, "name": "Solutions Showcase", "sort": 5},
]
categories = [
    {"id": 10, "title": "Track", "sort": 0, "items": [
        {"id": 101, "name": "AI & Agents", "sort": 0}, {"id": 102, "name": "Platform", "sort": 1},
        {"id": 103, "name": "Security", "sort": 2}, {"id": 104, "name": "Community", "sort": 3}]},
    {"id": 11, "title": "Audience Level", "sort": 1, "items": [
        {"id": 111, "name": "Beginner", "sort": 0}, {"id": 112, "name": "Intermediate", "sort": 1}, {"id": 113, "name": "Advanced", "sort": 2}]},
    {"id": 12, "title": "Session format", "sort": 2, "items": [
        {"id": 121, "name": "Keynote", "sort": 0}, {"id": 122, "name": "Talk", "sort": 1}, {"id": 123, "name": "Workshop", "sort": 2},
        {"id": 124, "name": "Lightning talk", "sort": 3}, {"id": 125, "name": "Panel", "sort": 4}]},
    {"id": 13, "title": "Tags", "sort": 3, "items": [{"id": 131, "name": "Open source", "sort": 0}, {"id": 132, "name": "Live demo", "sort": 1}]},
]
questions = [
    {"id": 1, "question": "Company", "questionType": "Short_Text", "sort": 0},
    {"id": 2, "question": "Speaker Title", "questionType": "Short_Text", "sort": 1},
    {"id": 3, "question": "Company URL", "questionType": "Short_Text", "sort": 2},
    {"id": 4, "question": "Presentation or Poster", "questionType": "File_upload", "sort": 3},
    {"id": 5, "question": "Event Logo", "questionType": "File_upload", "sort": 4},
    {"id": 6, "question": "Anything the organisers should know?", "questionType": "Long_Text", "sort": 5},
]

def link(kind, url, title=None):
    return {"title": title or kind.replace("_", " "), "url": url, "linkType": kind}

def speaker(i, first, last, tag=None, bio="", pic=True, top=False, links=(), company=None, title=None, company_url=None):
    answers = []
    if company: answers.append({"questionId": 1, "answerValue": company})
    if title: answers.append({"questionId": 2, "answerValue": title})
    if company_url: answers.append({"questionId": 3, "answerValue": company_url})
    return {"id": f"sp{i:02d}", "firstName": first, "lastName": last, "fullName": f"{first} {last}", "tagLine": tag, "bio": bio,
            "profilePicture": f"https://sessionize.com/image/sample-{i}.jpg" if pic else None, "isTopSpeaker": top,
            "links": list(links), "categoryItems": [], "questionAnswers": answers}

speakers = [
    speaker(1, "Ada", "Nguyen", "Principal engineer", "Builds agent platforms.\n\nLikes *clear* interfaces.", top=True,
            links=[link("LinkedIn", "https://linkedin.com/in/ada-sample"), link("Twitter", "https://x.com/ada"), link("Blog", "https://ada.example/blog", "Ada's blog"),
                   link("Company_Website", "https://acme.example"), link("Sessionize", "https://sessionize.com/ada-sample"), link("Other", "https://ada.example/talks", "All my talks")],
            company="Acme", title="Principal Engineer", company_url="https://acme.example"),
    speaker(2, "Björn", "Åkesson", "Security lead at ACME", "Threat models for the unthreatened.", links=[link("LinkedIn", "https://linkedin.com/in/bjorn-sample")],
            company="ACME", title=None, company_url="https://acme.example/logo.png"),
    speaker(3, "Chiamaka", "Okafor", None, "", pic=False, company="Initech", title="Developer Advocate"),
    speaker(4, "Dmitri", "Volkov", "Maintainer", "Open source since 2009.", links=[link("Blog", "https://dmitri.example"), link("Contact", "mailto:dmitri@example.com", "Email")],
            company="Initech", title="Staff Engineer"),
    speaker(5, "Emoji 🚀 Speaker", "Ünïcode", "名前 · naïve café", "日本語の自己紹介。Привет, мир!", company="Globex", title="CTO"),
    speaker(6, "Fatima", "Al-Sayed", "Keynote speaker", "A very long biography. " * 40, top=True, company="Globex", title="Chief Scientist", links=[link("LinkedIn", "https://linkedin.com/in/fatima-sample")]),
    speaker(7, "Grace", "Hopper-Smith", None, None, pic=False),
    speaker(8, "Hiro", "Tanaka", "Panelist", "", company="Hooli", title="VP Engineering"),
]

SLIDES = "https://sessionize.com/download/sample-deck.pdf"
sessions_spec = []   # (id, title, desc, speakers, room, day, start, end, cats, extra)

def add(i, title, desc=None, spk=(), room=1, day=0, start="09:00", end="09:45", cats=(), **extra):
    sessions_spec.append((i, title, desc, list(spk), room, day, start, end, list(cats), extra))

for d in range(3):
    add(f"sv{d}reg", "Registration and badge pickup", None, (), 1, d, "08:00", "09:00", isServiceSession=True)
    add(f"sv{d}cof1", "Coffee break", None, (), 6, d, "10:30", "11:00", isServiceSession=True)
    add(f"sv{d}lun", "Lunch", "Lunch is served in the exhibition area.", (), 6, d, "12:30", "13:30", isServiceSession=True)
add("sv2party", "Closing party", "Food, drinks and music.", (), 1, 2, "18:00", "22:00", isServiceSession=True)
add("allday", "Exhibition open", "The Solutions Showcase is open all day.", (), 6, 0, "08:00", "18:00", isServiceSession=True)

add("k1", "Opening keynote: Agents everywhere", "Why agents changed how we build.", (6,), 1, 0, "09:00", "09:45", (101, 121), isPlenumSession=True,
    liveUrl="https://live.example/main", recordingUrl="https://youtu.be/sample-k1")
add("t1", "Securing multi-server MCP access", "Production lessons.\n\n- Auth\n- Rate limits\n- Audit", (1, 2), 2, 0, "11:00", "11:45", (103, 112, 122, 131),
    recordingUrl="  https://youtu.be/sample-t1  ", slides=SLIDES)
add("t2", "A very long session title that goes on and on to check how cards, headers and the calendar wrap text across several lines on a small phone screen", "Short.", (3,), 3, 0, "11:00", "11:45", (102, 111, 122))
add("t3", "Talks without a speaker", "A session without any speaker, such as a sponsored slot.", (), 6, 0, "14:00", "14:30", (104, 122))
add("t4", "Emoji 🚀 and ünïcödé in a title", "日本語の説明。", (5,), 2, 0, "14:00", "14:45", (101, 113, 122, 132))
add("t5", "Open source maintainers panel", None, (4, 8, 1), 1, 0, "15:00", "16:00", (104, 125, 131))
add("w1", "Hands-on agents workshop. Part 1", "Bring a laptop.", (1,), 4, 0, "14:00", "16:00", (101, 112, 123))
add("w2", "Hands-on agents workshop. Part 2", "Bring a laptop.", (1,), 4, 0, "16:30", "18:00", (101, 112, 123))
for n in range(4):
    s = 16 * 60 + n * 10
    add(f"l{n}", f"Lightning talk {n + 1}", "Five minutes.", ((2, 3, 4, 7)[n],), 5, 0, f"{s // 60:02d}:{s % 60:02d}", f"{(s + 8) // 60:02d}:{(s + 8) % 60:02d}", (104, 124))
add("t6", "Day two: platform internals", "Deep dive.", (1,), 2, 1, "09:30", "10:30", (102, 113, 122), slides=SLIDES, logo="https://sessionize.com/download/logo.pdf")
add("t7", "Overlapping talk in Borealis", "Runs at the same time as the Aurora talk.", (4,), 3, 1, "09:45", "10:45", (103, 112, 122))
add("t8", "Panel: the future of work", "Who knows.", (8, 6), 1, 1, "14:00", "15:00", (104, 125))
add("k2", "Closing keynote", "What comes next.", (6,), 1, 2, "16:30", "17:30", (101, 121), isPlenumSession=True, recordingUrl="https://youtu.be/sample-k2")
add("t9", "A talk in a room nobody defined", "The room id is missing.", (7,), None, 2, "10:00", "10:45", (102, 122))
add("t10", "Not scheduled yet", "Accepted, but no time and room yet.", (3,), None, None, None, None, (101, 122))

def stamp(day, hhmm, utc):
    local = datetime.fromisoformat(f"{DAYS[day]}T{hhmm}:00").replace(tzinfo=ZONE)
    return local.astimezone(ZoneInfo("UTC")).strftime("%Y-%m-%dT%H:%M:%SZ") if utc else local.strftime("%Y-%m-%dT%H:%M:%S")

def build(utc):
    out = []
    for i, title, desc, spk, room, day, start, end, cats, extra in sessions_spec:
        answers = []
        if extra.get("slides"): answers.append({"questionId": 4, "answerValue": extra["slides"]})
        if extra.get("logo"): answers.append({"questionId": 5, "answerValue": extra["logo"]})
        out.append({"id": i, "title": title, "description": desc, "startsAt": stamp(day, start, utc) if start else None,
                    "endsAt": stamp(day, end, utc) if end else None, "isServiceSession": extra.get("isServiceSession", False),
                    "isPlenumSession": extra.get("isPlenumSession", False), "speakers": [f"sp{n:02d}" for n in spk],
                    "categoryItems": cats, "questionAnswers": answers, "roomId": room, "liveUrl": extra.get("liveUrl"),
                    "recordingUrl": extra.get("recordingUrl")})
    return {"sessions": out, "rooms": rooms, "speakers": speakers, "questions": questions, "categories": categories}

for utc, name in ((False, "full-featured-sample.json"), (True, "full-featured-sample-utc.json")):
    (OUT / name).write_text(json.dumps(build(utc), ensure_ascii=False, indent=1) + "\n")
    print("wrote", name)
