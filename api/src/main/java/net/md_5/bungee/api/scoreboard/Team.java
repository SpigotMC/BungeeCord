package net.md_5.bungee.api.scoreboard;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
public class Team
{

    private final String name;
    private String displayName;
    private String prefix;
    private String suffix;
    private boolean friendlyFire;
    private Set<String> players = new HashSet<>();

    public Collection<String> getPlayers()
    {
        return Collections.unmodifiableSet( players );
    }

    public void addPlayer(String name)
    {
        players.add( name );
    }

    public void removePlayer(String name)
    {
        players.remove( name );
    }
}
