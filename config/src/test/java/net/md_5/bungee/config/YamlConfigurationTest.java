package net.md_5.bungee.config;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class YamlConfigurationTest
{

    private String document = ""
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

    @Test
    public void testConfig() throws Exception
    {
        Configuration conf = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( document );
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
    }
}
