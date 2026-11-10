# Online Mod

A client-side host/join experience for **Minecraft Java 1.20.1 + Minecraft Forge 47.x**. The goal is to let a player open a single-player world and invite friends without requiring every player to use a third-party LAN VPN.

> **Current status: early development.** The in-game host and join screens, public IPv4 lookup, LAN publication, and address validation are implemented. Direct connections over the public internet are **not yet automated or verified**. Do not treat the displayed public IP as a ready-to-use invitation address.

## Features currently in the code

- **Host screen** from the pause menu, with start/stop controls.
- **Join screen** from the multiplayer menu and host screen.
- Public IPv4 discovery through HTTPS providers, with a fallback provider.
- Copy public IPv4 to clipboard.
- Vanilla integrated-server LAN publication on an automatically selected port.
- Stop incoming connections without intentionally shutting down the single-player world.
- Strict `host:port` parsing for hostnames, IPv4 and bracketed IPv6.
- English and Russian interface translations.
- JUnit tests and GitHub Actions build.

## How to try the current build

1. Install the mod on Minecraft **1.20.1 Forge** with Java 17.
2. Open a single-player world.
3. Press Escape and choose **Online Mod**.
4. Choose **Open world for players** (or **Start hosting** in English).
5. The screen shows the local listener port and your detected public IPv4 separately.
6. To test on the same local network, another player can enter your host PC's local network address and the shown port in **Join via Online Mod**.
7. Use **Close connection** to stop accepting incoming players. The world remains open.

The host's public IPv4 and the local listener port are **not a verified public endpoint**. Friends outside the local network will not be able to join unless the router and operating-system firewall allow inbound traffic. The current release does not configure either automatically.

## Important networking limitations

A public IP lookup is not a port test. For direct internet hosting, all of the following need to work:

- the integrated server is listening on the chosen TCP port;
- the host firewall allows inbound Minecraft traffic;
- the router forwards that port to the host PC;
- the ISP permits inbound connections and does not block the subscriber behind restrictive CGNAT.

UPnP/NAT-PMP support is not implemented yet. If it is added, it must be optional, clearly describe the router change, request only the required mapping, and remove the mapping when hosting stops. A relay fallback would require a separately operated, publicly reachable server; GitHub is not a game relay.

## Compatibility

The project targets Forge 1.20.1. Large modpacks (including packs with 100–200 mods, Create, and Biomes O' Plenty) have **not yet been compatibility-tested**. Both host and joining players will need compatible Minecraft, Forge, mod versions, and relevant configuration. Do not assume different modpacks can connect to one another.

## Commands

- `/onlinemod ip` — discover the public IPv4 address. This does not test inbound connectivity.
- `/onlinemod host` — publish the current single-player world on a LAN listener.
- `/onlinemod stop` — close incoming connections while leaving the world running.

In this first implementation, hosting cannot be restarted in the same world after stopping; close and reopen the world first.

## Build

Requirements: Java 17 and Gradle (the GitHub Actions workflow uses Gradle 8.8).

```bash
gradle build
```

The mod JAR is generated in `build/libs/`.

## Development roadmap

1. Stabilize the current host/join lifecycle and verify it in a real Minecraft client.
2. Add a reliable external reachability check (not just public-IP discovery).
3. Add opt-in router port mapping with clear consent, cleanup, and failure handling.
4. Improve diagnostics for firewall, NAT, and CGNAT cases.
5. Evaluate a relay transport only when a real relay server and operating budget are available.
6. Test common Forge modpacks and document the exact tested combinations.
