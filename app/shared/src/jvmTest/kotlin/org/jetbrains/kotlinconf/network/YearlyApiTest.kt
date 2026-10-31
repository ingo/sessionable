package org.jetbrains.kotlinconf.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.jetbrains.kotlinconf.event.EventSelection
import org.jetbrains.kotlinconf.event.SavedEvent
import org.jetbrains.kotlinconf.utils.NoopProdLogger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class YearlyApiTest {
    private val requested = mutableListOf<String>()

    private val feed = SessionizeFeed(
        HttpClient(MockEngine { request ->
            requested += request.url.toString()
            respond(
                """{"sessions": [{"id": "s1", "title": "T"}]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        })
    )

    /** Regression: the api used to read the global selection, which crashed the app at launch. */
    @Test
    fun readsTheEventItWasCreatedForWithoutAGlobalSelection() = runTest {
        EventSelection.current = null
        val api = YearlyApi(feed, SavedEvent("abc123", "Test Conf", "Europe/Berlin").toProfile(), NoopProdLogger())

        assertNotNull(api.downloadConferenceData())
        assertEquals(listOf("https://sessionize.com/api/v2/abc123/view/All"), requested)
    }
}
