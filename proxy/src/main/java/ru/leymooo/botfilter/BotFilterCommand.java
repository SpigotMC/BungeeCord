package ru.leymooo.botfilter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
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
        BotFilter botFilter = BungeeCord.getInstance().getBotFilter();
        if ( args.length == 0 )
        {
            sender.sendMessage( "§r--------------- §bBotFilter §cv" + Settings.IMP.BOT_FILTER_VERSION + "§r-----------------" );
            sender.sendMessage( "§r> §lbotfilter reload §6- §aПерезагружить конфиг" );
            sender.sendMessage( "§r> §lbotfilter stat §6- §aПоказать статистику" );
            sender.sendMessage( "§r> §lbotfilter export §6- §aВыгрузить список игроков, которые прошли проверку" );
            sender.sendMessage( "§r--------------- §bBotFilter §r-----------------" );
        } else if ( args[0].equalsIgnoreCase( "reload" ) )
        {
            botFilter.disable();
            BungeeCord.getInstance().setBotFilter( new BotFilter( false ) );
            sender.sendMessage( "§aКоманда выполнена" );
        } else if ( args[0].equalsIgnoreCase( "stat" ) || args[0].equalsIgnoreCase( "stats" ) || args[0].equalsIgnoreCase( "info" ) )
        {
            sendStat( sender );
        } else if ( args[0].equalsIgnoreCase( "export" ) )
        {
            List<String> out = botFilter.getUserCache().entrySet().stream()
                .map( entry -> entry.getKey() + "|" + entry.getValue() ).collect( Collectors.toList() );

            Path outFile = new File( "BotFilter", "whitelist.out.txt" ).toPath();

            try
            {
                Files.move( outFile, new File( "BotFilter", "whitelist.out.txt." + System.nanoTime() ).toPath() );
            } catch ( Exception ignore )
            {
            }
            try
            {
                Files.write( outFile, out, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
            } catch ( IOException e )
            {
                BungeeCord.getInstance().getLogger().log( Level.WARNING, "[BotFilter] Could not export ip's to file", e );
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
}
