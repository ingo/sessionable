package org.jetbrains.kotlinconf.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionizeFeedTest {
    private val requested = mutableListOf<String>()

    private val feed = SessionizeFeed(
        HttpClient(MockEngine { request ->
            requested += request.url.toString()
            val sessions = if ("one" in request.url.toString()) 1 else 3
            val body = """{"sessions": [${List(sessions) { """{"id": "s$it", "title": "T$it"}""" }.joinToString()}]}"""
            respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        })
    )

    @Test
    fun twoConferencesInARowGetTheirOwnFeed() = runTest {
        assertEquals(1, feed.get("https://sessionize.com/api/v2/one/view/All").sessions.size)
        assertEquals(3, feed.get("https://sessionize.com/api/v2/two/view/All").sessions.size)
    }

    @Test
    fun theSameFeedIsDownloadedOnceWithinAMinute() = runTest {
        feed.get("https://sessionize.com/api/v2/one/view/All")
        feed.get("https://sessionize.com/api/v2/one/view/All")
        feed.get("https://sessionize.com/api/v2/two/view/All")
        feed.get("https://sessionize.com/api/v2/one/view/All")

        assertEquals(2, requested.size)
    }
}
