package net.md_5.bungee.api.chat.objects;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.md_5.bungee.api.chat.player.Profile;
import net.md_5.bungee.api.chat.player.Property;

@Data
@AllArgsConstructor
public final class PlayerObject implements ChatObject
{

    /**
     * The profile of the player.
     */
    @NonNull
    private Profile profile;
    /**
     * If true, a hat layer will be rendered on the head. (default: true)
     */
    private Boolean hat;

    public PlayerObject(@NonNull String name)
    {
        this.profile = new Profile( name );
    }

    public PlayerObject(@NonNull UUID uuid)
    {
        this.profile = new Profile( uuid );
    }

    public PlayerObject(@NonNull Property[] properties)
    {
        this.profile = new Profile( properties );
    }
}
