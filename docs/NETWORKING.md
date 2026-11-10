# Networking design and current limitations

## Current connection path

The host screen publishes the vanilla integrated server using Minecraft's LAN publication API. The game selects an available TCP port. The host screen displays the public-facing IPv4 and a best-effort local IPv4. The Copy LAN address button copies local-address:port for friends on the same network.

The local address is selected using the operating system's route choice where possible. PCs with multiple adapters or VPN software may need the user to confirm the correct Wi-Fi/Ethernet address. Public IPv4 discovery only reports what an HTTPS provider sees; it does not prove that the game port is reachable.

## Direct internet setup (manual router configuration)

This build does not configure routers automatically. To attempt a direct connection over the internet:

1. Open a single-player world and start hosting in Online Mod.
2. Note the Local IPv4 and Server port shown in the host screen.
3. In the router administration interface, create a TCP port-forward rule. Set the external port to the server port shown by the mod, and forward it to the host PC's local IPv4 address using that same internal port.
4. In the host PC firewall, allow inbound TCP traffic for the Minecraft/Java process or that specific port on the appropriate network profile.
5. Keep the world open and hosting active. The selected port can change after the world is closed and reopened, so re-check the rule each session.
6. Give the joining player the public IPv4 plus the forwarded external port, in address:port format. They should test from a different internet connection, not the same Wi-Fi.
7. If it still fails, the ISP may use CGNAT or block inbound connections. Ordinary router port forwarding cannot solve that.

Only change router settings if you are authorized to administer that network. Never share router passwords or administrator credentials. Public IP addresses can reveal approximate network location; share them only with people you trust.

## Requirements and limitations

Direct hosting requires a listening game server, an inbound firewall rule, a router TCP port-forward, ISP support for inbound connections, and a joining player who can reach the host network. UPnP/NAT-PMP is not implemented. The mod does not silently change router settings and does not attempt to bypass ISP restrictions.

A reliable external reachability check needs a probe originating outside the host network. No external probe service is configured in this repository yet. A relay fallback requires a separately operated public server, bandwidth, authentication, abuse prevention, and transport integration compatible with Minecraft. GitHub is not a game relay.

## Privacy and compatibility

Do not upload or log public IPs or connection addresses. Avoid exposing router credentials. Joining clients need compatible Minecraft/Forge versions and matching required mods/configuration. Large modpacks have not yet been compatibility-tested.
