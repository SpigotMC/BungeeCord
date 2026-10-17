package net.md_5.bungee.api.chat;

import static net.md_5.bungee.api.ChatColor.*;
import static org.junit.jupiter.api.Assertions.*;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Test;

public class TranslatableComponentTest
{

    @Test
    public void testMissingPlaceholdersAdded()
    {
        TranslatableComponent testComponent = new TranslatableComponent( "Test string with %s placeholders: %s", 2, "aoeu" );
        assertEquals( "Test string with 2 placeholders: aoeu", testComponent.toPlainText() );
        assertEquals( "Test string with 2 placeholders: aoeu", testComponent.toLegacyText() );
    }

    @Test
    public void testJsonSerialisation()
    {
        TranslatableComponent testComponent = new TranslatableComponent( "Test string with %s placeholder", "a" );
        String jsonString = ComponentSerializer.toString( testComponent );
        BaseComponent[] baseComponents = ComponentSerializer.parse( jsonString );

        assertEquals( "Test string with a placeholder", BaseComponent.toPlainText( baseComponents ) );
        assertEquals( "Test string with a placeholder", BaseComponent.toLegacyText( baseComponents ) );
    }

    @Test
    public void testTranslateComponent()
    {
        TranslatableComponent item = new TranslatableComponent( "item.swordGold.name" );
        TranslatableComponent translatableComponent = new TranslatableComponent( "commands.give.success",
                item, "5", "thinkofdeath" );

        assertEquals( "Given Golden Sword * 5 to thinkofdeath", translatableComponent.toPlainText() );

        translatableComponent.setColor( RED );
        assertEquals( RED + "Given Golden Sword * 5 to thinkofdeath", translatableComponent.toLegacyText() );

        item.setColor( AQUA );
        assertEquals( RED + "Given " + AQUA + "Golden Sword" + RED + " * 5 to thinkofdeath", translatableComponent.toLegacyText() );

        translatableComponent.setColor( null );
        assertEquals( "Given " + AQUA + "Golden Sword" + RESET + " * 5 to thinkofdeath", translatableComponent.toLegacyText() );

        BaseComponent legacyColorTest = new ComponentBuilder( "Test " ).color( RED ).append( translatableComponent ).build();
        assertEquals( RED + "Test Given " + AQUA + "Golden Sword" + RED + " * 5 to thinkofdeath", legacyColorTest.toLegacyText() );

        BaseComponent legacyColorTest2 = new TextComponent( "Test " );
        legacyColorTest2.addExtra( new ComponentBuilder( "abc " ).color( GRAY ).build() );
        legacyColorTest2.addExtra( translatableComponent );
        legacyColorTest2.addExtra( new ComponentBuilder( " def" ).build() );
        assertEquals( "Test " + GRAY + "abc " + RED + "Given " + AQUA + "Golden Sword" + RED + " * 5 to thinkofdeath"
                + RESET + " def", legacyColorTest2.toLegacyText() );

        legacyColorTest2.setColor( RED );
        assertEquals( RED + "Test " + GRAY + "abc " + RED + "Given " + AQUA + "Golden Sword" + RED
                + " * 5 to thinkofdeath def", legacyColorTest2.toLegacyText() );

        TranslatableComponent positional = new TranslatableComponent( "book.pageIndicator", "5", "50" );

        assertEquals( "Page 5 of 50", positional.toPlainText() );
        assertEquals( "Page 5 of 50", positional.toLegacyText() );

        TranslatableComponent one_four_two = new TranslatableComponent( "filled_map.buried_treasure" );
        assertEquals( "Buried Treasure Map", one_four_two.toPlainText() );
    }
}
