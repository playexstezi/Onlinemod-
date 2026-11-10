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

## Development status

Initial Forge project scaffold. Networking, in-game screens, NAT traversal, and relay are not implemented yet. Compatibility with large modpacks must be tested; it is not claimed until tested.

## Build

Requires Java 17 and Gradle. From the project directory:

```bash
gradle build
```

The mod JAR will be in `build/libs/`.
