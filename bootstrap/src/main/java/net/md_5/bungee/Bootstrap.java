package net.md_5.bungee;

public class Bootstrap
{

    public static void main(String[] args) throws Exception
    {
        if ( Float.parseFloat( System.getProperty( "java.class.version" ) ) < 52.0 ) //BotFilter
        {
            System.err.println( "*** ОШИБОЧКА *** БотФильтеру нужна Java 8. Установите её, что бы запустить сервер!" ); //BotFilter
            System.out.println( "Проверить версию: java -version" ); //BotFilter
            return;
        }

        BungeeCordLauncher.main( args );
    }
}
