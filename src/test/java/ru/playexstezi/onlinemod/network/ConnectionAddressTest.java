package ru.playexstezi.onlinemod.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionAddressTest {
    @Test
    void parsesHostnameAndPort() {
        ConnectionAddress address = ConnectionAddress.parse("play.example.net:25565").orElseThrow();
        assertEquals("play.example.net", address.host());
        assertEquals(25565, address.port());
        assertEquals("play.example.net:25565", address.format());
    }

    @Test
    void parsesIpv4AndPort() {
        ConnectionAddress address = ConnectionAddress.parse("192.0.2.10:25565").orElseThrow();
        assertEquals("192.0.2.10:25565", address.format());
    }

    @Test
    void parsesBracketedIpv6AndPort() {
        ConnectionAddress address = ConnectionAddress.parse("[2001:db8::1]:25565").orElseThrow();
        assertEquals("2001:db8::1", address.host());
        assertEquals("[2001:db8::1]:25565", address.format());
    }

    @Test
    void rejectsMissingOrInvalidPort() {
        assertTrue(ConnectionAddress.parse("example.net").isEmpty());
        assertTrue(ConnectionAddress.parse("example.net:0").isEmpty());
        assertTrue(ConnectionAddress.parse("example.net:65536").isEmpty());
        assertTrue(ConnectionAddress.parse("example.net:abc").isEmpty());
    }

    @Test
    void rejectsAmbiguousUnbracketedIpv6() {
        assertTrue(ConnectionAddress.parse("2001:db8::1:25565").isEmpty());
        assertTrue(ConnectionAddress.parse("[2001:db8::1]25565").isEmpty());
    }

    @Test
    void rejectsEmptyAndNullInput() {
        assertTrue(ConnectionAddress.parse(null).isEmpty());
        assertTrue(ConnectionAddress.parse(" ").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new ConnectionAddress("", 25565));
        assertThrows(IllegalArgumentException.class, () -> new ConnectionAddress("example.net", 0));
    }
}
