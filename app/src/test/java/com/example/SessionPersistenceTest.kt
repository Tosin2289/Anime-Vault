package com.example

import com.example.data.local.AppSessionEntity
import com.example.data.local.SessionHistoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SessionPersistenceTest {

    @Test
    fun testAppSessionEntityMapping() {
        val now = System.currentTimeMillis()
        val sessionEntity = AppSessionEntity(
            id = "active_session",
            userId = "user_test",
            userName = "Otaku Master",
            userEmail = "ayoolatosin00@gmail.com",
            lastActiveDestination = "DISCOVER",
            lastActiveDayFilter = "Friday",
            lastViewedAnimeId = 5114,
            lastViewedAnimeTitle = "Fullmetal Alchemist: Brotherhood",
            sessionStartTime = now - 60_000L,
            lastActiveTime = now,
            sessionCount = 3,
            episodesWatchedThisSession = 4
        )

        val profile = sessionEntity.toUserProfile()
        assertEquals("user_test", profile.id)
        assertEquals("Otaku Master", profile.name)
        assertEquals("ayoolatosin00@gmail.com", profile.email)

        val sessionInfo = sessionEntity.toSessionInfo()
        assertEquals(3, sessionInfo.sessionNumber)
        assertEquals("DISCOVER", sessionInfo.lastDestination)
        assertEquals("Friday", sessionInfo.lastDayFilter)
        assertEquals(5114, sessionInfo.lastViewedAnimeId)
        assertEquals("Fullmetal Alchemist: Brotherhood", sessionInfo.lastViewedAnimeTitle)
        assertEquals(4, sessionInfo.episodesWatchedThisSession)
    }

    @Test
    fun testSessionHistoryEntityMapping() {
        val start = System.currentTimeMillis() - 3600_000L
        val end = System.currentTimeMillis()
        val historyEntity = SessionHistoryEntity(
            sessionId = 1L,
            sessionNumber = 2,
            startTime = start,
            endTime = end,
            durationMinutes = 60L,
            lastDestination = "CALENDAR",
            episodesWatchedCount = 5,
            summary = "Session #2 ended on CALENDAR."
        )

        val logItem = historyEntity.toSessionLogItem()
        assertEquals(1L, logItem.id)
        assertEquals(2, logItem.sessionNumber)
        assertEquals(60L, logItem.durationMinutes)
        assertEquals("CALENDAR", logItem.lastDestination)
        assertEquals(5, logItem.episodesWatchedCount)
        assertNotNull(logItem.summary)
    }
}
