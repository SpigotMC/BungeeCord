package ru.leymooo.captcha;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.extra.ChunkPacket;
import net.md_5.bungee.protocol.packet.extra.EntityEffect;
import net.md_5.bungee.protocol.packet.extra.PlayerAbilities;
import net.md_5.bungee.protocol.packet.extra.SetSlot;
import net.md_5.bungee.protocol.packet.extra.SpawnPosition;

public class CaptchaUser
{

    @Getter
    @Setter
    private PacketReciever pr;
    @Getter
    private String captchaAnswer;
    private int retries = 3;
    //========================================================================
    private static final Login loginPacket = new Login( -1, (short) 0, Configuration.getInstance().getWorldType(), (short) 0, (short) 100, "flat", false );
    private static final SpawnPosition spawnPositionPacket = new SpawnPosition( 5, 60, 5 );
    private static final SetSlot setSlotPacket = new SetSlot( 0, 36, 358, 0 );
    private static final ChunkPacket chunkPacket = new ChunkPacket( 0, 0, new byte[ 256 ] );
    private static final PlayerAbilities abilitiesPacket = new PlayerAbilities( (byte) 6, 0.0F, 0.0F );
    private static final EntityEffect entityEffectPacket = new EntityEffect( -1, (byte) 16, (byte) 2, 30 * 20, (byte) 0x01 );
    //========================================================================

    public CaptchaUser(PacketReciever pr)
    {
        this.pr = pr;
    }

    public void sendJoinPackets()
    {
        UserConnection con = this.getPr().getConnection();
        Connection.Unsafe unsafe = con.unsafe();
        con.setClientEntityId( -1 );

        unsafe.sendPacket( loginPacket );
        unsafe.sendPacket( spawnPositionPacket );
        unsafe.sendPacket( abilitiesPacket );
        unsafe.sendPacket( this.getPr().getPt().getPlayerPositionPacket() );
        unsafe.sendPacket( chunkPacket );
        unsafe.sendPacket( setSlotPacket );
        unsafe.sendPacket( this.getPr().getPt().getKeepAlivePacket() );
        unsafe.sendPacket( this.getPr().getPt().getTransactionPacket() );
        unsafe.sendPacket( entityEffectPacket );

        if ( con.getPendingConnection().getHandshake().getProtocolVersion() <= 47 )
        {
            getPr().getPt().setTpconfirm( true );
        }

        this.getAndSendCaptcha();
    }

    public void getAndSendCaptcha()
    {
        Object[] captcha = CaptchaGenerator.getInstance().getCaptchaAnswerWithPacket( getPr().getConnection().getPendingConnection().getHandshake().getProtocolVersion() );
        Channel channel = this.getPr().getConnection().getCh().getHandle();
        this.captchaAnswer = (String) captcha[0];
        this.write( channel, (ByteBuf) captcha[1] );
        channel.flush();
    }

    private void write(Channel channel, ByteBuf buf)
    {
        ByteBuf buffer = buf.copy();

        channel.write( buffer );
    }

    public void captchaEnter(Chat chat)
    {
        String msg = chat.getMessage().replace( "/", "" );
        if ( getPr().getPt().isBot() || msg.length() >= 5 || !getCaptchaAnswer().equalsIgnoreCase( msg ) )
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
