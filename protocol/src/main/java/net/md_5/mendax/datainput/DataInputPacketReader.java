package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.md_5.mendax.PacketDefinitions;
import net.md_5.mendax.PacketDefinitions.OpCode;

public class DataInputPacketReader
{

    private static final Instruction[][] instructions = new Instruction[ 256 ][];

    static
    {
        for ( int i = 0; i < instructions.length; i++ )
        {
            List<Instruction> output = new ArrayList<>();

            OpCode[] enums = PacketDefinitions.opCodes[i];
            if ( enums != null )
            {
                for ( OpCode struct : enums )
                {
                    try
                    {
                        output.add( (Instruction) Instruction.class.getDeclaredField( struct.name() ).get( null ) );
                    } catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex )
                    {
                        throw new UnsupportedOperationException( "No definition for " + struct.name() );
                    }
                }

                List<Instruction> crushed = new ArrayList<>();
                int nextJumpSize = 0;
                for ( Instruction child : output )
                {
                    if ( child instanceof Jump )
                    {
                        nextJumpSize += ( (Jump) child ).len;
                    } else
                    {
                        if ( nextJumpSize != 0 )
                        {
                            crushed.add( new Jump( nextJumpSize ) );
                        }
                        crushed.add( child );
                        nextJumpSize = 0;
                    }
                }
                if ( nextJumpSize != 0 )
                {
                    crushed.add( new Jump( nextJumpSize ) );
                }

                instructions[i] = crushed.toArray( new Instruction[ crushed.size() ] );
            }
        }
    }

    private static void readPacket(int packetId, DataInput in, byte[] buffer, int protocol) throws IOException
    {
        Instruction[] packetDef = null;
        if ( packetId + protocol < instructions.length )
        {
            packetDef = instructions[packetId + protocol];
        }

        if ( packetDef == null )
        {
            if ( protocol == PacketDefinitions.VANILLA_PROTOCOL )
            {
                throw new IOException( "Unknown packet id " + packetId );
            } else
            {
                readPacket( packetId, in, buffer, PacketDefinitions.VANILLA_PROTOCOL );
                return;
            }
        }

        for ( Instruction instruction : packetDef )
        {
            instruction.read( in, buffer );
        }
    }

    public static void readPacket(DataInput in, byte[] buffer, int protocol) throws IOException
    {
        int packetId = in.readUnsignedByte();
        readPacket( packetId, in, buffer, protocol );
    }
}
