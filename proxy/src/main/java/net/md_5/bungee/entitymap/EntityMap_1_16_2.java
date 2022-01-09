package net.md_5.bungee.entitymap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class EntityMap_1_16_2 extends EntityMap
{

    static final EntityMap_1_16_2 INSTANCE_1_16_2 = new EntityMap_1_16_2( 0x04, 0x2D );
    static final EntityMap_1_16_2 INSTANCE_1_17 = new EntityMap_1_16_2( 0x04, 0x2D );
    static final EntityMap_1_16_2 INSTANCE_1_18 = new EntityMap_1_16_2( 0x04, 0x2D );
    static final EntityMap_1_16_2 INSTANCE_1_19 = new EntityMap_1_16_2( 0x02, 0x2F );
    static final EntityMap_1_16_2 INSTANCE_1_19_1 = new EntityMap_1_16_2( 0x02, 0x30 );
    static final EntityMap_1_16_2 INSTANCE_1_19_4 = new EntityMap_1_16_2( 0x03, 0x30 );
    static final EntityMap_1_16_2 INSTANCE_1_20_2 = new EntityMap_1_16_2( -1, 0x33 );
    static final EntityMap_1_16_2 INSTANCE_1_20_3 = new EntityMap_1_16_2( -1, 0x34 );
    //
    private final int spawnPlayerId;
    private final int spectateId;

    @Override
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    public void rewriteClientbound(PacketWrapper wrapper, int oldId, int newId, int protocolVersion)
    {
        ByteBuf packet = wrapper.buf;

        // Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;

        if ( packetId == spawnPlayerId )
        {
            EntityMap_1_8.rewriteSpawnPlayerUuid( wrapper, readerIndex, packetIdLength );
        }
        packet.readerIndex( readerIndex );
    }

    @Override
    public void rewriteServerbound(PacketWrapper wrapper, int oldId, int newId)
    {
        ByteBuf packet = wrapper.buf;

        // Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;

        if ( packetId == spectateId && !BungeeCord.getInstance().getConfig().isIpForward() )
        {
            EntityMap_1_8.rewriteSpectateUuid( wrapper, readerIndex, packetIdLength );
        }
        packet.readerIndex( readerIndex );
    }
}
