package ru.leymooo.botfilter.packets.chunk;


import gnu.trove.TCollections;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;

public class BlockStorage {

    private int bitsPerEntry;

    private TIntList states;
    private FlexibleStorage storage;

    public BlockStorage() {
        this.bitsPerEntry = 4;

        this.states = new TIntArrayList();
        this.states.add(0);

        this.storage = new FlexibleStorage(this.bitsPerEntry, 4096);
    }


    private static int index(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    public void write(ByteBuf out) {
        out.writeByte(this.bitsPerEntry);

        if (this.bitsPerEntry <= 8) {
            DefinedPacket.writeVarInt(this.states.size(), out);
            for (int state : this.states.toArray()) {
                DefinedPacket.writeVarInt(state, out);
            }
        }

        long[] data = this.storage.getData();
        DefinedPacket.writeVarInt(data.length, out);
        for (long state : data) {
            out.writeLong(state);
        }
    }

    public int getBitsPerEntry() {
        return this.bitsPerEntry;
    }

    public TIntList getStates() {
        return TCollections.unmodifiableList(states);
    }

    public FlexibleStorage getStorage() {
        return this.storage;
    }

    public int get(int x, int y, int z) {
        int id = this.storage.get(index(x, y, z));
        return this.bitsPerEntry <= 8 ? (id >= 0 && id < this.states.size() ? this.states.get(id) : 0) : id;
    }

    public void set(int x, int y, int z, int state) {
        int id = this.bitsPerEntry <= 8 ? this.states.indexOf(state) : state;
        if (id == -1) {
            this.states.add(state);
            if (this.states.size() > 1 << this.bitsPerEntry) {
                this.bitsPerEntry++;

                TIntList oldStates = this.states;
                if (this.bitsPerEntry > 8) {
                    oldStates = new TIntArrayList(this.states);
                    this.states.clear();
                    this.bitsPerEntry = 13;
                }

                FlexibleStorage oldStorage = this.storage;
                this.storage = new FlexibleStorage(this.bitsPerEntry, this.storage.getSize());
                for (int index = 0; index < this.storage.getSize(); index++) {
                    this.storage.set(index, this.bitsPerEntry <= 8 ? oldStorage.get(index) : oldStates.get(index));
                }
            }

            id = this.bitsPerEntry <= 8 ? this.states.indexOf(state) : state;
        }

        this.storage.set(index(x, y, z), id);
    }

    public boolean isEmpty() {
        for (int index = 0; index < this.storage.getSize(); index++) {
            if (this.storage.get(index) != 0) {
                return false;
            }
        }

        return true;
    }
}
