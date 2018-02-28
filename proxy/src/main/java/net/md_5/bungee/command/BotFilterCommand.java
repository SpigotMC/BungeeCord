package net.md_5.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class BotFilterCommand extends Command
{

    public BotFilterCommand()
    {
        super( "botfilter", null, "bf", "antibot", "gg" );
    }
    private boolean permanent = false;

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        /*
        if ( sender instanceof ProxiedPlayer )
        {
            sendStat( sender );
            return;
        }
        if ( args.length == 0 )
        {
            sender.sendMessage( "§r--------------- §bBotFilter §r-----------------" );
            sender.sendMessage( "§r> §lbotfilter reload §a[config,proxy]" );
            sender.sendMessage( "§r> §lbotfilter stat §6- §aПоказать статистику" );
            sender.sendMessage( "§r> §lbotfilter mode §a[auto, permanent] " );
            sender.sendMessage( "§r--------------- §bBotFilter §r-----------------" );
            return;
        }
        if ( args[0].equalsIgnoreCase( "reload" ) )
        {
            Config.getConfig().getSql().fullClose();
            Config.getConfig().getGeoUtils().close();
            ServerPingUtils.getInstance().getPingList().invalidateAll();
            new Config();
            Utils.connections.invalidateAll();
            sender.sendMessage( "§aКоманда выполнена" );
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
                sender.sendMessage( "§c§lВнимание! §aПосле написания §6botfilter mode confirm §aв конфиге пропадут подсказки!" );
            } else if ( args[1].equalsIgnoreCase( "permanent" ) )
            {
                this.permanent = true;
                sender.sendMessage( "§c§lВнимание! §aПосле написания §6botfilter mode confirm §aв конфиге пропадут подсказки!" );
            } else if ( args[1].equalsIgnoreCase( "confirm" ) )
            {
                
            }

        }
        if ( args[0].equalsIgnoreCase( "stat" ) )
        {
            sendStat( sender );
        }
         */
    }

    private void sendStat(CommandSender sender)
    {
        /*
        sender.sendMessage( "§r--------------- §bBotFilter -----------------" );
        sender.sendMessage( "§r> §lОбнаружена атака: " + ( Config.getConfig().isUnderAttack() ? "§cДа" : "§aНет" ) );
        sender.sendMessage( "§r> §lРежим работы: " + ( Config.getConfig().isPermanent() ? "§aПостоянный" : "§aАвтоматический" ) );
        sender.sendMessage( "§r> §lБотов на проверке: " + Config.getConfig().getConnectedUsersSet().size() );
        sender.sendMessage( "§r> §lПрошло проверку: " + Config.getConfig().getUsers().size() );
        sender.sendMessage( "§r--------------- §bBotFilter -----------------" );
         */
    }
}
