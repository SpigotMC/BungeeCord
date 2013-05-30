package net.md_5.bungee.protocol.skip;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.protocol.OpCode;
import net.md_5.bungee.protocol.Protocol;

public class PacketReader
{

    private final Instruction[][] instructions;

    public PacketReader(Protocol protocol)
    {
        instructions = new Instruction[ protocol.getOpCodes().length ][];
        for ( int i = 0; i < instructions.length; i++ )
        {
            List<Instruction> output = new ArrayList<>();

            OpCode[] enums = protocol.getOpCodes()[i];
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

    public void tryRead(short packetId, ByteBuf in)
    {
        Instruction[] packetDef = instructions[packetId];

        if ( packetDef != null )
        {
            for ( Instruction instruction : packetDef )
            {
                instruction.read( in );
            }
        }
    }
}
