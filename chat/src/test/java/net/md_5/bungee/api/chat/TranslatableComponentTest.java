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
        assertEquals( WHITE + "Test string with " + WHITE + "2" + WHITE + " placeholders: " + WHITE + "aoeu", testComponent.toLegacyText() );
    }

    @Test
    public void testJsonSerialisation()
    {
        TranslatableComponent testComponent = new TranslatableComponent( "Test string with %s placeholder", "a" );
        String jsonString = ComponentSerializer.toString( testComponent );
        BaseComponent[] baseComponents = ComponentSerializer.parse( jsonString );

        assertEquals( "Test string with a placeholder", BaseComponent.toPlainText( baseComponents ) );
        assertEquals( WHITE + "Test string with " + WHITE + "a" + WHITE + " placeholder", BaseComponent.toLegacyText( baseComponents ) );
    }

    @Test
    public void testTranslateComponent()
    {
        TranslatableComponent item = new TranslatableComponent( "item.swordGold.name" );
        item.setColor( AQUA );
        TranslatableComponent translatableComponent = new TranslatableComponent( "commands.give.success",
                item, "5", "thinkofdeath" );

        assertEquals( "Given Golden Sword * 5 to thinkofdeath", translatableComponent.toPlainText() );
        assertEquals( WHITE + "Given " + AQUA + "Golden Sword" + WHITE + " * " + WHITE + "5" + WHITE + " to " + WHITE + "thinkofdeath",
                translatableComponent.toLegacyText() );

        TranslatableComponent positional = new TranslatableComponent( "book.pageIndicator", "5", "50" );

        assertEquals( "Page 5 of 50", positional.toPlainText() );
        assertEquals( WHITE + "Page " + WHITE + "5" + WHITE + " of " + WHITE + "50", positional.toLegacyText() );

        TranslatableComponent one_four_two = new TranslatableComponent( "filled_map.buried_treasure" );
        assertEquals( "Buried Treasure Map", one_four_two.toPlainText() );
    }

}
