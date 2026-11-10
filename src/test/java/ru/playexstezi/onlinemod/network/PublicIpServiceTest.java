package ru.playexstezi.onlinemod.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PublicIpServiceTest {
    @Test
    void acceptsValidIpv4AndNormalizesOctets() {
        assertEquals("203.0.113.7", PublicIpService.validateIpv4("203.0.113.7").orElseThrow());
        assertEquals("8.8.8.8", PublicIpService.validateIpv4("008.008.008.008").orElseThrow());
    }

    @Test
    void rejectsInvalidIpv4Responses() {
        assertTrue(PublicIpService.validateIpv4("").isEmpty());
        assertTrue(PublicIpService.validateIpv4("not-an-ip").isEmpty());
        assertTrue(PublicIpService.validateIpv4("256.1.1.1").isEmpty());
        assertTrue(PublicIpService.validateIpv4("1.2.3").isEmpty());
        assertTrue(PublicIpService.validateIpv4("1.2.3.4.5").isEmpty());
        assertTrue(PublicIpService.validateIpv4("1.2.-3.4").isEmpty());
        assertTrue(PublicIpService.validateIpv4("1.2.3.999999999999").isEmpty());
    }

    @Test
    void trimsOnlyAtProviderBoundary() {
        assertTrue(PublicIpService.validateIpv4(" 203.0.113.7 ").isEmpty());
    }
}
