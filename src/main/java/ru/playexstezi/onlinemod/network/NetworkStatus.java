package ru.playexstezi.onlinemod.network;

/**
 * Human-readable result categories for the host setup screen.
 * A public IP alone is not proof that an incoming connection can reach it.
 */
public enum NetworkStatus {
    NOT_CHECKED,
    PUBLIC_ADDRESS_FOUND,
    PUBLIC_ADDRESS_UNAVAILABLE,
    PORT_MAPPING_NOT_CONFIGURED,
    RELAY_REQUIRED
}
