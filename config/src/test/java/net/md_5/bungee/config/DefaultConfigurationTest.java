package net.md_5.bungee.config;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals( 10, actualConfig.getInt( "setting" ) );
        Assert.assertEquals( 11, actualConfig.getInt( "nested.setting" ) );
        Assert.assertEquals( 12, actualConfig.getInt( "double.nested.setting" ) );
    }
}
