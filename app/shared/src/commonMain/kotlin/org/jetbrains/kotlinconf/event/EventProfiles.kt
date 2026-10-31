package org.jetbrains.kotlinconf.event

import androidx.compose.runtime.mutableStateOf
import org.jetbrains.kotlinconf.sessionize.SessionizeRules

/**
 * The conferences this app can be built for.
 *
 * Without `-Pevent=<id>` (or `ORG_GRADLE_PROJECT_event=<id>` for Xcode builds) the app is the generic
 * build: the attendee pastes a Sessionize link and connects to any conference. With it, the build is
 * branded for that conference and has no connect screen. To brand a build for your own conference,
 * add a profile here with its Sessionize API id; see docs/FORKING.md. On an event's public schedule
 * page the id appears in embed URLs such as `https://sessionize.com/api/v2/{id}/view/GridSmart`; the
 * organiser can also enable the API.
 */
object EventProfiles {
    val AgntconMcpconNa2026 = EventProfile(
        id = "agntcon-mcpcon-na-2026",
        name = "AGNTCon + MCPCon North America 2026",
        storageScope = "2026",
        timeZoneId = "America/Los_Angeles",
        sessionizeId = "asqam7rn",
        sessionizeAppUrl = "https://agntcon-mcpcon-na-2026.sessionize.com",
        // Speakers upload slides (PDF or PowerPoint) or a poster image here
        sessionizeRules = SessionizeRules(slidesQuestion = "Presentation or Poster"),
        aboutDescription = "October 22–23, 2026\nSan Jose McEnery Convention Center, San Jose, CA\n\n" +
            "The AI agents conference: agentic AI infrastructure, production agent systems, and MCP.",
        websiteUrl = "https://events.linuxfoundation.org/agntcon-mcpcon-north-america/",
        codeOfConductUrl = "https://events.linuxfoundation.org/agntcon-mcpcon-north-america/attend/code-of-conduct/",
        venueAddress = "San Jose McEnery Convention Center, 150 W San Carlos St, San Jose, CA 95113",
        venueMap = SanJoseMcEneryConventionCenter,
        showcaseRoom = "Solutions Showcase",
        theme = AgntconTheme.theme,
    )

    val all: List<EventProfile> = listOf(AgntconMcpconNa2026)

    /**
     * Real public schedules, offered as "try an example" on the connect screen of the generic build. Their
     * organisers have enabled the Sessionize API; if one is switched off, remove it here.
     */
    val examples: List<SavedEvent> = listOf(
        SavedEvent("asqam7rn", "AGNTCon + MCPCon North America 2026", "America/Los_Angeles"),
        SavedEvent("1diujeu9", "Trondheim Developer Conference 2026", "Europe/Oslo"),
        SavedEvent("2r0suvtg", "KotlinConf 2026", "Europe/Berlin"),
    )

    fun byId(id: String): EventProfile =
        all.find { it.id == id } ?: error("Unknown event '$id'. Known events: ${all.joinToString { it.id }}")
}

/** The conference this build is branded for; null in the generic build, where the attendee picks one. */
val FixedEvent: EventProfile? = ACTIVE_EVENT_ID.takeIf { it.isNotBlank() }?.let(EventProfiles::byId)

/**
 * The conference the app is showing now. Backed by Compose state, so screens that read it update when
 * the attendee switches conference. Null only on the connect screen of the generic build.
 */
object EventSelection {
    private val state = mutableStateOf(FixedEvent)

    var current: EventProfile?
        get() = state.value
        set(value) {
            state.value = value
        }
}

/** The conference being shown. Only read from screens that appear once a conference is selected. */
val ActiveEvent: EventProfile
    get() = EventSelection.current ?: error("No conference is selected")
