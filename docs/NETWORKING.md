# Networking design and current limitations

## Current connection path

The host screen publishes the vanilla integrated server using Minecraft's LAN publication API. The game selects an available port. The join screen parses a user-supplied endpoint and delegates the connection to Minecraft's normal `ConnectScreen` flow.

The public IPv4 service asks external HTTPS providers what address they see for the host. This only discovers the public-facing address; it does not prove that a TCP connection can reach the Minecraft listener.

## What is required for direct internet hosting

A direct public connection requires all of these:

1. The integrated server is listening on the selected TCP port.
2. The host operating-system firewall allows inbound TCP traffic to that port.
3. The router forwards the same port to the host PC's private LAN address, or an equivalent mapping exists.
4. The ISP permits inbound traffic and does not place the connection behind restrictive CGNAT.
5. The joining player can reach the host network.

A successful public-IP lookup proves none of items 1–5 except that the lookup provider observed a public-facing address.

## Port mapping policy

UPnP/NAT-PMP is **not implemented**. Any future implementation must:
- be disabled by default or require a clear opt-in action;
- explain exactly which TCP port is requested and why;
- request only the Minecraft listener port;
- show whether the mapping succeeded and its lease duration;
- remove the mapping when the session stops or the client exits;
- handle routers where UPnP/NAT-PMP is disabled or unavailable;
- never claim that a mapping guarantees reachability.

Do not silently change router settings. Do not attempt to bypass ISP restrictions.

## Reachability checks

A reliable external check needs a probe that originates outside the host's network and connects to the actual listener. Testing the public address from inside the same LAN is not reliable because routers differ in NAT loopback support. A local socket check or successful public-IP lookup must never be presented as an external reachability test.

A future probe service would need a documented protocol, abuse limits, privacy controls, and an operational endpoint. No such service is configured in this repository yet.

## Relay fallback

A relay is the fallback for networks where direct inbound connections are impossible. It requires a real, publicly reachable relay service, bandwidth, authentication, abuse prevention, and a transport integration compatible with Minecraft's networking stack. A GitHub repository or GitHub Actions runner is not a persistent relay server.

## Privacy and safety

- Public IP addresses are connection information. Show them only in the host's UI and let the host choose when to copy/share them.
- Do not upload public IPs to a project backend.
- Do not log public IPs or connection addresses.
- Avoid exposing router credentials or asking users to paste them into the mod.
- Keep router changes opt-in and reversible.

## Modpack compatibility

The joining client must have compatible Minecraft/Forge versions and matching required mods/configuration. A network connection alone cannot make incompatible registries, entities, blocks, or packets compatible. Test large packs on both host and client before claiming support.
