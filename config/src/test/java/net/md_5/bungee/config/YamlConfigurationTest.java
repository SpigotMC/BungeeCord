package net.md_5.bungee.config;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class YamlConfigurationTest
{

    private String docuement = ""
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
    public void testRead() throws Exception
    {
        Configuration conf = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( docuement );

        Assert.assertEquals( "receipt", "Oz-Ware Purchase Invoice", conf.getString( "receipt" ) );
        // Assert.assertEquals( "date", "2012-08-06", conf.get( "date" ).toString() );

        Configuration customer = conf.getSection( "customer" );
        Assert.assertEquals( "customer.given", "Dorothy", customer.getString( "given" ) );
        Assert.assertEquals( "customer.given", "Dorothy", conf.getString( "customer.given" ) );

        List items = conf.getList( "items" );
        Map item = (Map) items.get( 0 );
        Assert.assertEquals( "items[0].part_no", "A4786", item.get( "part_no" ) );
    }
}
