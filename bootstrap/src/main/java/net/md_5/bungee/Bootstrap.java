package net.md_5.bungee;

public class Bootstrap
{

    public static void main(String[] args) throws Exception
    {
        if ( Float.parseFloat( System.getProperty( "java.class.version" ) ) < 51.0 )
        {
            System.err.println( "*** ERROR *** BungeeCord requires Java 7 or above to function! Please download and install it!" );
            System.out.println( "You can check your Java version with the command: java -version" );
            return;
        }

        BungeeCordLauncher.main( args );
    }
}
