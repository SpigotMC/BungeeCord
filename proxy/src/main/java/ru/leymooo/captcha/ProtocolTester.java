package ru.leymooo.captcha;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.protocol.packet.ConfirmTransaction;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.extra.PlayerPositionRotation;

/**
 *
 * @author Leymooo
 */
public class ProtocolTester
{

    @Setter
    public boolean settings = false;
    @Setter
    public boolean tpconfirm = false;
    @Setter
    public boolean mcbrand = false;
    @Setter
    public boolean alive = false;
    @Setter
    public boolean transaction = false;
    @Setter
    private PacketReciever pr;
    @Getter
    private final ConfirmTransaction transactionPacket = new ConfirmTransaction( (byte) 0, (short) 1, false );
    @Getter
    private final KeepAlive keepAlivePacket;
    @Getter
    private final PlayerPositionRotation playerPositionPacket;

    // Я не знаю для чего этот класс. Но пусть будет.
    public ProtocolTester(PacketReciever pr)
    {
        this.pr = pr;
        this.keepAlivePacket = new KeepAlive( this.pr.getRandom().nextInt( 9999 ) );
        this.playerPositionPacket = new PlayerPositionRotation( 5.0D, 500.0D, 5.0D, 90.0F, 54.2F, this.pr.getRandom().nextInt( 9999 ) );
    }

    public boolean isBot()
    {
        return ( System.currentTimeMillis() - pr.getJoinTime() >= 4000 ) && !( settings && tpconfirm && mcbrand && alive && transaction );
    }

}
