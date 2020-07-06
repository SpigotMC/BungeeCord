package net.md_5.bungee.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the standard list data returned by opening a server in the
 * Minecraft client server list, or hitting it with a packet 0xFE.
 */
@Data
@ToString(exclude = "favicon")
@NoArgsConstructor
@AllArgsConstructor
public class ServerPing {

    // Right now, we don't get the mods from the user, so we just use a stock ModInfo object to
    // create the server ping. Vanilla clients will ignore this.
    private final ModInfo modinfo = new ModInfo();
    private Protocol version;
    private Players players;
    private BaseComponent description;
    private Favicon favicon;

    @Deprecated
    public ServerPing(Protocol version, Players players, String description, String favicon) {
        this(version, players, new TextComponent(TextComponent.fromLegacyText(description)), favicon == null ? null : Favicon.create(favicon));
    }
    @Deprecated
    public ServerPing(Protocol version, Players players, String description, Favicon favicon) {
        this(version, players, new TextComponent(TextComponent.fromLegacyText(description)), favicon);
    }

    @Deprecated
    public String getFavicon() {
        return getFaviconObject() == null ? null : getFaviconObject().getEncoded();
    }

    @Deprecated
    public void setFavicon(String favicon) {
        setFavicon(favicon == null ? null : Favicon.create(favicon));
    }

    public void setFavicon(Favicon favicon) {
        this.favicon = favicon;
    }

    public Favicon getFaviconObject() {
        return this.favicon;
    }

    @Deprecated
    public String getDescription() {
        return BaseComponent.toLegacyText(description);
    }

    @Deprecated
    public void setDescription(String description) {
        this.description = new TextComponent(TextComponent.fromLegacyText(description));
    }

    public BaseComponent getDescriptionComponent() {
        return description;
    }

    public void setDescriptionComponent(BaseComponent description) {
        this.description = description;
    }

    @Data
    @AllArgsConstructor
    public static class Protocol {

        private String name;
        private int protocol;
    }

    @Data
    @AllArgsConstructor
    public static class Players {

        private int max;
        private int online;
        private PlayerInfo[] sample;
    }

    @Data
    @AllArgsConstructor
    public static class PlayerInfo {

        private static final UUID md5UUID = Util.getUUID("af74a02d19cb445bb07f6866a861f783");
        private String name;
        private UUID uniqueId;

        public PlayerInfo(String name, String id) {
            setName(name);
            setId(id);
        }

        public String getId() {
            return uniqueId.toString().replace("-", "");
        }

        public void setId(String id) {
            try {
                uniqueId = Util.getUUID(id);
            } catch (Exception e) {
                // Fallback on a valid uuid otherwise Minecraft complains
                uniqueId = md5UUID;
            }
        }
    }

    @Data
    public static class ModInfo {

        private String type = "FML";
        private List<ModItem> modList = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class ModItem {

        private String modid;
        private String version;
    }
}
