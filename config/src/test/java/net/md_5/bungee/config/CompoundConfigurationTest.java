package net.md_5.bungee.config;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RequiredArgsConstructor
@RunWith(Parameterized.class)
public class CompoundConfigurationTest
{

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data()
    {
        // CHECKSTYLE:OFF
        return Arrays.asList( new Object[][]
        {
            {
                // provider
                YamlConfiguration.class,
                // testDocument
                ""
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
                + "    man behind the curtain.",
                // numberTest
                ""
                + "someKey:\n"
                + "    1: 1\n"
                + "    2: 2\n"
                + "    3: 3\n"
                + "    4: 4",
                // nullTest
                ""
                + "null:\n"
                + "    null: object\n"
                + "    object: null\n"
            },
            {
                // provider
                JsonConfiguration.class,
                // testDocument
                ""
                + "{\n"
                + "  \"customer\": {\n"
                + "    \"given\": \"Dorothy\", \n"
                + "    \"family\": \"Gale\"\n"
                + "  }, \n"
                + "  \"ship-to\": {\n"
                + "    \"city\": \"East Centerville\", \n"
                + "    \"state\": \"KS\", \n"
                + "    \"street\": \"123 Tornado Alley\\nSuite 16\\n\"\n"
                + "  }, \n"
                + "  \"bill-to\": {\n"
                + "    \"city\": \"East Centerville\", \n"
                + "    \"state\": \"KS\", \n"
                + "    \"street\": \"123 Tornado Alley\\nSuite 16\\n\"\n"
                + "  }, \n"
                + "  \"date\": \"2012-08-06\", \n"
                + "  \"items\": [\n"
                + "    {\n"
                + "      \"part_no\": \"A4786\", \n"
                + "      \"price\": 1.47, \n"
                + "      \"descrip\": \"Water Bucket (Filled)\", \n"
                + "      \"quantity\": 4\n"
                + "    }, \n"
                + "    {\n"
                + "      \"part_no\": \"E1628\", \n"
                + "      \"descrip\": \"High Heeled \\\"Ruby\\\" Slippers\", \n"
                + "      \"price\": 100.27, \n"
                + "      \"quantity\": 1, \n"
                + "      \"size\": 8\n"
                + "    }\n"
                + "  ], \n"
                + "  \"receipt\": \"Oz-Ware Purchase Invoice\", \n"
                + "  \"specialDelivery\": \"Follow the Yellow Brick Road to the Emerald City. Pay no attention to the man behind the curtain.\"\n"
                + "}",
                // numberTest
                ""
                + "{\n"
                + "  \"someKey\": {\n"
                + "    \"1\": 1, \n"
                + "    \"2\": 2, \n"
                + "    \"3\": 3, \n"
                + "    \"4\": 4\n"
                + "  }\n"
                + "}",
                // nullTest
                ""
                + "{\n"
                + "  \"null\": {\n"
                + "    \"null\": \"object\", \n"
                + "    \"object\": null\n"
                + "  }\n"
                + "}"
            }
        } );
        // CHECKSTYLE:ON
    }
    //
    private final Class<? extends ConfigurationProvider> provider;
    private final String testDocument;
    private final String numberTest;
    private final String nullTest;

    @Test
    public void testConfig() throws Exception
    {
        Configuration conf = ConfigurationProvider.getProvider( provider ).load( testDocument );
        testSection( conf );

        StringWriter sw = new StringWriter();
        ConfigurationProvider.getProvider( provider ).save( conf, sw );

        // Check nulls were saved, see #1094
        Assert.assertFalse( "Config contains null", sw.toString().contains( "null" ) );

        conf = ConfigurationProvider.getProvider( provider ).load( new StringReader( sw.toString() ) );
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
        Configuration conf = ConfigurationProvider.getProvider( provider ).load( numberTest );

        Configuration section = conf.getSection( "someKey" );
        for ( String key : section.getKeys() )
        {
            // empty
        }
    }

    @Test
    public void testNull()
    {
        Configuration conf = ConfigurationProvider.getProvider( provider ).load( nullTest );

        Assert.assertEquals( "object", conf.get( "null.null" ) );
        Assert.assertEquals( "object", conf.getSection( "null" ).get( "null" ) );

        Assert.assertEquals( null, conf.get( "null.object" ) );
        Assert.assertEquals( "", conf.getString( "null.object" ) );
    }

    @Test
    public void testMapAddition()
    {
        Configuration conf = ConfigurationProvider.getProvider( provider ).load( testDocument );

        conf.set( "addition", Collections.singletonMap( "foo", "bar" ) );

        // Order matters
        Assert.assertEquals( "bar", conf.getSection( "addition" ).getString( "foo" ) );
        Assert.assertEquals( "bar", conf.getString( "addition.foo" ) );

        Assert.assertTrue( conf.get( "addition" ) instanceof Configuration );
    }
}
