package net.md_5.bungee.protocol.packet;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ChatSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.TagUtil;
import se.llbit.nbt.SpecificTag;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ShowDialog extends DefinedPacket
{

    protected Either<Integer, Dialog> dialog;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        int id = readVarInt( buf );
        if ( id == 0 )
        {
            dialog = Either.right( readDialog( buf, direction, protocolVersion ) );
        } else
        {
            dialog = Either.left( id );
        }
    }

    protected static Dialog readDialog(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        SpecificTag nbt = (SpecificTag) readTag( buf, protocolVersion );
        JsonElement json = TagUtil.toJson( nbt );
        return ChatSerializer.forVersion( protocolVersion ).getDialogSerializer().deserialize( json );
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( dialog.isLeft() )
        {
            writeVarInt( dialog.getLeft(), buf );
        } else
        {
            writeVarInt( 0, buf );
            writeDialog( dialog.getRight(), buf, direction, protocolVersion );
        }
    }

    protected static void writeDialog(Dialog dialog, ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        JsonElement json = ChatSerializer.forVersion( protocolVersion ).getDialogSerializer().toJson( dialog );
        SpecificTag nbt = TagUtil.fromJson( json );

        writeTag( nbt, buf, protocolVersion );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
