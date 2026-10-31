package org.jetbrains.kotlinconf.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.kotlinconf.ConferenceService

@ContributesIntoMap(AppScope::class)
@ViewModelKey
class InfoViewModel(
    private val service: ConferenceService,
) : ViewModel() {
    /** Back to the connect screen of the generic build. */
    fun switchConference() {
        viewModelScope.launch { service.leaveEvent() }
    }

    val venueAddress: StateFlow<String?> = service.conferenceInfo
        .map { it?.mapData?.venueAddress }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
