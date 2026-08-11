package net.md_5.bungee.serializer.chatnbt;

import static net.md_5.bungee.api.ChatColor.AQUA;
import static net.md_5.bungee.api.ChatColor.BOLD;
import static net.md_5.bungee.api.ChatColor.DARK_PURPLE;
import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;
import static net.md_5.bungee.api.ChatColor.of;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ChatVersion;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.protocol.util.TagUtil;
import org.junit.jupiter.api.Test;

class NbtToComponentConverterTest
{
    private static TypedTag toJsonToNbt(BaseComponent component)
    {
        VersionedComponentSerializer serializer = VersionedComponentSerializer.forVersion( ChatVersion.V1_21_5 );
        return TagUtil.fromJson( serializer.toJson( component ) );
    }

    private static TypedTag toJsonToNbt(BaseComponent[] component)
    {
        VersionedComponentSerializer serializer = VersionedComponentSerializer.forVersion( ChatVersion.V1_21_5 );
        return TagUtil.fromJson( serializer.getGson().toJsonTree( component ) );
    }

    @Test
    public void testSimple()
    {
        BaseComponent original = new TextComponent( "Hello NBT!" );

        assertEquals( original, NbtToComponentConverter.toComponent( toJsonToNbt( original ) ) );
    }

    @Test
    public void testColorized()
    {
        BaseComponent test = TextComponent.fromLegacy( AQUA + "Aqua " + RED + BOLD + "RedBold" );
        BaseComponent converted = NbtToComponentConverter.toComponent( toJsonToNbt( test ) );

        assertEquals( test, converted );
        assertEquals( test.toPlainText(), converted.toPlainText() );
        assertEquals( test.toLegacyText(), converted.toLegacyText() );
    }

    @Test
    public void testArrayAndClickEvent()
    {
        BaseComponent[] test = TextComponent.fromLegacyText( "Text http://spigotmc.org " + GREEN + "google.com/test" );
        BaseComponent[] converted = NbtToComponentConverter.toComponentArray( toJsonToNbt( test ) );

        assertArrayEquals( test, converted );
        assertEquals( BaseComponent.toPlainText( test ), BaseComponent.toPlainText( converted ) );
        assertEquals( BaseComponent.toLegacyText( test ), BaseComponent.toLegacyText( converted ) );


        ClickEvent url1 = test[ 1 ].getClickEvent();
        assertNotNull( url1 );
        assertTrue( url1.getAction() == ClickEvent.Action.OPEN_URL );
        assertEquals( "http://spigotmc.org", url1.getValue() );

        ClickEvent url2 = test[ 3 ].getClickEvent();
        assertNotNull( url2 );
        assertTrue( url2.getAction() == ClickEvent.Action.OPEN_URL );
        assertEquals( "http://google.com/test", url2.getValue() );
    }

    @Test
    public void testComplex()
    {
        BaseComponent test = new ComponentBuilder( "a" )
                .append( "b" ).color( AQUA )
                .append( "c" ).color( RED ).bold( true )
                .append( "d" ).italic( true ).insertion( "i'll get inserted" )
                .append( "e", ComponentBuilder.FormatRetention.NONE ).strikethrough( true )
                .append( "f" ).obfuscated( true )
                .append( "g" ).underlined( true )
                .append( "h" ).color( DARK_PURPLE )
                .append( "i" ).shadowColor( new Color( 1f, .5f, .1f, .4f ) )
                .append( "j" ).font( "minecraft:anotherfontfortest" ).event( new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new Text( new ComponentBuilder( "hover" )
                                .color( of( new Color( .5f, 1f, 0f ) ) )
                                .italic( true )
                                .build() ) ) )
                .append( "k", ComponentBuilder.FormatRetention.NONE ).event( new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/test command" )
                )
                .append( "l", ComponentBuilder.FormatRetention.NONE ).event( new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/test command" )
                )
                .append( "m" ).color( GOLD ).event( new ClickEvent(
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        "/test suggest command" ) ).event( new HoverEvent( HoverEvent.Action.SHOW_ITEM,
                            new Item( "itemid", 4, ItemTag.ofNbt( "bla" ) ) ) )
                .append( "n", ComponentBuilder.FormatRetention.NONE )
                .build();
        BaseComponent converted = NbtToComponentConverter.toComponent( toJsonToNbt( test ) );

        VersionedComponentSerializer serializer = VersionedComponentSerializer.forVersion( ChatVersion.V1_21_5 );
        System.out.println( serializer.toString( test ) );
        System.out.println( serializer.toString( converted ) );

        assertEquals( test, converted );
        assertEquals( test.toPlainText(), converted.toPlainText() );
        assertEquals( test.toLegacyText(), converted.toLegacyText() );
    }
}
