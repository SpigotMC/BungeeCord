package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;



/**
 * An non abstract class of DefinedPacket
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public class UndefinedPacket extends DefinedPacket {
	public UndefinedPacket(int id, byte[] buf) {
		super(id, buf);
	}
}
