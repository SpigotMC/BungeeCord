package net.md_5.bungee.dialog;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.api.dialog.DialogBase;
import net.md_5.bungee.api.dialog.NoticeDialog;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import org.junit.jupiter.api.Test;

public class SimpleTest
{

    @Test
    public void testNotice()
    {
        String json = "{type:\"minecraft:notice\",title:\"Hello\"}";
        Dialog deserialized = VersionedComponentSerializer.getDefault().getDialogSerializer().deserialize( json );
        System.err.println( deserialized );

        Dialog notice = new NoticeDialog( new DialogBase( new ComponentBuilder( "Hello" ).color( ChatColor.RED ).build() ) );
        String newJson = VersionedComponentSerializer.getDefault().getDialogSerializer().toString( notice );
        System.err.println( newJson );
    }
}
