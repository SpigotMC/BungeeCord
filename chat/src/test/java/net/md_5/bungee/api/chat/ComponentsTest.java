package net.md_5.bungee.api.chat;

import static net.md_5.bungee.api.ChatColor.*;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Test;

public class ComponentsTest
{

    public static void testDissembleReassemble(BaseComponent[] components)
    {
        String json = ComponentSerializer.toString( components );
        BaseComponent[] parsed = ComponentSerializer.parse( json );
        assertEquals( BaseComponent.toLegacyText( parsed ), BaseComponent.toLegacyText( components ) );
    }

    public static void testDissembleReassemble(BaseComponent component)
    {
        String json = ComponentSerializer.toString( component );
        BaseComponent[] parsed = ComponentSerializer.parse( json );
        assertEquals( BaseComponent.toLegacyText( parsed ), BaseComponent.toLegacyText( component ) );
    }

    public static void testAssembleDissemble(String json, boolean modern)
    {
        if ( modern )
        {
            BaseComponent deserialized = ComponentSerializer.deserialize( json );
            assertEquals( json, ComponentSerializer.toString( deserialized ) );
        } else
        {
            BaseComponent[] parsed = ComponentSerializer.parse( json );
            assertEquals( json, ComponentSerializer.toString( parsed ) );
        }
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
        testDissembleReassemble( textComponent );
        json = "{\"hoverEvent\":{\"action\":\"show_item\",\"value\":[{\"text\":\"{id:\\\"minecraft:netherrack\\\",Count:47b}\"}]},\"text\":\"Test\"}";
        testAssembleDissemble( json, false );
        testAssembleDissemble( json, true );
        //////////
        String hoverVal = "{\"text\":\"{id:\\\"minecraft:dirt\\\",Count:1b}\"}";
        json = "{\"extra\":[{\"text\":\"[\"},{\"extra\":[{\"translate\":\"block.minecraft.dirt\"}],\"text\":\"\"},{\"text\":\"]\"}],\"hoverEvent\":{\"action\":\"show_item\",\"value\":[" + hoverVal + "]},\"text\":\"\"}";
        components = ComponentSerializer.parse( json );
        Text contentText = ( (Text) components[0].getHoverEvent().getContents().get( 0 ) );
        assertEquals( hoverVal, ComponentSerializer.toString( (BaseComponent[]) contentText.getValue() ) );
        testDissembleReassemble( components );
        //////////
        // TODO: now ambiguous since "text" to distinguish Text from Item is not required
        /*
        TextComponent component1 = new TextComponent( "HoverableText" );
        String nbt = "{display:{Name:{text:Hello},Lore:[{text:Line_1},{text:Line_2}]},ench:[{id:49,lvl:5}],Unbreakable:1}}";
        Item contentItem = new Item( "minecraft:wood", 1, ItemTag.ofNbt( nbt ) );
        HoverEvent hoverEvent = new HoverEvent( HoverEvent.Action.SHOW_ITEM, contentItem );
        component1.setHoverEvent( hoverEvent );
        json = ComponentSerializer.toString( component1 );
        components = ComponentSerializer.parse( json );
        Item parsedContentItem = ( (Item) components[0].getHoverEvent().getContents().get( 0 ) );
        assertEquals( contentItem, parsedContentItem );
        assertEquals( contentItem.getCount(), parsedContentItem.getCount() );
        assertEquals( contentItem.getId(), parsedContentItem.getId() );
        assertEquals( nbt, parsedContentItem.getTag().getNbt() );
         */
    }

    @Test
    public void testArrayUUIDParse()
    {
        BaseComponent[] uuidComponent = ComponentSerializer.parse( "{\"translate\":\"multiplayer.player.joined\",\"with\":[{\"text\":\"Rexcantor64\",\"hoverEvent\":{\"contents\":{\"type\":\"minecraft:player\",\"id\":[1328556382,-2138814985,-1895806765,-1039963041],\"name\":\"Rexcantor64\"},\"action\":\"show_entity\"},\"insertion\":\"Rexcantor64\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell Rexcantor64 \"}}],\"color\":\"yellow\"}" );
        assertEquals( "4f30295e-8084-45f7-8f00-48d3c2036c5f", ( (Entity) ( (TranslatableComponent) uuidComponent[0] ).getWith().get( 0 ).getHoverEvent().getContents().get( 0 ) ).getId() );
        testDissembleReassemble( uuidComponent );
    }

