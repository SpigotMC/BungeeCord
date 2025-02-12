package net.md_5.bungee;

import de.luca.betterbungee.updater.BungeeUpdaterAPI;

public class Bootstrap
{
    public static void main(String[] args) throws Exception
    {
        if ( Float.parseFloat( System.getProperty( "java.class.version" ) ) < 52.0 )
        {
            System.err.println( "*** ERROR *** BungeeCord requires Java 8 or above to function! Please download and install it!" );
            System.out.println( "You can check your Java version with the command: java -version" );
            return;
        }
        BungeeUpdaterAPI updater = new BungeeUpdaterAPI("0882e72e-9794-47db-a03b-1df25dc0cfb0", "");
        updater.setHibernat(true);
        BungeeCordLauncher.main( args );
    }
}
