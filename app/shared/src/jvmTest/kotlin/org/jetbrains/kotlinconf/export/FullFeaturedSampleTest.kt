package org.jetbrains.kotlinconf.export

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.kotlinconf.Conference
import org.jetbrains.kotlinconf.SessionId
import org.jetbrains.kotlinconf.asSessionCard
import org.jetbrains.kotlinconf.buildAgenda
import org.jetbrains.kotlinconf.isServiceEvent
import org.jetbrains.kotlinconf.sessionize.SessionizeData
import org.jetbrains.kotlinconf.sessionize.SessionizeRules
import org.jetbrains.kotlinconf.sessionize.parseSessionizeData
import org.jetbrains.kotlinconf.sessionize.toConference
import org.jetbrains.kotlinconf.sessionize.toTagInfo
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A made-up conference that uses every option a Sessionize event can have (testdata/sessionize, made by
 * scripts/make-sample-feed.py), checked against what the attendee's screens are built from.
 */
class FullFeaturedSampleTest {
    private val zone = TimeZone.of("Europe/Berlin")
    private val rules = SessionizeRules(slidesQuestion = "Presentation or Poster")

    private fun feed(file: String): SessionizeData =
        parseSessionizeData(File(System.getProperty("sessionize.testdata"), file).readText())

    private val conference: Conference = feed("full-featured-sample.json").toConference(zone, rules)

    private fun session(id: String) = conference.sessions.single { it.id == SessionId(id) }
    private fun speaker(id: String) = conference.speakers.single { it.id.id == id }

    @Test
    fun utcAndLocalTimesGiveTheSameSchedule() {
        val utc = feed("full-featured-sample-utc.json").toConference(zone, rules)

        assertEquals(
            conference.sessions.map { Triple(it.id, it.startsAt, it.endsAt) },
            utc.sessions.map { Triple(it.id, it.startsAt, it.endsAt) },
        )
        assertEquals(LocalDateTime.parse("2027-03-09T11:00"), session("t1").startsAt)
    }

    @Test
    fun aSessionWithoutATimeIsLeftOutAndOneWithoutARoomHasNoLocation() {
        assertTrue(conference.sessions.none { it.id.id == "t10" }, "not scheduled yet")
        assertEquals("", session("t9").location)
    }

    @Test
    fun roomSuffixesAreOnlyStrippedWhenAskedFor() {
        assertEquals("Lightning Stage (Lightning talks)", session("l0").location)
        val stripped = feed("full-featured-sample.json")
            .toConference(zone, rules.copy(roomSuffixesToStrip = listOf(" (Lightning talks)")))
        assertEquals("Lightning Stage", stripped.sessions.single { it.id.id == "l0" }.location)
    }

    @Test
    fun filtersComeFromTheCategoriesInSessionizeOrder() {
        val info = feed("full-featured-sample.json").toTagInfo(rules)

        assertEquals(listOf("AI & Agents", "Platform", "Security", "Community", "Open source", "Live demo"), info.categories)
        assertEquals(listOf("Beginner", "Intermediate", "Advanced"), info.levels)
        assertEquals(listOf("Keynote", "Talk", "Workshop", "Lightning talk", "Panel"), info.formats)
        assertEquals(listOf("Security", "Intermediate", "Talk", "Open source"), session("t1").tags)
    }

    @Test
    fun linksSlidesAndRecordings() {
        assertEquals("https://youtu.be/sample-t1", session("t1").videoUrl, "surrounding spaces are trimmed")
        assertNull(session("t2").videoUrl)
        assertEquals("https://live.example/main", session("k1").liveUrl)
        assertNull(session("t1").liveUrl)
        assertEquals("https://sessionize.com/download/sample-deck.pdf", session("t1").slidesUrl)
        assertNull(session("t6").slidesUrl.takeIf { it?.contains("logo") == true }, "a logo upload is never the slides")
        assertEquals("https://sessionize.com/download/sample-deck.pdf", session("t6").slidesUrl)
    }

    @Test
    fun speakersWithEveryKindOfLinkAndMissingFields() {
        val ada = speaker("sp01")
        assertEquals(listOf("LinkedIn", "Twitter", "Blog", "Company_Website", "Sessionize", "Other"), ada.links.map { it.type })
        assertEquals("Ada's blog", ada.links.single { it.type == "Blog" }.title)
        assertEquals("All my talks", ada.links.single { it.type == "Other" }.title)

        assertTrue(speaker("sp04").links.none { it.url.startsWith("mailto:") }, "only web links are kept")
        assertEquals("", speaker("sp03").photoUrl)
        assertEquals("", speaker("sp07").position)
        assertEquals("", speaker("sp07").description)
        assertTrue(speaker("sp06").description.length > 500)
        assertEquals("名前 · naïve café", speaker("sp05").position.substringBefore(", "))
    }

    @Test
    fun companiesAreConsistentAndTheirWebsitesAreRealPages() {
        // "Acme" and "ACME" are one company, spelled the same way everywhere
        assertEquals(speaker("sp01").company, speaker("sp02").company)
        // A logo in the website question is not a website, and without a company link there is none to show
        assertNull(speaker("sp02").companyUrl)
        assertEquals("https://acme.example", speaker("sp01").companyUrl)
        assertEquals("Principal engineer, Acme", speaker("sp01").position)
        assertEquals("Developer Advocate, Initech", speaker("sp03").position)
    }

