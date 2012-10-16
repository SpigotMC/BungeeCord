package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class Packet46GameState extends DefinedPacket
{

    /**
     * Update game state. When sent with a state of 2, rain will cease.
     *
     * @param state the new game state
     * @param mode the new game mode. Used when state == 3.
     */
    public Packet46GameState(byte state, byte mode)
    {
        super(0x46);
        writeByte(state);
        writeByte(mode);
    }
}
