package ru.leymooo.botfilter;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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
            sender.sendMessage( "§r--------------- §bBotFilter §r-----------------" );
            sender.sendMessage( "§r> §lbotfilter reload §6- §aПерезагружить конфиг" );
            sender.sendMessage( "§r> §lbotfilter stat §6- §aПоказать статистику" );
            sender.sendMessage( "§r--------------- §bBotFilter §r-----------------" );
        } else if ( args[0].equalsIgnoreCase( "reload" ) )
        {
            BotFilter.getInstance().disable();
            new BotFilter( false );
            sender.sendMessage( "§aКоманда выполнена" );
        } else if ( args[0].equalsIgnoreCase( "stat" ) )
        {
            sendStat( sender );
        }
    }

    private void sendStat(CommandSender sender)
    {
        sender.sendMessage( "§r--------------- §bBotFilter -----------------" );
        sender.sendMessage( "§r> §lОбнаружена атака: " + ( BotFilter.getInstance().isUnderAttack() ? "§cДа" : "§aНет" ) );
        sender.sendMessage( "§r> §lБотов на проверке: " + BotFilter.getInstance().connectedUsersSet.size() );
        sender.sendMessage( "§r> §lПрошло проверку: " + BotFilter.getInstance().userCache.size() );
        sender.sendMessage( "§r--------------- §bBotFilter -----------------" );
    }
}
