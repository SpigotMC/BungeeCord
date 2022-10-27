package net.md_5.bungee.api.chat;

import java.awt.Color;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.Assert;
import org.junit.Test;

public class ComponentsTest
{

    public static void testDissembleReassemble(BaseComponent[] components)
    {
        String json = ComponentSerializer.toString( components );
        BaseComponent[] parsed = ComponentSerializer.parse( json );
        Assert.assertEquals( TextComponent.toLegacyText( parsed ), TextComponent.toLegacyText( components ) );
    }

    public static void testDissembleReassemble(String json)
    {
        BaseComponent[] parsed = ComponentSerializer.parse( json );
        Assert.assertEquals( json, ComponentSerializer.toString( parsed ) );
    }

    @Test
    public void testItemParse()
    {
        // Declare all commonly used variables for reuse.
        BaseComponent[] components;
        TextComponent textComponent;
        String json;

        textComponent = new TextComponent( "Test" );
        textComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_ITEM, new BaseComponent[]
        {
            new TextComponent( "{id:\"minecraft:netherrack\",Count:47b}" )
        } ) );
        testDissembleReassemble( new BaseComponent[]
        {
            textComponent
        } );
        json = "{\"hoverEvent\":{\"action\":\"show_item\",\"value\":[{\"text\":\"{id:\\\"minecraft:netherrack\\\",Count:47b}\"}]},\"text\":\"Test\"}";
        testDissembleReassemble( json );
        //////////
        String hoverVal = "{\"text\":\"{id:\\\"minecraft:dirt\\\",Count:1b}\"}";
        json = "{\"extra\":[{\"text\":\"[\"},{\"extra\":[{\"translate\":\"block.minecraft.dirt\"}],\"text\":\"\"},{\"text\":\"]\"}],\"hoverEvent\":{\"action\":\"show_item\",\"value\":[" + hoverVal + "]},\"text\":\"\"}";
        components = ComponentSerializer.parse( json );
        Text contentText = ( (Text) components[0].getHoverEvent().getContents().get( 0 ) );
        Assert.assertEquals( hoverVal, ComponentSerializer.toString( (BaseComponent[]) contentText.getValue() ) );
        testDissembleReassemble( components );
        //////////
        TextComponent component1 = new TextComponent( "HoverableText" );
        String nbt = "{display:{Name:{text:Hello},Lore:[{text:Line_1},{text:Line_2}]},ench:[{id:49,lvl:5}],Unbreakable:1}}";
        Item contentItem = new Item( "minecraft:wood", 1, ItemTag.ofNbt( nbt ) );
        HoverEvent hoverEvent = new HoverEvent( HoverEvent.Action.SHOW_ITEM, contentItem );
        component1.setHoverEvent( hoverEvent );
        json = ComponentSerializer.toString( component1 );
        components = ComponentSerializer.parse( json );
        Item parsedContentItem = ( (Item) components[0].getHoverEvent().getContents().get( 0 ) );
        Assert.assertEquals( contentItem, parsedContentItem );
        Assert.assertEquals( contentItem.getCount(), parsedContentItem.getCount() );
        Assert.assertEquals( contentItem.getId(), parsedContentItem.getId() );
        Assert.assertEquals( nbt, parsedContentItem.getTag().getNbt() );
    }

    @Test
    public void testEmptyComponentBuilder()
    {
        ComponentBuilder builder = new ComponentBuilder();

        BaseComponent[] parts = builder.create();
        Assert.assertEquals( parts.length, 0 );

        for ( int i = 0; i < 3; i++ )
        {
            builder.append( "part:" + i );
            parts = builder.create();
            Assert.assertEquals( parts.length, i + 1 );
        }
    }

    @Test
    public void testDummyRetaining()
    {
        ComponentBuilder builder = new ComponentBuilder();
        Assert.assertNotNull( builder.getCurrentComponent() );
        builder.color( ChatColor.GREEN );
        builder.append( "test ", ComponentBuilder.FormatRetention.ALL );
        Assert.assertEquals( builder.getCurrentComponent().getColor(), ChatColor.GREEN );
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testComponentGettingExceptions()
    {
        ComponentBuilder builder = new ComponentBuilder();
        builder.getComponent( -1 );
        builder.getComponent( 0 );
        builder.getComponent( 1 );
        BaseComponent component = new TextComponent( "Hello" );
        builder.append( component );
        Assert.assertEquals( builder.getComponent( 0 ), component );
        builder.getComponent( 1 );
    }

    @Test
    public void testComponentParting()
    {
        ComponentBuilder builder = new ComponentBuilder();
        TextComponent apple = new TextComponent( "apple" );
        builder.append( apple );
        Assert.assertEquals( builder.getCurrentComponent(), apple );
        Assert.assertEquals( builder.getComponent( 0 ), apple );

        TextComponent mango = new TextComponent( "mango" );
        TextComponent orange = new TextComponent( "orange" );
        builder.append( mango );
        builder.append( orange );
        builder.removeComponent( 1 ); // Removing mango
        Assert.assertEquals( builder.getComponent( 0 ), apple );
        Assert.assertEquals( builder.getComponent( 1 ), orange );
    }

    @Test
    public void testToLegacyFromLegacy()
    {
        String text = "§a§lHello §f§kworld§7!";
        Assert.assertEquals( text, TextComponent.toLegacyText( TextComponent.fromLegacyText( text ) ) );
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testComponentBuilderCursorInvalidPos()
    {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append( new TextComponent( "Apple, " ) );
        builder.append( new TextComponent( "Orange, " ) );
        builder.setCursor( -1 );
        builder.setCursor( 2 );
    }

    @Test
    public void testComponentBuilderCursor()
    {
        TextComponent t1, t2, t3;
        ComponentBuilder builder = new ComponentBuilder();
        Assert.assertEquals( builder.getCursor(), -1 );
        builder.append( t1 = new TextComponent( "Apple, " ) );
        Assert.assertEquals( builder.getCursor(), 0 );
        builder.append( t2 = new TextComponent( "Orange, " ) );
        builder.append( t3 = new TextComponent( "Mango, " ) );
        Assert.assertEquals( builder.getCursor(), 2 );

        builder.setCursor( 0 );
        Assert.assertEquals( builder.getCurrentComponent(), t1 );

        // Test that appending new components updates the position to the new list size
        // after having previously set it to 0 (first component)
        builder.append( new TextComponent( "and Grapefruit" ) );
        Assert.assertEquals( builder.getCursor(), 3 );

        builder.setCursor( 0 );
        builder.resetCursor();
        Assert.assertEquals( builder.getCursor(), 3 );
    }

    @Test
    public void testLegacyComponentBuilderAppend()
    {
        String text = "§a§lHello §r§kworld§7!";
        BaseComponent[] components = TextComponent.fromLegacyText( text );
        BaseComponent[] builderComponents = new ComponentBuilder().append( components ).create();
        Assert.assertArrayEquals( components, builderComponents );
    }

    /*
    @Test
    public void testItemTag()
    {
        TextComponent component = new TextComponent( "Hello world" );
        HoverEvent.ContentItem content = new HoverEvent.ContentItem();
        content.setId( "minecraft:diamond_sword" );
        content.setCount( 1 );
        content.setTag( ItemTag.builder()
                .ench( new ItemTag.Enchantment( 5, 16 ) )
                .name( new TextComponent( "Sharp Sword" ) )
                .unbreakable( true )
                .lore( new ComponentBuilder( "Line1" ).create() )
                .lore( new ComponentBuilder( "Line2" ).create() )
                .build() );
        HoverEvent event = new HoverEvent( HoverEvent.Action.SHOW_ITEM, content );
        component.setHoverEvent( event );
        String serialised = ComponentSerializer.toString( component );
        BaseComponent[] deserialised = ComponentSerializer.parse( serialised );
        Assert.assertEquals( TextComponent.toLegacyText( deserialised ), TextComponent.toLegacyText( component ) );
    }
     */

    @Test
    public void testModernShowAdvancement()
    {
        String advancement = "achievement.openInventory";
        // First do the text using the newer contents system
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text( advancement )
        );
        TextComponent component = new TextComponent( "test" );
        component.setHoverEvent( hoverEvent );
        Assert.assertEquals( component.getHoverEvent().getContents().size(), 1 );
        Assert.assertTrue( component.getHoverEvent().getContents().get( 0 ) instanceof Text );
        Assert.assertEquals( ( (Text) component.getHoverEvent().getContents().get( 0 ) ).getValue(), advancement );
    }

    @Test
    public void testHoverEventContents()
    {
        // First do the text using the newer contents system
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text( new ComponentBuilder( "First" ).create() ),
                new Text( new ComponentBuilder( "Second" ).create() )
        );

        TextComponent component = new TextComponent( "Sample text" );
        component.setHoverEvent( hoverEvent );
        Assert.assertEquals( hoverEvent.getContents().size(), 2 );
        Assert.assertFalse( hoverEvent.isLegacy() );
        String serialized = ComponentSerializer.toString( component );
        BaseComponent[] deserialized = ComponentSerializer.parse( serialized );
        Assert.assertEquals( component.getHoverEvent(), deserialized[0].getHoverEvent() );

        // check the test still works with the value method
        hoverEvent = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Sample text" ).create() );
        Assert.assertEquals( hoverEvent.getContents().size(), 1 );
        Assert.assertTrue( hoverEvent.isLegacy() );
        serialized = ComponentSerializer.toString( component );
        deserialized = ComponentSerializer.parse( serialized );
        Assert.assertEquals( component.getHoverEvent(), deserialized[0].getHoverEvent() );

        // Test single content:
        String json = "{\"italic\":true,\"color\":\"gray\",\"translate\":\"chat.type.admin\",\"with\":[{\"text\":\"@\"}"
                + ",{\"translate\":\"commands.give.success.single\",\"with\":[\"1\",{\"color\":\"white\""
                + ",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:diamond_sword\",\"tag\":\""
                + "{Damage:0,display:{Lore:['\\\"test lore'!\\\"'],Name:'\\\"test\\\"'}}\"}},"
                + "\"extra\":[{\"italic\":true,\"extra\":[{\"text\":\"test\"}],\"text\":\"\"},{\"text\":\"]\"}],"
                + "\"text\":\"[\"},{\"insertion\":\"Name\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":"
                + "\"/tell Name \"},\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":"
                + "{\"type\":\"minecraft:player\",\"id\":\"00000000-0000-0000-0000-00000000000000\",\"name\":"
                + "{\"text\":\"Name\"}}},\"text\":\"Name\"}]}]}";
        testDissembleReassemble( ComponentSerializer.parse( json ) );
    }

    @Test
    public void testFormatRetentionCopyFormatting()
    {
        TextComponent first = new TextComponent( "Hello" );
        first.setBold( true );
        first.setColor( ChatColor.RED );
        first.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "test" ) );
        first.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Test" ).create() ) );

        TextComponent second = new TextComponent( " world" );
        second.copyFormatting( first, ComponentBuilder.FormatRetention.ALL, true );
        Assert.assertEquals( first.isBold(), second.isBold() );
        Assert.assertEquals( first.getColor(), second.getColor() );
        Assert.assertEquals( first.getClickEvent(), second.getClickEvent() );
        Assert.assertEquals( first.getHoverEvent(), second.getHoverEvent() );
    }

    @Test
    public void testBuilderClone()
    {
        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( ChatColor.RED ).append( "world" ).color( ChatColor.DARK_RED );
        ComponentBuilder cloned = new ComponentBuilder( builder );

        Assert.assertEquals( TextComponent.toLegacyText( builder.create() ), TextComponent.toLegacyText( cloned.create() ) );
    }

    @Test
    public void testBuilderAppendMixedComponents()
    {
        ComponentBuilder builder = new ComponentBuilder( "Hello " );
        TextComponent textComponent = new TextComponent( "world " );
        TranslatableComponent translatableComponent = new TranslatableComponent( "item.swordGold.name" );
        // array based BaseComponent append
        builder.append( new BaseComponent[]
        {
            textComponent,
            translatableComponent
        } );
        ScoreComponent scoreComponent = new ScoreComponent( "myscore", "myobjective" );
        builder.append( scoreComponent ); // non array based BaseComponent append
        BaseComponent[] components = builder.create();
        Assert.assertEquals( "Hello ", components[0].toPlainText() );
        Assert.assertEquals( textComponent.toPlainText(), components[1].toPlainText() );
        Assert.assertEquals( translatableComponent.toPlainText(), components[2].toPlainText() );
        Assert.assertEquals( scoreComponent.toPlainText(), components[3].toPlainText() );
    }

    @Test
    public void testScore()
    {
        BaseComponent[] component = ComponentSerializer.parse( "{\"score\":{\"name\":\"@p\",\"objective\":\"TEST\",\"value\":\"hello\"}}" );
        String text = ComponentSerializer.toString( component );
        BaseComponent[] reparsed = ComponentSerializer.parse( text );

        Assert.assertArrayEquals( component, reparsed );
    }

    @Test
    public void testBuilderAppend()
    {
        ClickEvent clickEvent = new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/help " );
        HoverEvent hoverEvent = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Hello world" ).create() );

        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( ChatColor.YELLOW );
        builder.append( new ComponentBuilder( "world!" ).color( ChatColor.GREEN ).event( hoverEvent ).event( clickEvent ).create() );

        BaseComponent[] components = builder.create();

        Assert.assertEquals( components[1].getHoverEvent(), hoverEvent );
        Assert.assertEquals( components[1].getClickEvent(), clickEvent );
        Assert.assertEquals( "Hello world!", BaseComponent.toPlainText( components ) );
        Assert.assertEquals( ChatColor.YELLOW + "Hello " + ChatColor.GREEN + "world!", BaseComponent.toLegacyText( components ) );
    }

    @Test
    public void testBuilderAppendLegacy()
    {
        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( ChatColor.YELLOW );
        builder.appendLegacy( "§aworld!" );

        BaseComponent[] components = builder.create();

        Assert.assertEquals( "Hello world!", BaseComponent.toPlainText( components ) );
        Assert.assertEquals( ChatColor.YELLOW + "Hello " + ChatColor.GREEN + "world!", BaseComponent.toLegacyText( components ) );
    }

    @Test
    public void testBasicComponent()
    {
        TextComponent textComponent = new TextComponent( "Hello world" );
        textComponent.setColor( ChatColor.RED );

        Assert.assertEquals( "Hello world", textComponent.toPlainText() );
        Assert.assertEquals( ChatColor.RED + "Hello world", textComponent.toLegacyText() );
    }

    @Test
    public void testLegacyConverter()
    {
        BaseComponent[] test1 = TextComponent.fromLegacyText( ChatColor.AQUA + "Aqua " + ChatColor.RED + ChatColor.BOLD + "RedBold" );

        Assert.assertEquals( "Aqua RedBold", BaseComponent.toPlainText( test1 ) );
        Assert.assertEquals( ChatColor.AQUA + "Aqua " + ChatColor.RED + ChatColor.BOLD + "RedBold", BaseComponent.toLegacyText( test1 ) );

        BaseComponent[] test2 = TextComponent.fromLegacyText( "Text http://spigotmc.org " + ChatColor.GREEN + "google.com/test" );

        Assert.assertEquals( "Text http://spigotmc.org google.com/test", BaseComponent.toPlainText( test2 ) );
        //The extra ChatColor instances are sometimes inserted when not needed but it doesn't change the result
        Assert.assertEquals( ChatColor.WHITE + "Text " + ChatColor.WHITE + "http://spigotmc.org" + ChatColor.WHITE
                + " " + ChatColor.GREEN + "google.com/test" + ChatColor.GREEN, BaseComponent.toLegacyText( test2 ) );

        ClickEvent url1 = test2[1].getClickEvent();
        Assert.assertNotNull( url1 );
        Assert.assertTrue( url1.getAction() == ClickEvent.Action.OPEN_URL );
        Assert.assertEquals( "http://spigotmc.org", url1.getValue() );

        ClickEvent url2 = test2[3].getClickEvent();
        Assert.assertNotNull( url2 );
        Assert.assertTrue( url2.getAction() == ClickEvent.Action.OPEN_URL );
        Assert.assertEquals( "http://google.com/test", url2.getValue() );
    }

    @Test
    public void testTranslateComponent()
    {
        TranslatableComponent item = new TranslatableComponent( "item.swordGold.name" );
        item.setColor( ChatColor.AQUA );
        TranslatableComponent translatableComponent = new TranslatableComponent( "commands.give.success",
                item, "5",
                "thinkofdeath" );

        Assert.assertEquals( "Given Golden Sword * 5 to thinkofdeath", translatableComponent.toPlainText() );
        Assert.assertEquals( ChatColor.WHITE + "Given " + ChatColor.AQUA + "Golden Sword" + ChatColor.WHITE
                + " * " + ChatColor.WHITE + "5" + ChatColor.WHITE + " to " + ChatColor.WHITE + "thinkofdeath",
                translatableComponent.toLegacyText() );

        TranslatableComponent positional = new TranslatableComponent( "book.pageIndicator", "5", "50" );

        Assert.assertEquals( "Page 5 of 50", positional.toPlainText() );
        Assert.assertEquals( ChatColor.WHITE + "Page " + ChatColor.WHITE + "5" + ChatColor.WHITE + " of " + ChatColor.WHITE + "50", positional.toLegacyText() );

        TranslatableComponent one_four_two = new TranslatableComponent( "filled_map.buried_treasure" );
        Assert.assertEquals( "Buried Treasure Map", one_four_two.toPlainText() );
    }

    @Test
    public void testBuilder()
    {
        BaseComponent[] components = new ComponentBuilder( "Hello " ).color( ChatColor.RED ).
                append( "World" ).bold( true ).color( ChatColor.BLUE ).
                append( "!" ).color( ChatColor.YELLOW ).create();

        Assert.assertEquals( "Hello World!", BaseComponent.toPlainText( components ) );
        Assert.assertEquals( ChatColor.RED + "Hello " + ChatColor.BLUE + ChatColor.BOLD
                + "World" + ChatColor.YELLOW + ChatColor.BOLD + "!", BaseComponent.toLegacyText( components ) );
    }

    @Test
    public void testBuilderReset()
    {
        BaseComponent[] components = new ComponentBuilder( "Hello " ).color( ChatColor.RED )
                .append( "World" ).reset().create();

        Assert.assertEquals( components[0].getColor(), ChatColor.RED );
        Assert.assertEquals( components[1].getColor(), ChatColor.WHITE );
    }

    @Test
    public void testBuilderFormatRetention()
    {
        BaseComponent[] noneRetention = new ComponentBuilder( "Hello " ).color( ChatColor.RED )
                .append( "World", ComponentBuilder.FormatRetention.NONE ).create();

        Assert.assertEquals( noneRetention[0].getColor(), ChatColor.RED );
        Assert.assertEquals( noneRetention[1].getColor(), ChatColor.WHITE );

        HoverEvent testEvent = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "test" ).create() );

        BaseComponent[] formattingRetention = new ComponentBuilder( "Hello " ).color( ChatColor.RED )
                .event( testEvent ).append( "World", ComponentBuilder.FormatRetention.FORMATTING ).create();

        Assert.assertEquals( formattingRetention[0].getColor(), ChatColor.RED );
        Assert.assertEquals( formattingRetention[0].getHoverEvent(), testEvent );
        Assert.assertEquals( formattingRetention[1].getColor(), ChatColor.RED );
        Assert.assertNull( formattingRetention[1].getHoverEvent() );

        ClickEvent testClickEvent = new ClickEvent( ClickEvent.Action.OPEN_URL, "http://www.example.com" );

        BaseComponent[] eventRetention = new ComponentBuilder( "Hello " ).color( ChatColor.RED )
                .event( testEvent ).event( testClickEvent ).append( "World", ComponentBuilder.FormatRetention.EVENTS ).create();

        Assert.assertEquals( eventRetention[0].getColor(), ChatColor.RED );
        Assert.assertEquals( eventRetention[0].getHoverEvent(), testEvent );
        Assert.assertEquals( eventRetention[0].getClickEvent(), testClickEvent );
        Assert.assertEquals( eventRetention[1].getColor(), ChatColor.WHITE );
        Assert.assertEquals( eventRetention[1].getHoverEvent(), testEvent );
        Assert.assertEquals( eventRetention[1].getClickEvent(), testClickEvent );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoopSimple()
    {
        TextComponent component = new TextComponent( "Testing" );
        component.addExtra( component );
        ComponentSerializer.toString( component );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoopComplex()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( ChatColor.AQUA );
        TextComponent c = new TextComponent( "C" );
        c.setColor( ChatColor.RED );
        a.addExtra( b );
        b.addExtra( c );
        c.addExtra( a );
        ComponentSerializer.toString( a );
    }

    @Test
    public void testRepeated()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( ChatColor.AQUA );
        a.addExtra( b );
        a.addExtra( b );
        ComponentSerializer.toString( a );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRepeatedError()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( ChatColor.AQUA );
        TextComponent c = new TextComponent( "C" );
        c.setColor( ChatColor.RED );
        a.addExtra( b );
        a.addExtra( c );
        c.addExtra( a );
        a.addExtra( b );
        ComponentSerializer.toString( a );
    }

    @Test
    public void testInvalidColorCodes()
    {
        StringBuilder allInvalidColorCodes = new StringBuilder();

        // collect all invalid color codes (e.g. §z, §g, ...)
        for ( char alphChar : "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray() )
        {
            if ( ChatColor.ALL_CODES.indexOf( alphChar ) == -1 )
            {
                allInvalidColorCodes.append( ChatColor.COLOR_CHAR );
                allInvalidColorCodes.append( alphChar );
            }
        }

        // last char is a single '§'
        allInvalidColorCodes.append( ChatColor.COLOR_CHAR );

        String invalidColorCodesLegacyText = fromAndToLegacyText( allInvalidColorCodes.toString() );
        String emptyLegacyText = fromAndToLegacyText( "" );

        // all invalid color codes and the trailing '§' should be ignored
        Assert.assertEquals( emptyLegacyText, invalidColorCodesLegacyText );
    }

    @Test
    public void testFormattingOnlyTextConversion()
    {
        String text = "§a";

        BaseComponent[] converted = TextComponent.fromLegacyText( text );
        Assert.assertEquals( ChatColor.GREEN, converted[0].getColor() );

        String roundtripLegacyText = BaseComponent.toLegacyText( converted );

        // color code should not be lost during conversion
        Assert.assertEquals( text, roundtripLegacyText );
    }

    @Test
    public void testEquals()
    {
        TextComponent first = new TextComponent( "Hello, " );
        first.addExtra( new TextComponent( "World!" ) );

        TextComponent second = new TextComponent( "Hello, " );
        second.addExtra( new TextComponent( "World!" ) );

        Assert.assertEquals( first, second );
    }

    @Test
    public void testNotEquals()
    {
        TextComponent first = new TextComponent( "Hello, " );
        first.addExtra( new TextComponent( "World." ) );

        TextComponent second = new TextComponent( "Hello, " );
        second.addExtra( new TextComponent( "World!" ) );

        Assert.assertNotEquals( first, second );
    }

    @Test
    public void testLegacyHack()
    {
        BaseComponent[] hexColored = new ComponentBuilder().color( ChatColor.of( Color.GRAY ) ).append( "Test" ).create();
        String legacy = TextComponent.toLegacyText( hexColored );

        BaseComponent[] reColored = TextComponent.fromLegacyText( legacy );

        Assert.assertArrayEquals( hexColored, reColored );
    }

    /**
     * In legacy chat, colors and reset both reset all formatting.
     * Make sure it works in combination with ComponentBuilder.
     */
    @Test
    public void testLegacyResetInBuilder()
    {
        ComponentBuilder builder = new ComponentBuilder();
        BaseComponent[] a = TextComponent.fromLegacyText( "§4§n44444§rdd§6§l6666" );

        String expected = "{\"extra\":[{\"underlined\":true,\"color\":\"dark_red\",\"text\":\"44444\"},{\"color\":"
                + "\"white\",\"text\":\"dd\"},{\"bold\":true,\"color\":\"gold\",\"text\":\"6666\"}],\"text\":\"\"}";
        Assert.assertEquals( expected, ComponentSerializer.toString( a ) );

        builder.append( a );

        String test1 = ComponentSerializer.toString( builder.create() );
        Assert.assertEquals( expected, test1 );

        BaseComponent[] b = TextComponent.fromLegacyText( "§rrrrr" );
        builder.append( b );

        String test2 = ComponentSerializer.toString( builder.create() );
        Assert.assertEquals(
                "{\"extra\":[{\"underlined\":true,\"color\":\"dark_red\",\"text\":\"44444\"},"
                        + "{\"color\":\"white\",\"text\":\"dd\"},{\"bold\":true,\"color\":\"gold\",\"text\":\"6666\"},"
                        + "{\"color\":\"white\",\"text\":\"rrrr\"}],\"text\":\"\"}",
                test2 );
    }

    private static String fromAndToLegacyText(String legacyText)
    {
        return BaseComponent.toLegacyText( TextComponent.fromLegacyText( legacyText ) );
    }
}
