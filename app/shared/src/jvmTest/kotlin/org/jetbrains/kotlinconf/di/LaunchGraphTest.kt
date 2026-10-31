package org.jetbrains.kotlinconf.di

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.observable.makeObservable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import org.jetbrains.kotlinconf.EmptyLocalNotificationService
import org.jetbrains.kotlinconf.LocalNotificationService
import org.jetbrains.kotlinconf.event.EventSelection
import org.jetbrains.kotlinconf.event.SavedEvent
import org.jetbrains.kotlinconf.flags.Flags
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DependencyGraph(AppScope::class, excludes = [JvmBindings::class])
interface LaunchTestGraph : AppGraph {
    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides platformFlags: Flags = Flags()): LaunchTestGraph
    }

    @Provides @SingleIn(AppScope::class) fun settings(): ObservableSettings = LaunchTestStorage.settings
    @Provides @FileStorageDir fun fileStorageDir(): String = "build/test-files"
    @Provides @SingleIn(AppScope::class) fun notifications(): LocalNotificationService = EmptyLocalNotificationService()
}

/** Stands in for the device's storage, so a second graph sees what the first one saved, as after a restart. */
object LaunchTestStorage {
    var settings: ObservableSettings = MapSettings().makeObservable()
}

/** The real dependency graph, as the app builds it at launch. */
class LaunchGraphTest {
    @Test
    fun theConferenceThatWasOpenIsRestoredAfterARestart() = runBlocking {
        LaunchTestStorage.settings = MapSettings().makeObservable()
        val event = SavedEvent("abc123", "Test Conf", "Europe/Berlin")
        createGraphFactory<LaunchTestGraph.Factory>().create().conferenceService.connectEvent(event)

        EventSelection.current = null
        val restarted = createGraphFactory<LaunchTestGraph.Factory>().create().conferenceService

        assertEquals("abc123", restarted.activeEvent.value?.sessionizeId)
        assertEquals("abc123", EventSelection.current?.sessionizeId)
        assertEquals(listOf(event), restarted.savedEvents.value)
    }

    @Test
    fun leavingAConferenceClearsTheSelectionAndStaysOnTheConnectScreenAfterARestart() = runBlocking {
        LaunchTestStorage.settings = MapSettings().makeObservable()
        val service = createGraphFactory<LaunchTestGraph.Factory>().create().conferenceService
        service.connectEvent(SavedEvent("abc123", "Test Conf", "Europe/Berlin"))
        service.leaveEvent()

        assertNull(EventSelection.current)
        EventSelection.current = null
        val restarted = createGraphFactory<LaunchTestGraph.Factory>().create().conferenceService
        assertNull(restarted.activeEvent.value)
    }

    @Test
    fun theScheduleApiForAConferenceCanBeBuiltWithoutAGlobalSelection() {
        EventSelection.current = null
        val graph = createGraphFactory<LaunchTestGraph.Factory>().create()
        val factory = graph as YearGraph.Factory
        val profile = SavedEvent("abc123", "Test Conf", "Europe/Berlin").toProfile()

        assertNotNull(factory.create(profile.storageScope, profile).api)
    }
}
