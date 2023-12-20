package net.md_5.bungee.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DefaultConfigurationTest
{

    @Test
    public void testDefaultValues()
    {
        Configuration defaultConfig = new Configuration();
        defaultConfig.set( "setting", 10 );
        defaultConfig.set( "nested.setting", 11 );
        defaultConfig.set( "double.nested.setting", 12 );

        Configuration actualConfig = new Configuration( defaultConfig );

        assertEquals( 10, actualConfig.getInt( "setting" ) );
        assertEquals( 11, actualConfig.getInt( "nested.setting" ) );
        assertEquals( 12, actualConfig.getInt( "double.nested.setting" ) );
    }
}