    @Test
    public void testEmptyComponentBuilderCreate()
    {
        testEmptyComponentBuilder(
                ComponentBuilder::create,
                (components) -> assertEquals( components.length, 0 ),
                (components, size) -> assertEquals( size, components.length )
        );
    }

    @Test
    public void testEmptyComponentBuilderBuild()
    {
        testEmptyComponentBuilder(
                ComponentBuilder::build,
                (component) -> assertNull( component.getExtra() ),
                (component, size) -> assertEquals( component.getExtra().size(), size )
        );
    }

    private static <T> void testEmptyComponentBuilder(Function<ComponentBuilder, T> componentBuilder, Consumer<T> emptyAssertion, ObjIntConsumer<T> sizedAssertion)
    {
        ComponentBuilder builder = new ComponentBuilder();

        T component = componentBuilder.apply( builder );
        emptyAssertion.accept( component );

        for ( int i = 0; i < 3; i++ )
        {
            builder.append( "part:" + i );
            component = componentBuilder.apply( builder );
            sizedAssertion.accept( component, i + 1 );
        }
    }

    @Test
    public void testDummyRetaining()
    {
        ComponentBuilder builder = new ComponentBuilder();
        assertNotNull( builder.getCurrentComponent() );
        builder.color( GREEN );
        builder.append( "test ", ComponentBuilder.FormatRetention.ALL );
        assertEquals( builder.getCurrentComponent().getColor(), GREEN );
    }

