BungeeCord
==========
Layer 7 proxy designed to link Minecraft servers.
--------------------------------------------------

BungeeCord is a sophisticated proxy and API designed mainly to teleport players between multiple Minecraft servers. It is the latest incarnation of similar software written by the author from 2011-present.

Information
-----------
BungeeCord is maintained by [SpigotMC](https://www.spigotmc.org/) and has its own [discussion thread](https://www.spigotmc.org/go/bungeecord) with plenty of helpful information and links.

### Security warning

As your Minecraft servers have to run without authentication (online-mode=false) for BungeeCord to work, this poses a new security risk. Users may connect to your servers directly, under any username they wish to use. The kick "If you wish to use IP forwarding, please enable it in your BungeeCord config as well!" does not protect your Spigot servers.

To combat this, you need to restrict access to these servers for example with a firewall (please see [firewall guide](https://www.spigotmc.org/wiki/firewall-guide/)).

Running
-------
Running BungeeCord with Java 9 or higher requires the jvm argument `--add-opens java.base/java.lang.invoke=ALL-UNNAMED`

A complete guide to installing BungeeCord for end users is available on [SpigotMC Wiki](https://www.spigotmc.org/wiki/bungeecord-installation/)

Source
------
Source code is currently available on [GitHub](https://www.spigotmc.org/go/bungeecord-git).

Binaries
--------
Precompiled binaries are available for end users on [Jenkins](https://www.spigotmc.org/go/bungeecord-dl).

(c) 2012-2021 SpigotMC Pty. Ltd.
