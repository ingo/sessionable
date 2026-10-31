package org.jetbrains.kotlinconf.storage

import kotlinx.coroutines.flow.Flow
import org.jetbrains.kotlinconf.event.SavedEvent
import org.jetbrains.kotlinconf.flags.Flags
import org.jetbrains.kotlinconf.Theme

interface ApplicationStorage {
    fun isOnboardingComplete(): Flow<Boolean>
    suspend fun setOnboardingComplete(value: Boolean)

    fun getTheme(): Flow<Theme>
    suspend fun setTheme(value: Theme)

    fun getFlagsBlocking(): Flags?
    fun getFlags(): Flow<Flags?>
    suspend fun setFlags(value: Flags)

    /** The conferences the attendee connected to in the generic build, and the one that was open. */
    fun getSavedEvents(): Flow<List<SavedEvent>>
    fun getSavedEventsBlocking(): List<SavedEvent>
    suspend fun setSavedEvents(value: List<SavedEvent>)
    fun getActiveEventIdBlocking(): String?
    suspend fun setActiveEventId(sessionizeId: String?)

    fun initialize()
}
