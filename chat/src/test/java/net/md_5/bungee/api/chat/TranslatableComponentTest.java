package net.md_5.bungee.api.chat;

import org.junit.Assert;
import org.junit.Test;

public class TranslatableComponentTest
{

    @Test
    public void testMissingPlaceholdersAdded()
    {
        TranslatableComponent testComponent = new TranslatableComponent( "Test string with %s placeholders: %s", 2, "aoeu" );
        Assert.assertEquals( "Test string with 2 placeholders: aoeu", testComponent.toPlainText() );
        Assert.assertEquals( "§fTest string with §f2§f placeholders: §faoeu", testComponent.toLegacyText() );
    }
}
