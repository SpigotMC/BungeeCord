package net.md_5.bungee.dialog;

import static org.junit.jupiter.api.Assertions.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.api.dialog.DialogBase;
import net.md_5.bungee.api.dialog.NoticeDialog;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import org.junit.jupiter.api.Test;

public class SimpleTest
{

    public static void testDissembleReassemble(Dialog notice)
    {
        String json = VersionedComponentSerializer.getDefault().getDialogSerializer().toString( notice );
        Dialog parsed = VersionedComponentSerializer.getDefault().getDialogSerializer().deserialize( json );
        assertEquals( notice, parsed );
    }

    @Test
    public void testSimple()
    {
        String json = "{type:\"minecraft:notice\",title:\"Hello\"}";
        Dialog deserialized = VersionedComponentSerializer.getDefault().getDialogSerializer().deserialize( json );
        String serialized = VersionedComponentSerializer.getDefault().getDialogSerializer().toString( deserialized );

        assertEquals( "{\"type\":\"minecraft:notice\",\"title\":{\"text\":\"Hello\"}}", serialized );
    }

    @Test
    public void testNotice()
    {
        testDissembleReassemble( new NoticeDialog( new DialogBase( new ComponentBuilder( "Hello" ).color( ChatColor.RED ).build() ) ) );
    }
}
