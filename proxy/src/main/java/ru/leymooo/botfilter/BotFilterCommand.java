package ru.leymooo.botfilter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import ru.leymooo.botfilter.config.Settings;

public class BotFilterCommand extends Command
{

    public BotFilterCommand()
    {
        super( "botfilter", null, "bf", "antibot", "gg" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( sender instanceof ProxiedPlayer )
        {
            sendStat( sender );
            return;
        }
        if ( args.length == 0 )
        {
            sender.sendMessage( "§r--------------- §bBotFilter §cv" + Settings.IMP.BOT_FILTER_VERSION + "§r-----------------" );
            sender.sendMessage( "§r> §lbotfilter reload §6- §aПерезагружить конфиг" );
            sender.sendMessage( "§r> §lbotfilter stat §6- §aПоказать статистику" );
            sender.sendMessage( "§r> §lbotfilter export §6- §aВыгрузить список игроков, которые прошли проверку" );
            sender.sendMessage( "§r> §lbotfilter protection on/off §6- §aВключить или выключить ручной режим 'под атакой'" );
            sender.sendMessage( "§r--------------- §bBotFilter §r-----------------" );
        } else if ( args[0].equalsIgnoreCase( "reload" ) )
        {
            BungeeCord.getInstance().getBotFilter().disable();
            BungeeCord.getInstance().setBotFilter( new BotFilter( false ) );
            sender.sendMessage( "§aКоманда выполнена" );
        } else if ( args[0].equalsIgnoreCase( "stat" ) || args[0].equalsIgnoreCase( "stats" ) || args[0].equalsIgnoreCase( "info" ) )
        {
            sendStat( sender );
        } else if ( args[0].equalsIgnoreCase( "export" ) )
        {
            export( sender, args );
            sender.sendMessage( "§aКоманда выполнена" );
        } else if ( args[0].equalsIgnoreCase( "protection" ) )
        {
            if ( args.length >= 2 )
            {
                boolean enable = args[1].equalsIgnoreCase( "on" );
                BungeeCord.getInstance().getBotFilter().setForceProtectionEnabled( enable );
                sender.sendMessage( "§aЗашита " + ( enable ? "включена" : "§cотключена" ) );
            }
        }
    }

    private void sendStat(CommandSender sender)
    {
        BotFilter botFilter = BungeeCord.getInstance().getBotFilter();
        sender.sendMessage( "§r----------------- §bBotFilter §cv" + Settings.IMP.BOT_FILTER_VERSION + " §r-----------------" );
        sender.sendMessage( "§r> §lОбнаружена атака: " + ( botFilter.isUnderAttack() ? "§cДа" : "§aНет" ) );
        sender.sendMessage( "§r> §lБотов на проверке: " + botFilter.getOnlineOnFilter() );
        sender.sendMessage( "§r> §lПрошло проверку: " + botFilter.getUsersCount() );
        sender.sendMessage( "§r> §lСкачать BotFilter: http://www.rubukkit.org/threads/137038/" );
    }

    private void export(CommandSender sender, String[] args)
    {
        BotFilter botFilter = BungeeCord.getInstance().getBotFilter();

        if ( args.length == 1 )
        {
            sender.sendMessage( "§r> §lbotfilter export [TIME_IN_SECONDS] §6- §aвыгрузить список тех, кто прошёл"
                + " проверку за указаное время. укажите ALL чтобы получить за всё время." );
            sender.sendMessage( "§r> §lbotfilter export [TIME_IN_SECONDS] JOIN §6- §aвыгрузить список тех,"
                + " кто зашёл на сервер за указанное время (Учитывает и тех кто  также и прошёл проверку)." );
            return;
        }
        if ( args[1].equalsIgnoreCase( "all" ) )
        {
            List<String> out = new ArrayList<>( botFilter.getUsersCount() );
            botFilter.getUserCache().values().forEach( value ->
                out.add( value.getName() + "|" + value.getIp() + "|" + value.getLastCheck() + "|" + value.getLastJoin() )
            );
            exportToFile( out, args.length >= 3 && args[2].equalsIgnoreCase( "join" ) );
            return;
        }
        try
        {
            int seconds = Integer.parseInt( args[1] );
            boolean join = args.length >= 3 && args[2].equalsIgnoreCase( "join" );
            Calendar calendar = Calendar.getInstance();
            calendar.add( Calendar.SECOND, -seconds );
            long until = calendar.getTimeInMillis();

            List<String> out = new ArrayList<>( botFilter.getUsersCount() );
            botFilter.getUserCache().values().forEach( value ->
                {
                    if ( join )
                    {
                        if ( value.getLastJoin() >= until )
                        {
                            out.add( value.getName() + "|" + value.getIp() + "|" + value.getLastCheck() + "|" + value.getLastJoin() );
                        }
                    } else if ( value.getLastCheck() >= until )
                    {
                        out.add( value.getName() + "|" + value.getIp() + "|" + value.getLastCheck() + "|" + value.getLastJoin() );
                    }
                }
            );
            exportToFile( out, join );
        } catch ( Exception e )
        {
            sender.sendMessage( "§cУкажите число" );
        }
    }

    private void exportToFile(List<String> out, boolean join)
    {
        Path outFile = new File( "BotFilter", "whitelist.out." + ( join ? "join" : "" ) + ".txt" ).toPath();
        try
        {
            Files.write( outFile, out, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
        } catch ( IOException e )
        {
            BungeeCord.getInstance().getLogger().log( Level.WARNING, "[BotFilter] Could not export ip's to file", e );
        }
    }

}
