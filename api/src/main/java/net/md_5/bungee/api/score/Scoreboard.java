package net.md_5.bungee.api.score;

import java.util.Collection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface Scoreboard
{

    String getName();

    DisplaySlot getSlot();

    Objective getObjective(String name);

    Collection<Objective> getObjectives();

    Team getTeam(String name);

    Team getTeam(ProxiedPlayer player);

    Collection<Team> getTeams();

    Collection<Score> getScores(ProxiedPlayer player);

    void remove(ProxiedPlayer player);
}
