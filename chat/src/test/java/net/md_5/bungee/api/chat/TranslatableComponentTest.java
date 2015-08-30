package net.md_5.bungee.api.chat;

import net.md_5.bungee.api.chat.TranslatableComponent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TranslatableComponentTest {
    @Test
    public void testMissingPlaceholdersAdded()
    {
        TranslatableComponent testComponent = new TranslatableComponent( "Test string with %s placeholders: %s", "2", "aoeu" );
        assertEquals( "Test string with 2 placeholders: aoeu", testComponent.toPlainText() );
        assertEquals( "§fTest string with §f2§f placeholders: §faoeu", testComponent.toLegacyText() );
    }
}
