package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Setter;

public class MinecraftEncoder extends MessageToByteEncoder<DefinedPacket>
{

    public MinecraftEncoder( Protocol protocol, boolean server, int protocolVersion ){
        this.server = server;
        this.protocolVersion = protocolVersion;
        setProtocol(protocol);
    }

    private Protocol.DirectionData prot;
    private boolean server;
    @Setter
    private int protocolVersion;

    @Override
    protected void encode(ChannelHandlerContext ctx, DefinedPacket msg, ByteBuf out) throws Exception
    {
        DefinedPacket.writeVarInt( prot.getId( msg.getClass() ), out );
        msg.write( out, prot.getDirection(), protocolVersion );
    }

    public void setProtocol(Protocol protocol){
        if(server){
            prot = protocol.TO_CLIENT;
        } else {
            prot = protocol.TO_SERVER;
        }
    }
}
