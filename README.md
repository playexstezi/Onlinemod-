# Online Mod

Minecraft Forge 1.20.1 mod for inviting friends into a hosted single-player world.

## Planned connection experience

- The host opens their single-player world and starts an online session.
- The mod displays a connection address in **IP:port** form whenever a direct route is available.
- A friend enters that address in the mod's join screen (or Minecraft Direct Connect if supported by the final implementation).
- The host can stop the session at any time. The world remains stored on the host's computer.

## Important networking facts

A mod cannot manufacture a publicly reachable IP address. It can discover the host's public IP, select a game port, and attempt automatic router port mapping (UPnP/NAT-PMP). Direct connections still depend on the router, ISP, and firewall allowing inbound traffic. Carrier-grade NAT and some restrictive networks prevent direct hosting.

The project will therefore be designed in stages:
1. Direct IP:port connection and clear connectivity diagnostics.
2. Optional automatic port mapping, with explicit user control.
3. If direct connectivity is impossible, an optional relay transport. A relay requires a reachable server; it cannot be provided by GitHub itself.

Do not post a public IP address in public chats. Share it only with people you trust.

## Current commands

Open the in-game pause menu and select **Online Mod** to open the first host-management screen. It can start the vanilla LAN listener, stop incoming connections, and refresh the detected public IPv4 address. The screen deliberately labels the game port as LAN-only until external reachability can be verified.

Commands are also available for diagnostics and troubleshooting:

- `/onlinemod ip` checks the public IPv4 address. This does not prove that inbound connections work.
- `/onlinemod host` opens the current single-player world using Minecraft's integrated-server LAN publication on a random port. The command does not configure your router or firewall.
- `/onlinemod stop` closes the integrated server's incoming network listener while leaving the world running. In this first implementation, hosting cannot be restarted until the world is closed and opened again.

**Important:** LAN publication alone is not internet hosting. Friends outside your local network will only be able to connect after a suitable router port mapping and firewall rule are in place. CGNAT or restrictive networks may prevent direct hosting entirely. The public IP and port are not shown as a working internet address until reachability checks exist.

## Development status

Implemented so far: Forge project scaffold, public IPv4 discovery, connection-address parsing, integrated-server LAN open/close service, initial pause-menu host screen, and unit tests for address parsing.

Still in progress: join screen, verified external reachability, router mapping with explicit consent, and optional relay support. Compatibility with large modpacks must be tested; it is not claimed until tested.

## Build

Requires Java 17 and Gradle. From the project directory:

```bash
gradle build
```

The mod JAR will be in `build/libs/`.
