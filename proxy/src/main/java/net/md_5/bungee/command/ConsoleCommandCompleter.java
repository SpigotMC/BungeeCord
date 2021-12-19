package net.md_5.bungee.command;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jline.console.completer.Completer;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

@RequiredArgsConstructor
public class ConsoleCommandCompleter implements Completer
{

    private final ProxyServer proxy;

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates)
    {

        int lastSpace = buffer.lastIndexOf( ' ' );
        if ( lastSpace == -1 )
        {
            Iterables.transform( Iterables.filter( ProxyServer.getInstance().getPluginManager().getCommands(), new Predicate<Map.Entry<String, Command>>()
            {
                @Override
                public boolean apply(Map.Entry<String, Command> commandEntry)
                {
                    return commandEntry.getKey().toLowerCase( Locale.ROOT ).startsWith( buffer );
                }
            } ), new Function<Map.Entry<String, Command>, String>()
            {
                @Override
                public String apply(Map.Entry<String, Command> entry)
                {
                    return entry.getKey();
                }
            } ).forEach( candidates::add );
        } else
        {
            List<String> suggestions = new ArrayList<>();
            proxy.getPluginManager().dispatchCommand( proxy.getConsole(), buffer, suggestions );
            candidates.addAll( suggestions );
        }

        return ( lastSpace == -1 ) ? cursor - buffer.length() : cursor - ( buffer.length() - lastSpace - 1 );
    }
}
