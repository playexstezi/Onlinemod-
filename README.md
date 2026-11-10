# Online Mod

A host/join mod for Minecraft Java 1.20.1 + Minecraft Forge 47.x. The goal is to let a player open a single-player world and invite friends without requiring every player to use a third-party LAN VPN.

> Current status: early testing build. The host and join screens, public IPv4 lookup, local IPv4 discovery, LAN publication, and address validation are implemented. Internet play still requires manual router/firewall configuration; the mod does not automatically open router ports or bypass CGNAT.

## Current features

- Host screen from the pause menu.
- Join screen from the multiplayer menu and host screen.
- Public IPv4 discovery through HTTPS providers.
- Best-effort local IPv4 discovery using the operating system's selected network route.
- Copy public IP, local address plus active server port, and a clearly marked public address candidate.
- Optional, explicit UPnP IGD port mapping for the current Minecraft TCP port, with a one-hour renewable lease and cleanup when hosting stops or the world is left.
- Vanilla integrated-server LAN publication on an automatically selected TCP port.
- Stop incoming connections while leaving the single-player world running.
- Strict host:port parsing for hostnames, IPv4 and bracketed IPv6.
- English and Russian interface translations.
- JUnit tests and GitHub Actions builds that publish the installable JAR.

## Install

1. Install Minecraft Java 1.20.1.
2. Install Minecraft Forge 47.x for 1.20.1 and launch it once.
3. Download the latest successful OnlineMod-1.20.1-Forge artifact from the repository Actions page.
4. Extract the downloaded artifact ZIP. Put the onlinemod-0.1.0.jar file into the Minecraft mods folder.
5. Start Minecraft using the Forge 1.20.1 profile.

Both host and joining player need compatible Minecraft/Forge versions and the same required mods/configuration. Large modpacks have not yet been compatibility-tested.

## Try on a local network (same Wi-Fi/router)

1. The host opens a single-player world.
2. Press Escape and choose Online Mod.
3. Choose Open world for players.
4. The screen shows Local IPv4 and Server port. Use Copy LAN address to copy them together.
5. The joining player uses Join via Online Mod and enters the copied address, for example 192.168.1.20:54321.
6. If the connection fails, check that both PCs are on the same network and that the host firewall allows Java/Minecraft on a private network.

On PCs with VPNs or several network adapters, confirm that the detected Local IPv4 belongs to the Wi-Fi/Ethernet network the friend is using.

## Try direct internet play (manual setup)

This is not automatic yet. The host must configure the network:

1. Start hosting and note Local IPv4 and Server port in Online Mod.
2. In the router administration interface, create a TCP port-forward rule. Forward the displayed server port to the displayed Local IPv4 using the same internal port.
3. Allow inbound TCP traffic for Minecraft/Java or that specific port in the host PC firewall.
4. Keep the world open. The selected port may change next time the world is opened, so re-check the router rule each session. The Copy public address candidate button copies public-ip:port for convenience, but the mod does not test whether it is reachable.
5. The friend connects using the host's public IPv4 and forwarded port, in public-ip:port format, from a different internet connection.

Use UPnP or router settings only on a network you are authorized to manage. UPnP may be disabled or unsupported by the router; the host PC firewall may still need to allow Minecraft/Java. The mapping is limited to the active Minecraft TCP port, but any internet-facing game server should only be shared with people you trust. If the ISP uses CGNAT, double NAT, or blocks inbound traffic, UPnP on the local router cannot solve that. A public IP lookup and a successful UPnP response are not external reachability tests. NAT-PMP, external port testing, and a relay service are not implemented.

See docs/NETWORKING.md for networking details and limitations.

## Commands

- /onlinemod ip — discover public IPv4; does not test inbound connectivity.
- /onlinemod host — publish the current single-player world on a LAN listener.
- /onlinemod stop — close incoming connections while leaving the world running.

In this first implementation, hosting cannot be restarted in the same world after stopping; close and reopen the world first.

## Build locally

Requirements: Java 17 and Gradle 8.8.

```bash
gradle clean build
```

The installable mod JAR is generated in build/libs/.

## Compatibility status

The project targets Forge 1.20.1. Large modpacks (including packs with 100–200 mods, Create, and Biomes O' Plenty) have not yet been compatibility-tested. A network connection cannot make incompatible registries, entities, blocks, or packets compatible.
