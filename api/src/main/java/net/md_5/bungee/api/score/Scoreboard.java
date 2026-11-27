package net.md_5.bungee.api.score;

import com.google.common.base.Preconditions;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Scoreboard
{

    /**
     * Keeps track of the most recent scoreboard name and position sent by server.
     */
    @Deprecated
    private Map.Entry<Position, String> lastEntry;
    /**
     * The name of each scoreboard in their given positions.
     */
    private final Map<Position, String> names = new HashMap<>();
    /**
     * Objectives for this scoreboard.
     */
    private final Map<String, Objective> objectives = new HashMap<>();
    /**
     * Scores for this scoreboard.
     */
    private final Map<String, Score> scores = new HashMap<>();
    /**
     * Teams on this board.
     */
    private final Map<String, Team> teams = new HashMap<>();

    public Map<Position, String> getNames()
    {
        return Collections.unmodifiableMap( names );
    }

    public Collection<Objective> getObjectives()
    {
        return Collections.unmodifiableCollection( objectives.values() );
    }

    public Collection<Score> getScores()
    {
        return Collections.unmodifiableCollection( scores.values() );
    }

    public Collection<Team> getTeams()
    {
        return Collections.unmodifiableCollection( teams.values() );
    }

    /**
     * Gets the name of the most recent scoreboard sent by server
     *
     * @return the name of the scoreboard
     * @deprecated method retained for backwards compatibility
     */
    @Deprecated
    public String getName()
    {
        return ( lastEntry == null ) ? null : lastEntry.getValue();
    }

    /**
     * Gets the position of the most recent scoreboard sent by server
     *
     * @return the position of the scoreboard
     * @deprecated method retained for backwards compatibility
     */
    @Deprecated
    public Position getPosition()
    {
        return ( lastEntry == null ) ? null : lastEntry.getKey();
    }

    /**
     * Gets the name of the scoreboard in a given position
     *
     * @return the name of the scoreboard
     */
    public String getName( Position position )
    {
        return names.get( position );
    }

    /**
     * Updates the name of the scoreboard for a slot.
     *
     * @param name the new name
     * @param position the position being updated
     */
    public void updateName( Position position, String name )
    {
        names.put( position, name );
        lastEntry = new AbstractMap.SimpleEntry<>( position, name );
    }

    public void addObjective(Objective objective)
    {
        Preconditions.checkNotNull( objective, "objective" );
        Preconditions.checkArgument( !objectives.containsKey( objective.getName() ), "Objective %s already exists in this scoreboard", objective.getName() );
        objectives.put( objective.getName(), objective );
    }

    public void addScore(Score score)
    {
        Preconditions.checkNotNull( score, "score" );
        scores.put( score.getItemName(), score );
    }

    public Score getScore(String name)
    {
        return scores.get( name );
    }

    public void addTeam(Team team)
    {
        Preconditions.checkNotNull( team, "team" );
        Preconditions.checkArgument( !teams.containsKey( team.getName() ), "Team %s already exists in this scoreboard", team.getName() );
        teams.put( team.getName(), team );
    }

    public Team getTeam(String name)
    {
        return teams.get( name );
    }

    public Objective getObjective(String name)
    {
        return objectives.get( name );
    }

    public void removeObjective(String objectiveName)
    {
        objectives.remove( objectiveName );
    }

    public void removeScore(String scoreName)
    {
        scores.remove( scoreName );
    }

    public void removeTeam(String teamName)
    {
        teams.remove( teamName );
    }

    public void clear()
    {
        lastEntry = null;
        names.clear();
        objectives.clear();
        scores.clear();
        teams.clear();
    }
}
