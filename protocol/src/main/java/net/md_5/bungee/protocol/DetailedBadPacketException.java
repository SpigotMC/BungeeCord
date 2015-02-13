package net.md_5.bungee.protocol;

import io.netty.handler.codec.DecoderException;

public class DetailedBadPacketException extends BadPacketException {

    private final Integer packetId;
    private final DefinedPacket packet;
    private final Protocol protocol;
    private final ProtocolConstants.Direction direction;

    private static String makeMessage(String words, Integer packetId, DefinedPacket packet, Protocol protocol, ProtocolConstants.Direction direction) {
        String msg = protocol.name() + ":" + direction.name() + " ";

        if(packet != null) {
            msg += packet.getClass().getSimpleName();
        } else if(packetId != null) {
            msg += "PACKET(" + Integer.toHexString(packetId) + ")";
        }

        if(words != null) {
            msg = words + " [" + msg + "]";
        }

        return msg;
    }

    public DetailedBadPacketException(String message, Integer packetId, DefinedPacket packet, Protocol protocol, ProtocolConstants.Direction direction) {
        this(message, null, packetId, packet, protocol, direction);
    }

    public DetailedBadPacketException(String message, Throwable cause, Integer packetId, DefinedPacket packet, Protocol protocol, ProtocolConstants.Direction direction) {
        super(makeMessage(message, packetId, packet, protocol, direction),
              (cause instanceof DecoderException && cause.getCause() != null) ? cause.getCause() : cause);

        this.packetId = packetId;
        this.packet = packet;
        this.protocol = protocol;
        this.direction = direction;
    }

    /**
     * Return the ID of the packet that was bad, or null if no ID was decoded
     */
    public Integer getPacketId() {
        return packetId;
    }

    /**
     * Return the packet that failed to be read, or null if no known packet was ever instantiated
     */
    public DefinedPacket getPacket() {
        return packet;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public ProtocolConstants.Direction getDirection() {
        return direction;
    }
}
