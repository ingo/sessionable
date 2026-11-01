package org.jetbrains.kotlinconf.event

private val connectLink = Regex("^sessionable://connect/?\\?(.*)$", RegexOption.IGNORE_CASE)

/**
 * The conference id in a `sessionable://connect?id=<id>` link, the kind an organiser puts in a QR code: the phone's
 * camera app offers to open it in Sessionable. Null for any other link.
 */
fun parseConnectLink(link: String): String? {
    val query = connectLink.find(link.trim())?.groupValues?.get(1) ?: return null
    val id = query.substringBefore('#').split('&')
        .firstNotNullOfOrNull { it.takeIf { p -> p.startsWith("id=", ignoreCase = true) }?.substring(3) }
        ?: return null
    return id.takeIf { Regex("[A-Za-z0-9]{4,32}").matches(it) }
}
