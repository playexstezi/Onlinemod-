# Networking design

## Direct IP mode

The first connection mode is a direct endpoint, displayed as `public-ip:port`.
`PublicIpService` discovers the public IPv4 address through HTTPS providers.
It deliberately does not claim that the address is reachable.

For a direct connection to work, all of these must be true:

- the integrated Minecraft server is listening on the selected TCP port;
- the operating-system firewall permits the connection;
- the router forwards the port to the host PC, or an equivalent mapping exists;
- the ISP does not block inbound traffic or place the subscriber behind restrictive CGNAT.

## Port mapping

UPnP/NAT-PMP support is a separate next milestone. It must be opt-in, request only
the selected game port, show the mapping result, and remove the mapping when the
session ends. Never silently change router settings.

## Relay fallback

A relay is required when direct inbound connections are impossible. It needs a
publicly reachable relay server and bandwidth. GitHub repositories cannot act as
a live relay server. The relay protocol will be designed separately from the
Minecraft integrated-server lifecycle.

## Privacy

The public IP is sensitive connection information. Show it only to the host,
provide a copy button, and warn before sharing it. Do not upload it to a project
backend or write it to logs.
