package net.md_5.bungee.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the standard list data returned by opening a server in the
 * Minecraft client server list, or hitting it with a packet 0xFE.
 */
@Data
@ToString(exclude = "favicon")
@NoArgsConstructor
@AllArgsConstructor
public class ServerPing
{

    @NonNull
    private Protocol version;

    @Data
    @AllArgsConstructor
    public static class Protocol
    {

        private String name;
        private int protocol;
    }
    @NonNull
    private Players players;

    @Data
    @AllArgsConstructor
    public static class Players
    {

        private int max;
        private int online;
        @Nullable
        private PlayerInfo[] sample;
    }

    @Data
    @AllArgsConstructor
    public static class PlayerInfo
    {

        private String name;
        private UUID uniqueId;

        private static final UUID md5UUID = Util.getUUID( "af74a02d19cb445bb07f6866a861f783" );

        public PlayerInfo(String name, String id)
        {
            setName( name );
            setId( id );
        }

        public void setId(String id)
        {
            try
            {
                uniqueId = Util.getUUID( id );
            } catch ( Exception e )
            {
                // Fallback on a valid uuid otherwise Minecraft complains
                uniqueId = md5UUID;
            }
        }

        @NotNull
        public String getId()
        {
            return uniqueId.toString().replace( "-", "" );
        }
    }

    @NonNull
    private BaseComponent description;
    @Nullable
    private Favicon favicon;

    @Data
    public static class ModInfo
    {

        private String type = "FML";
        private List<ModItem> modList = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class ModItem
    {

        private String modid;
        private String version;
    }

    // Right now, we don't get the mods from the user, so we just use a stock ModInfo object to
    // create the server ping. Vanilla clients will ignore this.
    private final ModInfo modinfo = new ModInfo();

    @Deprecated
    public ServerPing(Protocol version, Players players, String description, String favicon)
    {
        this( version, players, new TextComponent( TextComponent.fromLegacyText( description ) ), favicon == null ? null : Favicon.create( favicon ) );
    }

    @Deprecated
    public ServerPing(Protocol version, Players players, String description, Favicon favicon)
    {
        this( version, players, new TextComponent( TextComponent.fromLegacyText( description ) ), favicon );
    }

    @Nullable
    @Deprecated
    public String getFavicon()
    {
        return getFaviconObject() == null ? null : getFaviconObject().getEncoded();
    }

    @Nullable
    public Favicon getFaviconObject()
    {
        return this.favicon;
    }

    @Deprecated
    public void setFavicon(@Nullable String favicon)
    {
        setFavicon( favicon == null ? null : Favicon.create( favicon ) );
    }

    public void setFavicon(@Nullable Favicon favicon)
    {
        this.favicon = favicon;
    }

    @Deprecated
    public void setDescription(@NotNull String description)
    {
        this.description = new TextComponent( TextComponent.fromLegacyText( description ) );
    }

    @NotNull
    @Deprecated
    public String getDescription()
    {
        return BaseComponent.toLegacyText( description );
    }

    public void setDescriptionComponent(@NotNull BaseComponent description)
    {
        this.description = description;
    }

    @NotNull
    public BaseComponent getDescriptionComponent()
    {
        return description;
    }
}
