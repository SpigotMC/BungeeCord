package ru.leymooo.captcha;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.extra.ChunkPacket;
import net.md_5.bungee.protocol.packet.extra.PlayerAbilities;
import net.md_5.bungee.protocol.packet.extra.SetSlot;
import net.md_5.bungee.protocol.packet.extra.SpawnPosition;

public class CaptchaUser
{

    @Getter
    @Setter
    private PacketReciever pr;
    @Getter
    private String ip;
    @Getter
    private String captchaAnswer;
    private int retries = 3;
    //========================================================================
    private static final Login loginPacket = new Login( -1, (short) 0, 0, (short) 0, (short) 100, "flat", false );
    private static final SpawnPosition spawnPositionPacket = new SpawnPosition( 5, 60, 5 );
    private static final SetSlot setSlotPacket = new SetSlot( 0, 36, 358, 0 );
    private static final ChunkPacket chunkPacket = new ChunkPacket( 0, 0 );
    private static final PlayerAbilities abilitiesPacket = new PlayerAbilities( (byte) 6, 0.0F, 0.0F );
    //========================================================================

    public CaptchaUser(PacketReciever pr)
    {
        this.pr = pr;
    }

    public void sendJoinPackets()
    {
        ip = this.getPr().getConnection().getAddress().getAddress().getHostAddress();
        Channel channel = this.getPr().getConnection().getCh().getHandle();
        int protocol = this.getPr().getConnection().getPendingConnection().getHandshake().getProtocolVersion();
        this.getPr().getConnection().setClientEntityId( -1 );

        int loginId = protocol > 47 ? 0x23 : 1;
        this.write( channel, loginPacket, protocol, loginId );
        //НЕ НАВИЖУ ЭТИ ПАКЕТЫ!!!!!!!!!
        int spawnId = protocol > 47 ? ( protocol > 316 ? 0x45 : 0x43 ) : 5;
        this.write( channel, spawnPositionPacket, protocol, spawnId );

        int abilitiesId = protocol > 47 ? 0x2B : 57;
        this.write( channel, abilitiesPacket, protocol, abilitiesId );

        int positionId = protocol > 47 ? 0x2E : 8;
        this.write( channel, this.getPr().getPt().getPlayerPositionPacket(), protocol, positionId );

        int chunkId = protocol > 47 ? 0x20 : 33;
        this.write( channel, chunkPacket, protocol, chunkId );

        int slotId = protocol > 47 ? 0x16 : 47;
        this.write( channel, setSlotPacket, protocol, slotId );

        int keepAliveId = protocol > 47 ? 0x1F : 0;
        this.write( channel, this.getPr().getPt().getKeepAlivePacket(), protocol, keepAliveId );
        if ( protocol <= 47 )
        {
            this.getPr().getPt().setTpconfirm( true );
        }
        int transactionId = protocol > 47 ? 0x11 : 0x32;
        this.write( channel, this.getPr().getPt().getTransactionPacket(), protocol, transactionId );
        channel.flush();

        this.getAndSendCaptcha();
        this.write( channel, spawnPositionPacket, protocol, spawnId );
        channel.flush();
    }

    public void getAndSendCaptcha()
    {
        Object[] captcha = CaptchaGenerator.getInstance().getCaptchaAnswerWithPacket( getPr().getConnection().getPendingConnection().getHandshake().getProtocolVersion() );
        Channel channel = this.getPr().getConnection().getCh().getHandle();
        this.captchaAnswer = (String) captcha[0];
        this.write( channel, (ByteBuf) captcha[1] );
        channel.flush();
    }

    public void write(Channel channel, DefinedPacket packet, int protocol, int id)
    {
        ByteBuf buf = channel.alloc().buffer();

        DefinedPacket.writeVarInt( id, buf );
        packet.write( buf, ProtocolConstants.Direction.TO_CLIENT, protocol );
        channel.write( buf );
    }

    private void write(Channel channel, ByteBuf buf)
    {
        ByteBuf buffer = buf.copy();

        channel.write( buffer );
    }

    public void captchaEnter(Chat chat)
    {
        if ( getPr().getPt().isBot() || chat.getMessage().length() >= 5 || !getCaptchaAnswer().equalsIgnoreCase( chat.getMessage() ) )
        {
            if ( --this.retries == 0 )
            {
                if ( getPr().getPt().isBot() )
                {
                    kick( Configuration.getInstance().getBotKick() );
                } else
                {
                    kick( Configuration.getInstance().getWrongCaptchaKick() );
                }
                return;
            }
            getPr().getConnection().sendMessage( String.format( Configuration.getInstance().getWrongCaptcha(), this.retries, this.retries == 1 ? "а" : "и" ) );
            this.getAndSendCaptcha();
            return;
        }
        this.getPr().finish();
    }

    public void enterCapthca()
    {
        getPr().getConnection().sendMessage( Configuration.getInstance().getEnterCaptcha() );
    }

    public void kick(String reason)
    {
        this.getPr().getConnection().disconnect( reason );
    }
}