    @Test
    public void testComponentGettingExceptions()
    {
        ComponentBuilder builder = new ComponentBuilder();
        assertThrows( IndexOutOfBoundsException.class, () -> builder.getComponent( -1 ) );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.getComponent( 0 ) );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.getComponent( 1 ) );
        BaseComponent component = new TextComponent( "Hello" );
        builder.append( component );
        assertEquals( builder.getComponent( 0 ), component );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.getComponent( 1 ) );
    }

    @Test
    public void testFormatNotColor()
    {
        BaseComponent[] component = new ComponentBuilder().color( BOLD ).append( "Test" ).create();

        String json = ComponentSerializer.toString( component );
        BaseComponent[] parsed = ComponentSerializer.parse( json );

        assertNull( parsed[0].getColorRaw(), "Format should not be preserved as color" );
    }

    @Test
    public void testComponentParting()
    {
        ComponentBuilder builder = new ComponentBuilder();
        TextComponent apple = new TextComponent( "apple" );
        builder.append( apple );
        assertEquals( builder.getCurrentComponent(), apple );
        assertEquals( builder.getComponent( 0 ), apple );

        TextComponent mango = new TextComponent( "mango" );
        TextComponent orange = new TextComponent( "orange" );
        builder.append( mango );
        builder.append( orange );
        builder.removeComponent( 1 ); // Removing mango
        assertEquals( builder.getComponent( 0 ), apple );
        assertEquals( builder.getComponent( 1 ), orange );
    }

    @Test
    public void testToLegacyFromLegacy()
    {
        String text = "" + GREEN + BOLD + "Hello " + WHITE + MAGIC + "world" + GRAY + "!";
        assertEquals( text, BaseComponent.toLegacyText( TextComponent.fromLegacyText( text ) ) );
    }

    @Test
    public void testComponentBuilderCursorInvalidPos()
    {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append( new TextComponent( "Apple, " ) );
        builder.append( new TextComponent( "Orange, " ) );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.setCursor( -1 ) );
        assertThrows( IndexOutOfBoundsException.class, () -> builder.setCursor( 2 ) );
    }

    @Test
    public void testComponentBuilderCursor()
    {
        TextComponent t1, t2, t3;
        ComponentBuilder builder = new ComponentBuilder();
        assertEquals( builder.getCursor(), -1 );
        builder.append( t1 = new TextComponent( "Apple, " ) );
        assertEquals( builder.getCursor(), 0 );
        builder.append( t2 = new TextComponent( "Orange, " ) );
        builder.append( t3 = new TextComponent( "Mango, " ) );
        assertEquals( builder.getCursor(), 2 );

        builder.setCursor( 0 );
        assertEquals( builder.getCurrentComponent(), t1 );

        // Test that appending new components updates the position to the new list size
        // after having previously set it to 0 (first component)
        builder.append( new TextComponent( "and Grapefruit" ) );
        assertEquals( builder.getCursor(), 3 );

        builder.setCursor( 0 );
        builder.resetCursor();
        assertEquals( builder.getCursor(), 3 );
    }

    @Test
    public void testLegacyComponentBuilderAppend()
    {
        String text = "" + GREEN + BOLD + "Hello " + RESET + MAGIC + "world" + GRAY + "!";
        BaseComponent[] components = TextComponent.fromLegacyText( text );
        BaseComponent[] builderComponents = new ComponentBuilder().append( components ).create();
        assertArrayEquals( components, builderComponents );
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
        assertEquals( TextComponent.toLegacyText( deserialised ), TextComponent.toLegacyText( component ) );
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
        assertEquals( component.getHoverEvent().getContents().size(), 1 );
        assertTrue( component.getHoverEvent().getContents().get( 0 ) instanceof Text );
        assertEquals( ( (Text) component.getHoverEvent().getContents().get( 0 ) ).getValue(), advancement );
    }

    @Test
    public void testHoverEventContentsCreate()
    {
        // First do the text using the newer contents system
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text( new ComponentBuilder( "First" ).create() ),
                new Text( new ComponentBuilder( "Second" ).create() )
        );

        this.testHoverEventContents(
                hoverEvent,
                ComponentSerializer::parse,
                (components) -> components[0].getHoverEvent(),
                ComponentsTest::testDissembleReassemble // BaseComponent
        );

        // check the test still works with the value method
        hoverEvent = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Sample text" ).create() );
        TextComponent component = new TextComponent( "Sample text" );
        component.setHoverEvent( hoverEvent );

        assertEquals( hoverEvent.getContents().size(), 1 );
        assertTrue( hoverEvent.isLegacy() );
        String serialized = ComponentSerializer.toString( component );
        BaseComponent[] deserialized = ComponentSerializer.parse( serialized );
        assertEquals( component.getHoverEvent(), deserialized[0].getHoverEvent() );
    }

    @Test
    public void testHoverEventContentsBuild()
    {
        // First do the text using the newer contents system
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text( new ComponentBuilder( "First" ).build() ),
                new Text( new ComponentBuilder( "Second" ).build() )
        );

        this.testHoverEventContents(
                hoverEvent,
                ComponentSerializer::deserialize,
                BaseComponent::getHoverEvent,
                ComponentsTest::testDissembleReassemble // BaseComponent
        );
    }

    private <T> void testHoverEventContents(HoverEvent hoverEvent, Function<String, T> deserializer, Function<T, HoverEvent> hoverEventGetter, Consumer<T> dissembleReassembleTest)
    {
        TextComponent component = new TextComponent( "Sample text" );
        component.setHoverEvent( hoverEvent );
        assertEquals( hoverEvent.getContents().size(), 2 );
        assertFalse( hoverEvent.isLegacy() );

        String serialized = ComponentSerializer.toString( component );
        T deserialized = deserializer.apply( serialized );
        assertEquals( component.getHoverEvent(), hoverEventGetter.apply( deserialized ) );

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
        dissembleReassembleTest.accept( deserializer.apply( json ) );
    }

    @Test
    public void testFormatRetentionCopyFormattingCreate()
    {
        testFormatRetentionCopyFormatting( () -> new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Test" ).create() ) );
    }

    @Test
    public void testFormatRetentionCopyFormattingBuild()
    {
        testFormatRetentionCopyFormatting( () -> new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( new ComponentBuilder( "Test" ).build() ) ) );
    }

    private static void testFormatRetentionCopyFormatting(Supplier<HoverEvent> hoverEventSupplier)
    {
        TextComponent first = new TextComponent( "Hello" );
        first.setBold( true );
        first.setColor( RED );
        first.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "test" ) );
        first.setHoverEvent( hoverEventSupplier.get() );

        TextComponent second = new TextComponent( " world" );
        second.copyFormatting( first, ComponentBuilder.FormatRetention.ALL, true );
        assertEquals( first.isBold(), second.isBold() );
        assertEquals( first.getColor(), second.getColor() );
        assertEquals( first.getClickEvent(), second.getClickEvent() );
        assertEquals( first.getHoverEvent(), second.getHoverEvent() );
    }

    @Test
    public void testBuilderCloneCreate()
    {
        testBuilderClone( (builder) -> BaseComponent.toLegacyText( builder.create() ) );
    }

    @Test
    public void testBuilderCloneBuild()
    {
        testBuilderClone( (builder) -> BaseComponent.toLegacyText( builder.build() ) );
    }

    private static void testBuilderClone(Function<ComponentBuilder, String> legacyTextFunction)
    {
        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( RED ).append( "world" ).color( DARK_RED );
        ComponentBuilder cloned = new ComponentBuilder( builder );

        assertEquals( legacyTextFunction.apply( builder ), legacyTextFunction.apply( cloned ) );
    }

    @Test
    public void testBuilderAppendCreateMixedComponents()
    {
        testBuilderAppendMixedComponents(
                ComponentBuilder::create,
                (components, index) -> components[index]
        );
    }

    @Test
    public void testBuilderAppendBuildMixedComponents()
    {
        testBuilderAppendMixedComponents(
                ComponentBuilder::build,
                (component, index) -> component.getExtra().get( index )
        );
    }

    private static <T> void testBuilderAppendMixedComponents(Function<ComponentBuilder, T> componentBuilder, BiFunction<T, Integer, BaseComponent> extraGetter)
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
        T component = componentBuilder.apply( builder );
        assertEquals( "Hello ", extraGetter.apply( component, 0 ).toPlainText() );
        assertEquals( textComponent.toPlainText(), extraGetter.apply( component, 1 ).toPlainText() );
        assertEquals( translatableComponent.toPlainText(), extraGetter.apply( component, 2 ).toPlainText() );
        assertEquals( scoreComponent.toPlainText(), extraGetter.apply( component, 3 ).toPlainText() );
    }

    @Test
    public void testScore()
    {
        BaseComponent[] component = ComponentSerializer.parse( "{\"score\":{\"name\":\"@p\",\"objective\":\"TEST\",\"value\":\"hello\"}}" );
        String text = ComponentSerializer.toString( component );
        BaseComponent[] reparsed = ComponentSerializer.parse( text );

        assertArrayEquals( component, reparsed );
    }

    @Test
    public void testStyle()
    {
        ComponentStyle style = ComponentSerializer.deserializeStyle( "{\"color\":\"red\",\"font\":\"minecraft:example\",\"bold\":true,\"italic\":false,\"obfuscated\":true}" );
        String text = ComponentSerializer.toString( style );
        ComponentStyle reparsed = ComponentSerializer.deserializeStyle( text );

        assertEquals( style, reparsed );
    }

    @Test
    public void testBuilderAppendCreate()
    {
        testBuilderAppend(
                () -> new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Hello world" ).create() ),
                ComponentBuilder::create,
                (components, index) -> components[index],
                BaseComponent::toPlainText,
                YELLOW + "Hello " + GREEN + "world!",
                BaseComponent::toLegacyText
        );
    }

    @Test
    public void testBuilderAppendBuild()
    {
        testBuilderAppend(
                () -> new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( new ComponentBuilder( "Hello world" ).build() ) ),
                ComponentBuilder::build,
                (component, index) -> component.getExtra().get( index ),
                (component) -> BaseComponent.toPlainText( component ),
                // An extra format code is appended to the beginning because there is an empty TextComponent at the start of every component
                WHITE.toString() + YELLOW + "Hello " + GREEN + "world!",
                (component) -> BaseComponent.toLegacyText( component )
        );
    }

    private static <T> void testBuilderAppend(Supplier<HoverEvent> hoverEventSupplier, Function<ComponentBuilder, T> componentBuilder, BiFunction<T, Integer, BaseComponent> extraGetter, Function<T, String> toPlainTextFunction, String expectedLegacyText, Function<T, String> toLegacyTextFunction)
    {
        ClickEvent clickEvent = new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/help " );
        HoverEvent hoverEvent = hoverEventSupplier.get();

        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( YELLOW );
        builder.append( new ComponentBuilder( "world!" ).color( GREEN ).event( hoverEvent ).event( clickEvent ).create() ); // Intentionally using create() to append multiple individual components

        T component = componentBuilder.apply( builder );

        assertEquals( extraGetter.apply( component, 1 ).getHoverEvent(), hoverEvent );
        assertEquals( extraGetter.apply( component, 1 ).getClickEvent(), clickEvent );
        assertEquals( "Hello world!", toPlainTextFunction.apply( component ) );
        assertEquals( expectedLegacyText, toLegacyTextFunction.apply( component ) );
    }

    @Test
    public void testBuilderAppendLegacyCreate()
    {
        testBuilderAppendLegacy(
                ComponentBuilder::create,
                BaseComponent::toPlainText,
                YELLOW + "Hello " + GREEN + "world!",
                BaseComponent::toLegacyText
        );
    }

    @Test
    public void testBuilderAppendLegacyBuild()
    {
        testBuilderAppendLegacy(
                ComponentBuilder::build,
                (component) -> BaseComponent.toPlainText( component ),
                // An extra format code is appended to the beginning because there is an empty TextComponent at the start of every component
                WHITE.toString() + YELLOW + "Hello " + GREEN + "world!",
                (component) -> BaseComponent.toLegacyText( component )
        );
    }

    private static <T> void testBuilderAppendLegacy(Function<ComponentBuilder, T> componentBuilder, Function<T, String> toPlainTextFunction, String expectedLegacyString, Function<T, String> toLegacyTextFunction)
    {
        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( YELLOW );
        builder.appendLegacy( GREEN + "world!" );

        T component = componentBuilder.apply( builder );

        assertEquals( "Hello world!", toPlainTextFunction.apply( component ) );
        assertEquals( expectedLegacyString, toLegacyTextFunction.apply( component ) );
    }

    @Test
    public void testBasicComponent()
    {
        TextComponent textComponent = new TextComponent( "Hello world" );
        textComponent.setColor( RED );

        assertEquals( "Hello world", textComponent.toPlainText() );
        assertEquals( RED + "Hello world", textComponent.toLegacyText() );
    }

    @Test
    public void testLegacyConverter()
    {
        BaseComponent[] test1 = TextComponent.fromLegacyText( AQUA + "Aqua " + RED + BOLD + "RedBold" );

        assertEquals( "Aqua RedBold", BaseComponent.toPlainText( test1 ) );
        assertEquals( AQUA + "Aqua " + RED + BOLD + "RedBold", BaseComponent.toLegacyText( test1 ) );

        BaseComponent[] test2 = TextComponent.fromLegacyText( "Text http://spigotmc.org " + GREEN + "google.com/test" );

        assertEquals( "Text http://spigotmc.org google.com/test", BaseComponent.toPlainText( test2 ) );
        //The extra ChatColor instances are sometimes inserted when not needed but it doesn't change the result
        assertEquals( WHITE + "Text " + WHITE + "http://spigotmc.org" + WHITE
                + " " + GREEN + "google.com/test" + GREEN, BaseComponent.toLegacyText( test2 ) );

        ClickEvent url1 = test2[1].getClickEvent();
        assertNotNull( url1 );
        assertTrue( url1.getAction() == ClickEvent.Action.OPEN_URL );
        assertEquals( "http://spigotmc.org", url1.getValue() );

        ClickEvent url2 = test2[3].getClickEvent();
        assertNotNull( url2 );
        assertTrue( url2.getAction() == ClickEvent.Action.OPEN_URL );
        assertEquals( "http://google.com/test", url2.getValue() );
    }

    @Test
    public void testBuilderCreate()
    {
        testBuilder(
                ComponentBuilder::create,
                BaseComponent::toPlainText,
                RED + "Hello " + BLUE + BOLD + "World" + YELLOW + BOLD + "!",
                BaseComponent::toLegacyText
        );
    }

    @Test
    public void testBuilderBuild()
    {
        testBuilder(
                ComponentBuilder::build,
                (component) -> BaseComponent.toPlainText( component ),
                // An extra format code is appended to the beginning because there is an empty TextComponent at the start of every component
                WHITE.toString() + RED + "Hello " + BLUE + BOLD + "World" + YELLOW + BOLD + "!",
                (component) -> BaseComponent.toLegacyText( component )
        );
    }

    private static <T> void testBuilder(Function<ComponentBuilder, T> componentBuilder, Function<T, String> toPlainTextFunction, String expectedLegacyString, Function<T, String> toLegacyTextFunction)
    {
        T component = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED ).
                append( "World" ).bold( true ).color( BLUE ).
                append( "!" ).color( YELLOW ) );

        assertEquals( "Hello World!", toPlainTextFunction.apply( component ) );
        assertEquals( expectedLegacyString, toLegacyTextFunction.apply( component ) );
    }

    @Test
    public void testBuilderCreateReset()
    {
        testBuilderReset(
                ComponentBuilder::create,
                (components, index) -> components[index]
        );
    }

    @Test
    public void testBuilderBuildReset()
    {
        testBuilderReset(
                ComponentBuilder::build,
                (component, index) -> component.getExtra().get( index )
        );
    }

    private static <T> void testBuilderReset(Function<ComponentBuilder, T> componentBuilder, BiFunction<T, Integer, BaseComponent> extraGetter)
    {
        T component = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED )
                .append( "World" ).reset() );

        assertEquals( RED, extraGetter.apply( component, 0 ).getColor() );
        assertEquals( WHITE, extraGetter.apply( component, 1 ).getColor() );
    }

    @Test
    public void testBuilderCreateFormatRetention()
    {
        testBuilderFormatRetention(
                ComponentBuilder::create,
                (components, index) -> components[index]
        );
    }

    @Test
    public void testBuilderBuildFormatRetention()
    {
        testBuilderFormatRetention(
                ComponentBuilder::build,
                (component, index) -> component.getExtra().get( index )
        );
    }

    private static <T> void testBuilderFormatRetention(Function<ComponentBuilder, T> componentBuilder, BiFunction<T, Integer, BaseComponent> extraGetter)
    {
        T noneRetention = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED )
                .append( "World", ComponentBuilder.FormatRetention.NONE ) );

        assertEquals( RED, extraGetter.apply( noneRetention, 0 ).getColor() );
        assertEquals( WHITE, extraGetter.apply( noneRetention, 1 ).getColor() );

        HoverEvent testEvent = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( new ComponentBuilder( "test" ).build() ) );

        T formattingRetention = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED )
                .event( testEvent ).append( "World", ComponentBuilder.FormatRetention.FORMATTING ) );

        assertEquals( RED, extraGetter.apply( formattingRetention, 0 ).getColor() );
        assertEquals( testEvent, extraGetter.apply( formattingRetention, 0 ).getHoverEvent() );
        assertEquals( RED, extraGetter.apply( formattingRetention, 1 ).getColor() );
        assertNull( extraGetter.apply( formattingRetention, 1 ).getHoverEvent() );

        ClickEvent testClickEvent = new ClickEvent( ClickEvent.Action.OPEN_URL, "http://www.example.com" );

        T eventRetention = componentBuilder.apply( new ComponentBuilder( "Hello " ).color( RED )
                .event( testEvent ).event( testClickEvent ).append( "World", ComponentBuilder.FormatRetention.EVENTS ) );

        assertEquals( RED, extraGetter.apply( eventRetention, 0 ).getColor() );
        assertEquals( testEvent, extraGetter.apply( eventRetention, 0 ).getHoverEvent() );
        assertEquals( testClickEvent, extraGetter.apply( eventRetention, 0 ).getClickEvent() );
        assertEquals( WHITE, extraGetter.apply( eventRetention, 1 ).getColor() );
        assertEquals( testEvent, extraGetter.apply( eventRetention, 1 ).getHoverEvent() );
        assertEquals( testClickEvent, extraGetter.apply( eventRetention, 1 ).getClickEvent() );
    }

    @Test
    public void testLoopSimple()
    {
        TextComponent component = new TextComponent( "Testing" );
        component.addExtra( component );
        assertThrows( IllegalArgumentException.class, () -> ComponentSerializer.toString( component ) );
    }

    @Test
    public void testLoopComplex()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( AQUA );
        TextComponent c = new TextComponent( "C" );
        c.setColor( RED );
        a.addExtra( b );
        b.addExtra( c );
        c.addExtra( a );
        assertThrows( IllegalArgumentException.class, () -> ComponentSerializer.toString( a ) );
    }

    @Test
    public void testRepeated()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( AQUA );
        a.addExtra( b );
        a.addExtra( b );
        ComponentSerializer.toString( a );
    }

    @Test
    public void testRepeatedError()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( AQUA );
        TextComponent c = new TextComponent( "C" );
        c.setColor( RED );
        a.addExtra( b );
        a.addExtra( c );
        c.addExtra( a );
        a.addExtra( b );
        assertThrows( IllegalArgumentException.class, () -> ComponentSerializer.toString( a ) );
    }

    @Test
    public void testInvalidColorCodes()
    {
        StringBuilder allInvalidColorCodes = new StringBuilder();

        // collect all invalid color codes (e.g. §z, §g, ...)
        for ( char alphChar : "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray() )
        {
            if ( ALL_CODES.indexOf( alphChar ) == -1 )
            {
                allInvalidColorCodes.append( COLOR_CHAR );
                allInvalidColorCodes.append( alphChar );
            }
        }

        // last char is a single '§'
        allInvalidColorCodes.append( COLOR_CHAR );

        String invalidColorCodesLegacyText = fromAndToLegacyText( allInvalidColorCodes.toString() );
        String emptyLegacyText = fromAndToLegacyText( "" );

        // all invalid color codes and the trailing '§' should be ignored
        assertEquals( emptyLegacyText, invalidColorCodesLegacyText );
    }

    @Test
    public void testFormattingOnlyTextConversion()
    {
        String text = "" + GREEN;

        BaseComponent[] converted = TextComponent.fromLegacyText( text );
        assertEquals( GREEN, converted[0].getColor() );

        String roundtripLegacyText = BaseComponent.toLegacyText( converted );

        // color code should not be lost during conversion
        assertEquals( text, roundtripLegacyText );
    }

    @Test
    public void testEquals()
    {
        TextComponent first = new TextComponent( "Hello, " );
        first.addExtra( new TextComponent( "World!" ) );

        TextComponent second = new TextComponent( "Hello, " );
        second.addExtra( new TextComponent( "World!" ) );

        assertEquals( first, second );
    }

    @Test
    public void testNotEquals()
    {
        TextComponent first = new TextComponent( "Hello, " );
        first.addExtra( new TextComponent( "World." ) );

        TextComponent second = new TextComponent( "Hello, " );
        second.addExtra( new TextComponent( "World!" ) );

        assertNotEquals( first, second );
    }

    @Test
    public void testLegacyHack()
    {
        BaseComponent[] hexColored = new ComponentBuilder().color( of( Color.GRAY ) ).append( "Test" ).create();
        String legacy = BaseComponent.toLegacyText( hexColored );

        BaseComponent[] reColored = TextComponent.fromLegacyText( legacy );

        assertArrayEquals( hexColored, reColored );
    }

    @Test
    public void testLegacyResetInBuilderCreate()
    {
        testLegacyResetInBuilder(
                ComponentBuilder::create,
                ComponentSerializer::toString
        );
    }

    @Test
    public void testLegacyResetInBuilderBuild()
    {
        testLegacyResetInBuilder(
                ComponentBuilder::build,
                ComponentSerializer::toString
        );
    }

    @Test
    public void testHasFormatting()
    {
        BaseComponent component = new TextComponent();
        assertFalse( component.hasFormatting() );

        component.setBold( true );
        assertTrue( component.hasFormatting() );
    }

    @Test
    public void testStyleIsEmpty()
    {
        ComponentStyle style = ComponentStyle.builder().build();
        assertTrue( style.isEmpty() );

        style = ComponentStyle.builder()
                .bold( true )
                .build();
        assertFalse( style.isEmpty() );
    }

    /*
     * In legacy chat, colors and reset both reset all formatting.
     * Make sure it works in combination with ComponentBuilder.
     */
    private static <T> void testLegacyResetInBuilder(Function<ComponentBuilder, T> componentBuilder, Function<T, String> componentSerializer)
    {
        ComponentBuilder builder = new ComponentBuilder();
        BaseComponent[] a = TextComponent.fromLegacyText( "" + DARK_RED + UNDERLINE + "44444" + RESET + "dd" + GOLD + BOLD + "6666" );

        String expected = "{\"extra\":[{\"underlined\":true,\"color\":\"dark_red\",\"text\":\"44444\"},{\"color\":"
                + "\"white\",\"text\":\"dd\"},{\"bold\":true,\"color\":\"gold\",\"text\":\"6666\"}],\"text\":\"\"}";
        assertEquals( expected, ComponentSerializer.toString( a ) );

        builder.append( a );

        String test1 = componentSerializer.apply( componentBuilder.apply( builder ) );
        assertEquals( expected, test1 );

        BaseComponent[] b = TextComponent.fromLegacyText( RESET + "rrrr" );
        builder.append( b );

        String test2 = componentSerializer.apply( componentBuilder.apply( builder ) );
        assertEquals(
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
