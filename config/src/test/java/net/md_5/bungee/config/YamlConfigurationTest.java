package net.md_5.bungee.config;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class YamlConfigurationTest
{

    private static final String TEST_DOCUMENT = ""
            + "receipt:     Oz-Ware Purchase Invoice\n"
            + "date:        2012-08-06\n"
            + "customer:\n"
            + "    given:   Dorothy\n"
            + "    family:  Gale\n"
            + "\n"
            + "items:\n"
            + "    - part_no:   A4786\n"
            + "      descrip:   Water Bucket (Filled)\n"
            + "      price:     1.47\n"
            + "      quantity:  4\n"
            + "\n"
            + "    - part_no:   E1628\n"
            + "      descrip:   High Heeled \"Ruby\" Slippers\n"
            + "      size:      8\n"
            + "      price:     100.27\n"
            + "      quantity:  1\n"
            + "\n"
            + "bill-to:  &id001\n"
            + "    street: |\n"
            + "            123 Tornado Alley\n"
            + "            Suite 16\n"
            + "    city:   East Centerville\n"
            + "    state:  KS\n"
            + "\n"
            + "ship-to:  *id001\n"
            + "\n"
            + "specialDelivery:  >\n"
            + "    Follow the Yellow Brick\n"
            + "    Road to the Emerald City.\n"
            + "    Pay no attention to the\n"
            + "    man behind the curtain.";
    private static final String NUMBER_TEST = ""
            + "someKey:\n"
            + "    1: 1\n"
            + "    2: 2\n"
            + "    3: 3\n"
            + "    4: 4";
    private static final String NULL_TEST = ""
            + "null:\n"
            + "    null: object\n"
            + "    object: null\n";

    @Test
    public void testConfig() throws Exception
    {
        Configuration conf = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( TEST_DOCUMENT );
        testSection( conf );

        StringWriter sw = new StringWriter();
        ConfigurationProvider.getProvider( YamlConfiguration.class ).save( conf, sw );

        // Check nulls were saved, see #1094
        Assert.assertFalse( "Config contains null", sw.toString().contains( "null" ) );

        conf = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( new StringReader( sw.toString() ) );
        conf.set( "receipt", "Oz-Ware Purchase Invoice" ); // Add it back
        testSection( conf );
    }

    private void testSection(Configuration conf)
    {
        Assert.assertEquals( "receipt", "Oz-Ware Purchase Invoice", conf.getString( "receipt" ) );
        // Assert.assertEquals( "date", "2012-08-06", conf.get( "date" ).toString() );

        Configuration customer = conf.getSection( "customer" );
        Assert.assertEquals( "customer.given", "Dorothy", customer.getString( "given" ) );
        Assert.assertEquals( "customer.given", "Dorothy", conf.getString( "customer.given" ) );

        List items = conf.getList( "items" );
        Map item = (Map) items.get( 0 );
        Assert.assertEquals( "items[0].part_no", "A4786", item.get( "part_no" ) );

        conf.set( "receipt", null );
        Assert.assertEquals( null, conf.get( "receipt" ) );
        Assert.assertEquals( "foo", conf.get( "receipt", "foo" ) );

        Configuration newSection = conf.getSection( "new.section" );
        newSection.set( "value", "foo" );
        Assert.assertEquals( "foo", conf.get( "new.section.value" ) );

        conf.set( "other.new.section", "bar" );
        Assert.assertEquals( "bar", conf.get( "other.new.section" ) );

        Assert.assertTrue( conf.contains( "customer.given" ) );
        Assert.assertTrue( customer.contains( "given" ) );

        Assert.assertFalse( conf.contains( "customer.foo" ) );
        Assert.assertFalse( customer.contains( "foo" ) );
    }

    @Test
    public void testNumberedKeys()
    {
        Configuration conf = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( NUMBER_TEST );

        Configuration section = conf.getSection( "someKey" );
        for ( String key : section.getKeys() )
        {
            // empty
        }
    }

    @Test
    public void testNull()
    {
        Configuration conf = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( NULL_TEST );

        Assert.assertEquals( "object", conf.get( "null.null" ) );
        Assert.assertEquals( "object", conf.getSection( "null" ).get( "null" ) );

        Assert.assertEquals( null, conf.get( "null.object" ) );
        Assert.assertEquals( "", conf.getString( "null.object" ) );
    }

    @Test
    public void testMapAddition()
    {
        Configuration conf = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( TEST_DOCUMENT );

        conf.set( "addition", Collections.singletonMap( "foo", "bar" ) );

        // Order matters
        Assert.assertEquals( "bar", conf.getSection( "addition" ).getString( "foo" ) );
        Assert.assertEquals( "bar", conf.getString( "addition.foo" ) );

        Assert.assertTrue( conf.get( "addition" ) instanceof Configuration );
    }
}
