package net.md_5.bungee.command;

import jline.console.completer.Completer;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ConsoleCommandCompleter implements Completer {

    private final ProxyServer proxy;

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        List<String> suggestions = new ArrayList<>();
        proxy.getPluginManager().dispatchCommand(proxy.getConsole(), buffer, suggestions);
        candidates.addAll(suggestions);

        int lastSpace = buffer.lastIndexOf(' ');
        return (lastSpace == -1) ? cursor - buffer.length() : cursor - (buffer.length() - lastSpace - 1);
    }
}
