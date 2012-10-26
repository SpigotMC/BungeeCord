Mendax
======
The Minecraft protocol is a lie!
--------------------------------
Mendax is a predominantly I Minecraft protocol parser and inspector. It also includes a built in proxy.

History
-------
MinecraftProtocolLib was designed to be the most efficient way of separating the Minecraft protocol into byte arrays. With other tools like SMProxy by @SirCmpwn and MinerHat by @sk89q becoming obsolete, the decision was made to expand MinecraftProtocolLib into a parser as well. Mendax is that parser.
With no runtime dependencies Mendax is the perfect choice for use in your next Java, Minecraft related project.

Operation
---------
Mendax has 2 modes of operation.

- Parsing Mode - Bytes are read from an InputStream and returned in byte arrays. This mode is the most efficient, however the raw data itself is not very useful
- Inspection Mode - Bytes are read from an InputStream and Packet objects are returned. These packet objects contain all useable information about a packet. In this mode Items, Locations and Compressed Data are expanded into their own fields and data types.


>Please note that the above features may not be entirely implemented in the current version of Mendax. Additionally breaking changes may occur without warning, however they should all be easy to update.