package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

public class MapDataPacket
extends DefinedPacket {
    private int mapId;
    private byte scale;
    private Type type;
    private MapData data;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        MapDataPacket.writeVarInt(this.mapId, buf);
        if (protocolVersion >= 47) {
            buf.writeByte(this.scale);
            MapDataPacket.writeVarInt(0, buf);
            if (protocolVersion >= 107) {
                buf.writeBoolean(false);
            }
            switch (this.type) {
                case IMAGE: {
                    MapDataNew column = (MapDataNew)this.data;
                    buf.writeByte(column.getColumns());
                    if (column.getColumns() <= 0) return;
                    buf.writeByte(column.getRows());
                    buf.writeByte(column.getX());
                    buf.writeByte(column.getY());
                    MapDataPacket.writeVarInt(column.getData().length, buf);
                    buf.writeBytes(column.getData());
                    return;
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        } else {
            byte[] data = null;
            switch (this.type) {
                case IMAGE: {
                    MapColumnUpdate column = (MapColumnUpdate)this.data;
                    data = new byte[column.getHeight() + 3];
                    data[0] = 0;
                    data[1] = (byte)column.getX();
                    data[2] = (byte)column.getY();
                    System.arraycopy(column.getColors(), 0, data, 3, data.length - 3);
                    break;
                }
                case PLAYERS: {
                    MapPlayers players = (MapPlayers)this.data;
                    data = new byte[players.getPlayers().size() * 3 + 1];
                    data[0] = 1;
                    for (int index = 0; index < players.getPlayers().size(); ++index) {
                        MapPlayer player = players.getPlayers().get(index);
                        data[index * 3 + 1] = (byte)((byte)player.getIconSize() << 4 | (byte)player.getIconRotation() & 15);
                        data[index * 3 + 2] = (byte)player.getCenterX();
                        data[index * 3 + 3] = (byte)player.getCenterZ();
                    }
                    break;
                }
                case SCALE: {
                    MapScale scale = (MapScale)this.data;
                    data = new byte[]{2, (byte)scale.getScale()};
                }
            }
            buf.writeShort(data.length);
            buf.writeBytes(data);
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        throw new UnsupportedOperationException();
    }

    public int getMapId() {
        return this.mapId;
    }

    public byte getScale() {
        return this.scale;
    }

    public Type getType() {
        return this.type;
    }

    public MapData getData() {
        return this.data;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public void setScale(byte scale) {
        this.scale = scale;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setData(MapData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MapDataPacket(mapId=" + this.getMapId() + ", scale=" + this.getScale() + ", type=" + (Object)((Object)this.getType()) + ", data=" + this.getData() + ")";
    }

    @ConstructorProperties(value={"mapId", "scale", "type", "data"})
    public MapDataPacket(int mapId, byte scale, Type type, MapData data) {
        this.mapId = mapId;
        this.scale = scale;
        this.type = type;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MapDataPacket)) {
            return false;
        }
        MapDataPacket other = (MapDataPacket)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getMapId() != other.getMapId()) {
            return false;
        }
        if (this.getScale() != other.getScale()) {
            return false;
        }
        Type this_type = this.getType();
        Type other_type = other.getType();
        if (this_type == null ? other_type != null : !this_type.equals((Object)other_type)) {
            return false;
        }
        MapData this_data = this.getData();
        MapData other_data = other.getData();
        if (this_data == null ? other_data != null : !this_data.equals(other_data)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(Object other) {
        return other instanceof MapDataPacket;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 59 + this.getMapId();
        result = result * 59 + this.getScale();
        Type $type = this.getType();
        result = result * 59 + ($type == null ? 0 : $type.hashCode());
        MapData $data = this.getData();
        result = result * 59 + ($data == null ? 0 : $data.hashCode());
        return result;
    }

    public static class MapScale
    implements MapData {
        private int scale;

        public MapScale(int scale) {
            this.scale = scale;
        }

        public int getScale() {
            return this.scale;
        }
    }

    public static class MapPlayer {
        private int iconSize;
        private int iconRotation;
        private int centerX;
        private int centerZ;

        public MapPlayer(int iconSize, int iconRotation, int centerX, int centerZ) {
            this.iconSize = iconSize;
            this.iconRotation = iconRotation;
            this.centerX = centerX;
            this.centerZ = centerZ;
        }

        public int getIconSize() {
            return this.iconSize;
        }

        public int getIconRotation() {
            return this.iconRotation;
        }

        public int getCenterX() {
            return this.centerX;
        }

        public int getCenterZ() {
            return this.centerZ;
        }
    }

    public static class MapPlayers
    implements MapData {
        private List<MapPlayer> players = new ArrayList<MapPlayer>();

        public MapPlayers(List<MapPlayer> players) {
            this.players = players;
        }

        public List<MapPlayer> getPlayers() {
            return new ArrayList<MapPlayer>(this.players);
        }
    }

    public static class MapDataNew
    implements MapData {
        private int columns;
        private int rows;
        private int x;
        private int y;
        private byte[] data;

        public MapDataNew(int columns, int rows, int x, int y, byte[] data) {
            this.columns = columns;
            this.rows = rows;
            this.x = x;
            this.y = y;
            this.data = data;
        }

        public int getColumns() {
            return this.columns;
        }

        public int getRows() {
            return this.rows;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public byte[] getData() {
            return this.data;
        }
    }

    public static class MapColumnUpdate
    implements MapData {
        private int x;
        private int y;
        private int height;
        private byte[] colors;

        public MapColumnUpdate(int x, int y, int height, byte[] colors) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.colors = colors;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getHeight() {
            return this.height;
        }

        public byte[] getColors() {
            return this.colors;
        }
    }

    public static interface MapData {
    }

    public static enum Type {
        IMAGE,
        PLAYERS,
        SCALE;
    }

}
