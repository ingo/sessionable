package org.jetbrains.kotlinconf.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.kotlinconf.ConferenceService
import org.jetbrains.kotlinconf.event.FeedSummary
import org.jetbrains.kotlinconf.event.SavedEvent
import org.jetbrains.kotlinconf.event.parseSessionizeId

sealed interface ConnectUiState {
    data object Idle : ConnectUiState
    data object Checking : ConnectUiState
    data object NotALink : ConnectUiState
    data object NotFound : ConnectUiState
    data class Found(val sessionizeId: String, val summary: FeedSummary) : ConnectUiState
}

/** Connects the generic build to a conference by its Sessionize link. */
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class ConnectViewModel(
    private val service: ConferenceService,
) : ViewModel() {
    val savedEvents: StateFlow<List<SavedEvent>> = service.savedEvents

    val uiState: StateFlow<ConnectUiState>
        field = MutableStateFlow<ConnectUiState>(ConnectUiState.Idle)

    val pendingConnectId: StateFlow<String?> = service.pendingConnectId

    fun consumeConnectId() = service.consumeConnectId()

    private var job: Job? = null

    /** Reads the feed behind [input] to make sure it is a conference with a schedule. */
    fun check(input: String) {
        job?.cancel()
        val id = parseSessionizeId(input)
        if (id == null) {
            uiState.value = ConnectUiState.NotALink
            return
        }
        uiState.value = ConnectUiState.Checking
        job = viewModelScope.launch {
            val summary = service.inspectFeed(id)
            uiState.value = if (summary == null) ConnectUiState.NotFound else ConnectUiState.Found(id, summary)
        }
    }

    /** Forgets a result when the link is edited. */
    fun reset() {
        job?.cancel()
        uiState.value = ConnectUiState.Idle
    }

    fun connect(sessionizeId: String, name: String, timeZoneId: String) {
        viewModelScope.launch { service.connectEvent(SavedEvent(sessionizeId, name.trim(), timeZoneId)) }
    }

    fun open(sessionizeId: String) {
        viewModelScope.launch { service.switchEvent(sessionizeId) }
    }

    fun forget(sessionizeId: String) {
        viewModelScope.launch { service.forgetEvent(sessionizeId) }
    }
}
