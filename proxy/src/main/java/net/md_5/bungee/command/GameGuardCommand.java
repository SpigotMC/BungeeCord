package net.md_5.bungee.command;

import java.io.File;
import java.io.IOException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import ru.leymooo.gameguard.Config;
import ru.leymooo.gameguard.utils.Proxy;
import ru.leymooo.gameguard.utils.Utils;

public class GameGuardCommand extends Command
{

    public GameGuardCommand()
    {
        super( "gg", null, "gameguard" );
    }
    private boolean permanent = false;

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
            sender.sendMessage( "§r--------------- §bGameGuard §r-----------------" );
            sender.sendMessage( "§r> §lgameguard reload §a[config,proxy]" );
            sender.sendMessage( "§r> §lgameguard stat §6- §aПоказать статистику" );
            sender.sendMessage( "§r> §lgameguard mode §a[auto, permanent] " );
            sender.sendMessage( "§r--------------- §bGameGuard §r-----------------" );
            return;
        }
        if ( args[0].equalsIgnoreCase( "reload" ) )
        {
            if ( args.length == 1 )
            {
                sender.sendMessage( "§cconfig §aили §cproxy" );
                return;
            }
            if ( args[1].equals( "config" ) )
            {
                new Config();
                Utils.connections.invalidateAll();
                sender.sendMessage( "§aКоманда выполнена" );
            } else if ( args[1].equalsIgnoreCase( "proxy" ) )
            {
                Config.getConfig().setProxy( new Proxy( new File( "GameGuard" ) ) );
                sender.sendMessage( "§aКоманда выполнена" );
            }
        }
        if ( args[0].equalsIgnoreCase( "mode" ) )
        {
            if ( args.length == 1 )
            {
                sender.sendMessage( "§cauto §aили §cpermanent" );
                return;
            }
            if ( args[1].equals( "auto" ) )
            {
                this.permanent = false;
                sender.sendMessage( "§c§lВнимание! §aПосле написания §6gameguard mode confirm §aв конфиге пропадут подсказки!" );
            } else if ( args[1].equalsIgnoreCase( "permanent" ) )
            {
                this.permanent = true;
                sender.sendMessage( "§c§lВнимание! §aПосле написания §6gameguard mode confirm §aв конфиге пропадут подсказки!" );
            } else if ( args[1].equalsIgnoreCase( "confirm" ) )
            {
                try
                {
                    Config.getConfig().getMainConfig().set( "permanent-protection", permanent );
                    Config.getConfig().setPermanent( permanent );
                    ConfigurationProvider.getProvider( YamlConfiguration.class ).save( Config.getConfig().getMainConfig(), new File( "GameGuard", "gameguard.yml" ) );
                    sender.sendMessage( "§aКоманда выполнена" );
                } catch ( IOException ex )
                {
                    ex.printStackTrace();
                }
            }

        }
        if ( args[0].equalsIgnoreCase( "stat" ) )
        {
            sendStat( sender );
        }
    }

    private void sendStat(CommandSender sender)
    {
        sender.sendMessage( "§r--------------- §bGameGuard -----------------" );
        sender.sendMessage( "§r> §lОбнаружена атака: " + ( Config.getConfig().isUnderAttack() ? "§cДа" : "§aНет" ) );
        sender.sendMessage( "§r> §lРежим работы: " + ( Config.getConfig().isPermanent() ? "§aПостоянный" : "§aАвтоматический" ) );
        sender.sendMessage( "§r> §lБотов на проверке: " + Config.getConfig().getConnectedUsersSet().size() );
        sender.sendMessage( "§r> §lПрошло проверку: " + Config.getConfig().getUsers().size() );
        sender.sendMessage( "§r--------------- §bGameGuard -----------------" );
    }
}
