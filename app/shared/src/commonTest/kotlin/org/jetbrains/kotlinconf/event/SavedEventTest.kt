package org.jetbrains.kotlinconf.event

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.observable.makeObservable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.jetbrains.kotlinconf.SessionId
import org.jetbrains.kotlinconf.storage.YearlyStorageImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SavedEventTest {
    @Test
    fun idIsFoundInAnySessionizeApiLink() {
        assertEquals("asqam7rn", parseSessionizeId("asqam7rn"))
        assertEquals("asqam7rn", parseSessionizeId("  asqam7rn \n"))
        assertEquals("asqam7rn", parseSessionizeId("https://sessionize.com/api/v2/asqam7rn/view/All"))
        assertEquals("asqam7rn", parseSessionizeId("https://sessionize.com/api/v2/asqam7rn/view/GridSmart"))
        assertEquals("asqam7rn", parseSessionizeId("sessionize.com/api/v2/asqam7rn"))
    }

    @Test
    fun otherInputIsNotAnId() {
        assertNull(parseSessionizeId(""))
        assertNull(parseSessionizeId("https://example.com/schedule"))
        assertNull(parseSessionizeId("not an id"))
        assertNull(parseSessionizeId("abc"))
    }

    @Test
<<<<<<< HEAD
    fun anEventsWebAppAddressIsRecognisedAsNotTheApiLink() {
        assertTrue(isSessionizeEventPage("https://agntcon-mcpcon-na-2026.sessionize.com"))
        assertTrue(isSessionizeEventPage("agntcon-mcpcon-na-2026.sessionize.com/schedule"))
        assertTrue(isSessionizeEventPage("  http://my-event.sessionize.com/?x=1 "))
        assertFalse(isSessionizeEventPage("https://sessionize.com/api/v2/asqam7rn/view/All"))
        assertFalse(isSessionizeEventPage("asqam7rn"))
        assertFalse(isSessionizeEventPage("https://example.com/schedule"))
        assertFalse(isSessionizeEventPage("https://evilsessionize.com"))
        assertNull(parseSessionizeId("https://agntcon-mcpcon-na-2026.sessionize.com"))
=======
    fun aConnectLinkCarriesTheSessionizeId() {
        assertEquals("asqam7rn", parseConnectLink("sessionable://connect?id=asqam7rn"))
        assertEquals("asqam7rn", parseConnectLink("  sessionable://connect/?id=asqam7rn  "))
        assertEquals("asqam7rn", parseConnectLink("SESSIONABLE://connect?name=x&id=asqam7rn#top"))
        assertNull(parseConnectLink("sessionable://connect?id="))
        assertNull(parseConnectLink("sessionable://connect?id=a b"))
        assertNull(parseConnectLink("sessionable://connect"))
        assertNull(parseConnectLink("sessionable://other?id=asqam7rn"))
        assertNull(parseConnectLink("https://sessionable.example/connect?id=asqam7rn"))
>>>>>>> 262c44c1 (Open Sessionable from a sessionable://connect?id=<id> link, for QR codes scanned with the camera app)
    }

    @Test
    fun savedEventBecomesAProfileWithItsOwnStorage() {
        val profile = SavedEvent("abcd1234", "My Conf", "Europe/Berlin").toProfile()

        assertEquals("My Conf", profile.name)
        assertEquals("https://sessionize.com/api/v2/abcd1234/view/All", profile.sessionizeUrl)
        assertEquals("Europe/Berlin", profile.timeZone.id)
        assertNotEquals(SavedEvent("zzzz9999", "Other", "Europe/Berlin").toProfile().storageScope, profile.storageScope)
    }

    @OptIn(ExperimentalSettingsApi::class)
    @Test
    fun clearingOneConferenceLeavesTheOthersAlone() = runTest {
        val settings = MapSettings().makeObservable()
        val a = YearlyStorageImpl("sessionize-a", settings)
        val b = YearlyStorageImpl("sessionize-b", settings)
        a.setFavorites(setOf(SessionId("1")))
        b.setFavorites(setOf(SessionId("2")))

        a.clear()

        assertEquals(emptySet(), a.getFavorites().first())
        assertEquals(setOf(SessionId("2")), b.getFavorites().first())
    }
}
