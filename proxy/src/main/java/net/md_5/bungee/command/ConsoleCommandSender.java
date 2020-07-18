package net.md_5.bungee.command;

import java.util.Collection;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Command sender representing the proxy console.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleCommandSender implements CommandSender
{

    @Getter
    private static final ConsoleCommandSender instance = new ConsoleCommandSender();

    @Override
    public void sendMessage(@NotNull String message)
    {
        ProxyServer.getInstance().getLogger().info( message );
    }

    @Override
    public void sendMessages(String... messages)
    {
        for ( String message : messages )
        {
            sendMessage( message );
        }
    }

    @Override
    public void sendMessage(BaseComponent... message)
    {
        sendMessage( BaseComponent.toLegacyText( message ) );
    }

    @Override
    public void sendMessage(@NotNull BaseComponent message)
    {
        sendMessage( message.toLegacyText() );
    }

    @Override
    @NotNull
    public String getName()
    {
        return "CONSOLE";
    }

    @Override
    @NotNull
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
    public boolean hasPermission(@NotNull String permission)
    {
        return true;
    }

    @Override
    public void setPermission(@NotNull String permission, boolean value)
    {
        throw new UnsupportedOperationException( "Console has all permissions" );
    }

    @Override
    @NotNull
    public Collection<String> getPermissions()
    {
        return Collections.emptySet();
    }
}
