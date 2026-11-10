# Online Mod

A host/join mod for **Minecraft Java 1.20.1 + Minecraft Forge 47.x**. The goal is to let a player open a single-player world and invite friends without requiring every player to use a third-party LAN VPN.

> **Current status: early testing build.** The in-game host and join screens, public IPv4 lookup, LAN publication, and address validation are implemented. Direct internet connections are not automatically configured or verified yet.

## Current features

- Host screen from the pause menu.
- Join screen from the multiplayer menu and host screen.
- Public IPv4 discovery through HTTPS providers, with a fallback.
- Copy public IPv4 to clipboard.
- Vanilla integrated-server LAN publication on an automatically selected port.
- Stop incoming connections while leaving the single-player world running.
- Strict `host:port` parsing for hostnames, IPv4 and bracketed IPv6.
- English and Russian interface translations.
- JUnit tests and GitHub Actions builds.
- Successful CI builds publish an installable JAR artifact.

## Install

1. Install **Minecraft Java 1.20.1**.
2. Install **Minecraft Forge 47.x** for 1.20.1 and launch it once.
3. Download the latest successful **OnlineMod-1.20.1-Forge** artifact from the repository's [Actions page](https://github.com/playexstezi/Onlinemod-/actions).
4. Extract the downloaded artifact ZIP. Put the `onlinemod-0.1.0.jar` file into the Minecraft `mods` folder.
5. Start Minecraft using the Forge 1.20.1 profile.

Both host and joining player need compatible Minecraft/Forge versions and the same required mods/configuration. This build has not yet been tested with large modpacks.

## Try on a local network (same Wi-Fi/router)

1. The host opens a single-player world.
2. Press Escape and choose **Online Mod**.
3. Choose **Open world for players**.
4. The host's local server port is shown in the screen.
5. The joining player uses **Join via Online Mod** and enters the host PC's local IPv4 address followed by the port, for example `192.168.1.20:54321`.
6. If the connection fails, check that both PCs are on the same network and that the host firewall allows Java/Minecraft on a private network.

The local IPv4 address can be found on Windows with `ipconfig` (look for the active Wi-Fi/Ethernet adapter's IPv4 Address).

## Playing over the internet

The public IPv4 displayed by the mod is only an address discovery result. It does not prove the Minecraft port is reachable. Direct public hosting requires:
- the host firewall to allow inbound traffic on the selected TCP port;
- the router to forward that TCP port to the host PC's private address;
- the ISP to permit inbound connections and not place the connection behind restrictive CGNAT.

UPnP/NAT-PMP support and external reachability testing are not implemented yet. Do not share the public IP as a working invitation address until port forwarding is configured and tested from a different network. A relay fallback would require a separately operated public server; GitHub is not a game relay.

See [networking notes](docs/NETWORKING.md) for details.

## Commands

- `/onlinemod ip` — discover public IPv4; does not test inbound connectivity.
- `/onlinemod host` — publish the current single-player world on a LAN listener.
- `/onlinemod stop` — close incoming connections while leaving the world running.

In this first implementation, hosting cannot be restarted in the same world after stopping; close and reopen the world first.

## Build locally

Requirements: Java 17 and Gradle 8.8.

```bash
gradle clean build
```

The installable mod JAR is generated in `build/libs/`.

## Compatibility status

The project targets Forge 1.20.1. Large modpacks (including packs with 100–200 mods, Create, and Biomes O' Plenty) have not yet been compatibility-tested. A network connection cannot make incompatible registries, entities, blocks, or packets compatible.
