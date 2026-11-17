package com.example.courtup

import org.junit.Assert.assertEquals
import org.junit.Test

class RoomStateTest {
    private val room = Room(1, "Cricket", "Noida", "6:00 PM", 2, 2, "Host",
        listOf("Host", "Member"), listOf("Pending"))
    @Test fun hostAndMembershipTakePriorityOverFull() {
        assertEquals("host", room.state("Host"))
        assertEquals("member", room.state("Member"))
        assertEquals("pending", room.state("Pending"))
        assertEquals("full", room.state("Visitor"))
    }
    @Test fun visitorCanRequestWhenThereIsSpace() {
        assertEquals("join", room.copy(maxPlayers = 3).state("Visitor"))
    }
}
