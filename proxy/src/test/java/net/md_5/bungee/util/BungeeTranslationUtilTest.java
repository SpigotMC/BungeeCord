package net.md_5.bungee.util;

import static org.junit.jupiter.api.Assertions.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Test;

public class BungeeTranslationUtilTest
{

    private static final String TEST_TRANSLATION = ChatColor.RED + "Kicked whilst connecting to " + ChatColor.BOLD + "{0}: {1}";

    @Test
    public void test()
    {
        BaseComponent component = BungeeTranslationUtil.getTranslationComponent0( TEST_TRANSLATION,
                new ComponentBuilder( "lobby" ).color( ChatColor.AQUA ).build(), "Test-Kick" );
        assertEquals( "{\"extra\":[{\"color\":\"red\",\"text\":\"Kicked whilst connecting to \"},"
                + "{\"bold\":true,\"color\":\"red\",\"text\":\"\"},"
                + "{\"bold\":true,\"color\":\"red\",\"extra\":[{\"color\":\"aqua\",\"text\":\"lobby\"}],\"text\":\"\"},"
                + "{\"bold\":true,\"color\":\"red\",\"text\":\": \"},"
                + "{\"bold\":true,\"color\":\"red\",\"text\":\"Test-Kick\"}],"
                + "\"text\":\"\"}", ComponentSerializer.toString( component ) );
    }

    @Test
    public void testLegacyArgumentReset()
    {
        BaseComponent component = BungeeTranslationUtil.getTranslationComponent0( TEST_TRANSLATION,
                TextComponent.fromLegacy( ChatColor.AQUA + "lobby" ), "Test-Kick" );
        assertEquals( "{\"extra\":[{\"color\":\"red\",\"text\":\"Kicked whilst connecting to \"},"
                + "{\"bold\":true,\"color\":\"red\",\"text\":\"\"},"
                + "{\"extra\":[{\"color\":\"aqua\",\"text\":\"lobby\"}],\"text\":\"\"}," // argument 0 not bold unlike above
                + "{\"bold\":true,\"color\":\"red\",\"text\":\": \"},"
                + "{\"bold\":true,\"color\":\"red\",\"text\":\"Test-Kick\"}],\""
                + "text\":\"\"}", ComponentSerializer.toString( component ) );
    }
}
