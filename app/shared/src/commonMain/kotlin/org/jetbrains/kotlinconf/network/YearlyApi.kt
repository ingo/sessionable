package org.jetbrains.kotlinconf.network

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import org.jetbrains.kotlinconf.Conference
import org.jetbrains.kotlinconf.ConferenceInfo
import org.jetbrains.kotlinconf.di.YearScope
import org.jetbrains.kotlinconf.event.EventProfile
import org.jetbrains.kotlinconf.event.toConferenceInfo
import org.jetbrains.kotlinconf.sessionize.toConference
import org.jetbrains.kotlinconf.utils.Logger
import org.jetbrains.kotlinconf.utils.tagged

/** Reads the conference's schedule and info from its public Sessionize feed. */
@Inject
@SingleIn(YearScope::class)
class YearlyApi(
    private val sessionizeFeed: SessionizeFeed,
    private val event: EventProfile,
    logger: Logger,
) {
    private val taggedLogger = logger.tagged("YearlyApi")

    suspend fun downloadConferenceData(): Conference? = safeApiCall {
        sessionizeFeed.get(event.sessionizeUrl).toConference(event.timeZone, event.sessionizeRules)
    }

    suspend fun downloadConferenceInfo(): ConferenceInfo? = safeApiCall {
        event.toConferenceInfo(sessionizeFeed.get(event.sessionizeUrl))
    }

    private suspend fun <T> safeApiCall(call: suspend () -> T): T? {
        return try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            taggedLogger.log { "Could not read the schedule: ${e.message}" }
            null
        }
    }
}
