package org.jetbrains.kotlinconf.event

import kotlinx.serialization.Serializable

/** A conference the attendee connected to by its Sessionize link, kept on the device. */
@Serializable
data class SavedEvent(
    /** The id in `https://sessionize.com/api/v2/{id}/view/All`. */
    val sessionizeId: String,
    val name: String,
    val timeZoneId: String,
) {
    fun toProfile(): EventProfile = EventProfile(
        id = "$SCOPE_PREFIX$sessionizeId",
        name = name,
        storageScope = "$SCOPE_PREFIX$sessionizeId",
        timeZoneId = timeZoneId,
        sessionizeId = sessionizeId,
    )
}

private const val SCOPE_PREFIX = "sessionize-"

/** What a conference feed holds, shown before the attendee connects to it. */
data class FeedSummary(val sessions: Int, val speakers: Int)

private val idPattern = Regex("[A-Za-z0-9]{4,32}")
private val apiPath = Regex("sessionize\\.com/api/v2/([A-Za-z0-9]+)", RegexOption.IGNORE_CASE)

/**
 * The Sessionize API id in whatever the attendee pasted: the id itself, or any of the event's API links
 * (`https://sessionize.com/api/v2/{id}/view/All`, `.../view/GridSmart`, ...). Null when there is none.
 */
fun parseSessionizeId(input: String): String? {
    val text = input.trim()
    apiPath.find(text)?.let { return it.groupValues[1] }
    return text.takeIf { idPattern.matches(it) }
}
