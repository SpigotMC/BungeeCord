package net.md_5.bungee.command;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

/**
 * Command sender representing the proxy console.
 */
public class ConsoleCommandSender implements CommandSender
{

    @Getter
    private static final ConsoleCommandSender instance = new ConsoleCommandSender();

    private ConsoleCommandSender()
    {
    }

    @Override
    public void sendMessage(String message)
    {
        System.out.println( ChatColor.stripColor( message ) );
    }
    
    @Override
    public void sendMessage(String... messages)
    {
        for(int i=0;i<messages.length;i++) {
            sendMessage(messages[i]);
        }
    }

    @Override
    public String getName()
    {
        return "CONSOLE";
    }

    @Override
    public Collection<String> getGroups()
    {
        return Collections.emptySet();
    }

    @Override
    public void addGroups(String... groups)
    {
        throw new UnsupportedOperationException( "Console may not have groups" );
    }

    @Override
    public void removeGroups(String... groups)
    {
        throw new UnsupportedOperationException( "Console may not have groups" );
    }

    @Override
    public boolean hasPermission(String permission)
    {
        return true;
    }

    @Override
    public void setPermission(String permission, boolean value)
    {
        throw new UnsupportedOperationException( "Console has all permissions" );
    }
}
