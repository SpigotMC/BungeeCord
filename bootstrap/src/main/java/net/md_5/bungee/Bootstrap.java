package net.md_5.bungee;

public class Bootstrap
{

    public static void main(String[] args) throws Exception
    {
        if ( Float.parseFloat( System.getProperty( "java.class.version" ) ) < 52.0 )
        {
            System.err.println( "*** ERROR *** BungeeCord requires Java 8 or above to function! Please download and install it!" );
            System.out.println( "You are currently running the Java version " + System.getProperty( "java.version" ) );
            return;
        }

        BungeeCordLauncher.main( args );
    }
}
