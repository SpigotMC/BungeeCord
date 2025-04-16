package net.md_5.bungee.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import jline.console.completer.Completer;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;

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
            String lowerCase = buffer.toLowerCase( Locale.ROOT );
            candidates.addAll( proxy.getPluginManager().getCommands().stream()
                    .map( Map.Entry::getKey )
                    .filter( (name) -> name.toLowerCase( Locale.ROOT ).startsWith( lowerCase ) )
                    .collect( Collectors.toList() ) );
        } else
        {
            List<String> suggestions = new ArrayList<>();
            proxy.getPluginManager().dispatchCommand( proxy.getConsole(), buffer, suggestions );
            candidates.addAll( suggestions );
        }

        return ( lastSpace == -1 ) ? cursor - buffer.length() : cursor - ( buffer.length() - lastSpace - 1 );
    }
}
