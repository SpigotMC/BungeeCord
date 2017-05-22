package ru.leymooo.captcha;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.protocol.packet.extra.ConfirmTransaction;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.extra.PlayerPositionRotation;

/**
 *
 * @author Leymooo
 */
public class ProtocolTester
{

    @Setter
    private boolean settings = false;
    @Setter
    private boolean tpconfirm = false;
    @Setter
    private boolean mcbrand = false;
    @Setter
    private boolean alive = false;
    @Setter
    private boolean transaction = false;
    @Setter @Getter
    private boolean posRot = false;
    @Setter
    private PacketReciever pr;
    @Getter
    private final ConfirmTransaction transactionPacket = new ConfirmTransaction( (byte) 0, (short) this.pr.getRandom().nextInt( Short.MAX_VALUE ), false );
    @Getter
    private final KeepAlive keepAlivePacket;
    @Getter
    private final PlayerPositionRotation playerPositionPacket;

    // Я не знаю для чего этот класс. Но пусть будет.
    public ProtocolTester(PacketReciever pr)
    {
        this.pr = pr;
        this.keepAlivePacket = new KeepAlive( this.pr.getRandom().nextInt( 9999 ) );
        this.playerPositionPacket = new PlayerPositionRotation( 5.0D, 50.0D, 5.0D, 90.0F, 54.2F, this.pr.getRandom().nextInt( 9999 ), false );
    }

    public boolean isBot()
    {
        return ( System.currentTimeMillis() - pr.getJoinTime() >= 4000 ) && !( settings && tpconfirm && mcbrand && alive && transaction && posRot );
    }

}
