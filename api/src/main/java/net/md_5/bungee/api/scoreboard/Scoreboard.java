package net.md_5.bungee.api.scoreboard;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class Scoreboard
{

    /**
     * Unique name for this scoreboard.
     */
    private final String name;
    /**
     * Position of this scoreboard.
     */
    private final Position position;
    /**
     * Objectives for this scoreboard.
     */
    @Getter(AccessLevel.NONE)
    private final Map<String, Objective> objectives = new HashMap<>();
    /**
     * Scores for this scoreboard.
     */
    @Getter(AccessLevel.NONE)
    private final Map<String, Score> scores = new HashMap<>();

    public Collection<Objective> getObjectives()
    {
        return Collections.unmodifiableCollection( objectives.values() );
    }

    public Collection<Score> getScores()
    {
        return Collections.unmodifiableCollection( scores.values() );
    }

    public void addObjective(Objective objective)
    {
        Preconditions.checkArgument( !objectives.containsKey( objective.getName() ), "Objective %s already exists in this scoreboard", objective );
        objectives.put( objective.getName(), objective );
    }

    public void addScore(Score score)
    {
        Preconditions.checkArgument( !scores.containsKey( score.getItemName() ), "Score %s already exists in this scoreboard", score );
        scores.put( score.getItemName(), score );
    }

    public void removeObjective(String objectiveName)
    {
        objectives.remove( objectiveName );
    }

    public void removeScore(String scoreName)
    {
        scores.remove( scoreName );
    }
}