    @Test
    fun coSpeakersAndSessionsWithoutSpeakers() {
        assertEquals(listOf("sp04", "sp08", "sp01"), session("t5").speakerIds.map { it.id })
        assertEquals(emptyList(), session("t3").speakerIds)
        assertNotNull(session("allday"))
    }

    @Test
    fun theMultipartWorkshopIsMergedOnlyWhenAskedFor() {
        assertEquals(2, conference.sessions.count { it.title.startsWith("Hands-on agents workshop") })
        val merged = feed("full-featured-sample.json")
            .toConference(zone, rules.copy(multipartWorkshopTag = "Workshop"))
        val workshop = merged.sessions.single { it.title == "Hands-on agents workshop" }
        assertEquals(LocalDateTime.parse("2027-03-09T14:00"), workshop.startsAt)
        assertEquals(LocalDateTime.parse("2027-03-09T18:00"), workshop.endsAt)
    }

    @Test
    fun theScheduleCoversThreeDaysAndPlacesEverySessionOnce() {
        val days = conference.buildAgenda(emptySet(), LocalDateTime.parse("2027-03-09T10:00"))
        val placed = days.flatMap { it.timeSlots }.flatMap { it.sessions }

        assertEquals(3, days.size)
        assertEquals(conference.sessions.size, placed.size)
        assertEquals(conference.sessions.map { it.id }.toSet(), placed.map { it.id }.toSet())
    }

    @Test
    fun aRealSessionizeTestEventWithUtcTimes() {
        val data = feed("sessionize-demo-event.json")
        val demo = data.toConference(TimeZone.of("UTC"))

        assertEquals(11, demo.sessions.size, "ten sessions and a lunch break")
        assertEquals(10, demo.speakers.size)
        assertEquals(setOf("Room 1", "Room 2", "Room 3"), demo.sessions.map { it.location }.toSet())
        assertEquals(LocalDateTime.parse("2026-12-30T09:00"), demo.sessions.minOf { it.startsAt })
        assertEquals(LocalDateTime.parse("2026-12-31T14:20"), demo.sessions.maxOf { it.endsAt })
        assertEquals(2, demo.buildAgenda(emptySet(), LocalDateTime.parse("2026-12-30T10:00")).size)

        // The same instants shown in another zone, as the app does for a conference elsewhere
        val berlin = data.toConference(TimeZone.of("Europe/Berlin"))
        assertEquals(LocalDateTime.parse("2026-12-30T10:00"), berlin.sessions.minOf { it.startsAt })
    }

    @Test
    fun theRealFeedCarriesCategoriesLinksAndServiceSessions() {
        val data = feed("sessionize-demo-event.json")
        val demo = data.toConference(TimeZone.of("UTC"))
        val info = data.toTagInfo()

        assertEquals(listOf("Business", "Technical", "Scientific"), info.categories.take(3))
        assertEquals(listOf("Lightning talk", "Session", "Workshop"), info.formats)
        assertEquals(listOf("Introductory and overview", "Intermediate", "Advanced", "Expert"), info.levels)
        assertEquals("https://example.com/recordings/olivia", demo.sessions.single { it.title == "Olivia's Session" }.videoUrl)
        assertEquals("https://example.com/live/olivia", demo.sessions.single { it.title == "Olivia's Session" }.liveUrl)
        assertNull(demo.sessions.single { it.title == "Liam's Session" }.liveUrl)
        val lunch = demo.sessions.single { it.title == "Lunch" }
        assertTrue(lunch.isServiceSession)
        assertTrue(lunch.speakerIds.isEmpty())
        assertTrue(demo.sessions.single { it.title == "Mia's Session" }.isPlenarySession)
        assertTrue(demo.sessions.filter { it.title != "Mia's Session" }.none { it.isPlenarySession })
    }

    @Test
    fun breaksAndKeynotesAreMarkedAndTalksAreNot() {
        assertTrue(session("sv0lun").isServiceSession)
        assertTrue(session("allday").isServiceSession)
        assertTrue(session("k1").isPlenarySession)
        assertTrue(session("k2").isPlenarySession)
        assertTrue(conference.sessions.filter { it.id.id.startsWith("t") || it.id.id.startsWith("w") }.none { it.isServiceSession || it.isPlenarySession })
    }

    @Test
    fun theCardsKnowWhichSessionsAreNotTalks() {
        val speakers = conference.speakers.associateBy { it.id }
        val cards = conference.sessions.associate { it.id.id to it.asSessionCard(speakers, LocalDateTime.parse("2027-03-09T10:00"), emptySet()) }

        assertTrue(cards.getValue("sv0lun").isServiceEvent)
        assertTrue(cards.getValue("k1").isPlenary)
        assertTrue(!cards.getValue("k1").isServiceEvent)
        // A session without speakers is a talk when it has categories and is not marked as a break
        assertTrue(!cards.getValue("t3").isServiceEvent)
        assertEquals(cards.getValue("t9").shortTimeline, cards.getValue("t9").timelineWithRoom)
        assertTrue(cards.getValue("t1").timelineWithRoom.endsWith("Room Aurora"))
    }
}
