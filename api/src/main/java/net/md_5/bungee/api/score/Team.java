package net.md_5.bungee.api.score;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;

@Data
public class Team
{

    @NonNull
    private final String name;
    private BaseComponent displayName;
    private BaseComponent prefix;
    private BaseComponent suffix;
    private byte friendlyFire;
    private String nameTagVisibility;
    private String collisionRule;
    private int color;
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
