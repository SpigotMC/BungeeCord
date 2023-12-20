package net.md_5.bungee.config;

import static org.junit.jupiter.api.Assertions.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@RequiredArgsConstructor
public class CompoundConfigurationTest
{

    public static Stream<Arguments> data()
    {
        return Stream.of(
                Arguments.of(
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
                ),
                Arguments.of(
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
                )
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testConfig(Class<? extends ConfigurationProvider> provider, String testDocument, String numberTest, String nullTest) throws Exception
    {
        Configuration conf = ConfigurationProvider.getProvider( provider ).load( testDocument );
        testSection( conf );

        StringWriter sw = new StringWriter();
        ConfigurationProvider.getProvider( provider ).save( conf, sw );

        // Check nulls were saved, see #1094
        assertFalse( sw.toString().contains( "null" ), "Config contains null" );

        conf = ConfigurationProvider.getProvider( provider ).load( new StringReader( sw.toString() ) );
        conf.set( "receipt", "Oz-Ware Purchase Invoice" ); // Add it back
        testSection( conf );
    }

    private void testSection(Configuration conf)
    {
        assertEquals( "Oz-Ware Purchase Invoice", conf.getString( "receipt" ), "receipt" );
        // assertEquals( "2012-08-06", conf.get( "date" ).toString(), "date" );

        Configuration customer = conf.getSection( "customer" );
        assertEquals( "Dorothy", customer.getString( "given" ), "customer.given" );
        assertEquals( "Dorothy", conf.getString( "customer.given" ), "customer.given" );

        List items = conf.getList( "items" );
        Map item = (Map) items.get( 0 );
        assertEquals( "A4786", item.get( "part_no" ), "items[0].part_no" );

        conf.set( "receipt", null );
        assertEquals( null, conf.get( "receipt" ) );
        assertEquals( "foo", conf.get( "receipt", "foo" ) );

        Configuration newSection = conf.getSection( "new.section" );
        newSection.set( "value", "foo" );
        assertEquals( "foo", conf.get( "new.section.value" ) );

        conf.set( "other.new.section", "bar" );
        assertEquals( "bar", conf.get( "other.new.section" ) );

        assertTrue( conf.contains( "customer.given" ) );
        assertTrue( customer.contains( "given" ) );

        assertFalse( conf.contains( "customer.foo" ) );
        assertFalse( customer.contains( "foo" ) );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNumberedKeys(Class<? extends ConfigurationProvider> provider, String testDocument, String numberTest, String nullTest)
    {
        Configuration conf = ConfigurationProvider.getProvider( provider ).load( numberTest );

        Configuration section = conf.getSection( "someKey" );
        for ( String key : section.getKeys() )
        {
            // empty
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNull(Class<? extends ConfigurationProvider> provider, String testDocument, String numberTest, String nullTest)
    {
        Configuration conf = ConfigurationProvider.getProvider( provider ).load( nullTest );

        assertEquals( "object", conf.get( "null.null" ) );
        assertEquals( "object", conf.getSection( "null" ).get( "null" ) );

        assertEquals( null, conf.get( "null.object" ) );
        assertEquals( "", conf.getString( "null.object" ) );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testMapAddition(Class<? extends ConfigurationProvider> provider, String testDocument, String numberTest, String nullTest)
    {
        Configuration conf = ConfigurationProvider.getProvider( provider ).load( testDocument );

        conf.set( "addition", Collections.singletonMap( "foo", "bar" ) );

        // Order matters
        assertEquals( "bar", conf.getSection( "addition" ).getString( "foo" ) );
        assertEquals( "bar", conf.getString( "addition.foo" ) );

        assertTrue( conf.get( "addition" ) instanceof Configuration );
    }
}
