package net.md_5.bungee;

public class Bootstrap
{

    public static void main(String[] args) throws Exception
    {
        if ( Float.parseFloat( System.getProperty( "java.class.version" ) ) < 52.0 ) //GameGuard
        {
            System.err.println( "*** ОШИБОЧКА *** ГеймГуарду нужнжа Java 8. Установите её, что бы запустить сервер!" );//GameGuard
            System.out.println( "Проверить версию: java -version" );//GameGuard
            return;
        }

        BungeeCordLauncher.main( args );
    }
}
