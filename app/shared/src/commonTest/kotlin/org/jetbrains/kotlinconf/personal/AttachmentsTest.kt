package org.jetbrains.kotlinconf.personal

import org.jetbrains.kotlinconf.SessionId
import kotlin.test.Test
import kotlin.test.assertEquals

class AttachmentsTest {
    private fun photo(id: String, at: Long) = Attachment(id, MediaKind.PHOTO, "$id.jpg", addedAt = at)

    @Test
    fun addedAttachmentsStayInTheOrderTheyWereAdded() {
        val s1 = SessionId("s1")
        val map = emptyMap<String, List<Attachment>>()
            .withAdded(s1, listOf(photo("b", 2), photo("a", 1)))
            .withAdded(s1, listOf(photo("c", 3)))

        assertEquals(listOf("a", "b", "c"), map.getValue("s1").map { it.id })
    }

    @Test
    fun addingTheSameAttachmentTwiceKeepsOne() {
        val s1 = SessionId("s1")
        val map = emptyMap<String, List<Attachment>>().withAdded(s1, listOf(photo("a", 1))).withAdded(s1, listOf(photo("a", 1)))

        assertEquals(1, map.getValue("s1").size)
    }

    @Test
    fun removingTheLastAttachmentRemovesTheSession() {
        val s1 = SessionId("s1")
        val map = emptyMap<String, List<Attachment>>().withAdded(s1, listOf(photo("a", 1)))

        assertEquals(emptyMap(), map.withRemoved(s1, "a"))
    }

    @Test
    fun aVideoShowsItsStillAndKeepsBothFiles() {
        val video = Attachment("v", MediaKind.VIDEO, "v.mp4", thumbnail = "v.jpg")

        assertEquals("v.jpg", video.preview)
        assertEquals(listOf("v.mp4", "v.jpg"), video.files)
    }
}
