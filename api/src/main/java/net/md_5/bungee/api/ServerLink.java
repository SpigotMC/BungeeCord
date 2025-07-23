package net.md_5.bungee.api;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents a server link which may be sent to the client.
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerLink
{

    /**
     * The links type.
     *
     * Note: This value is nullable, if null, label is non-null.
     */
    private final LinkType type;

    /**
     * The label for the link.
     *
     * Note: This value is nullable, if null, type is non-null.
     */
    private final BaseComponent label;

    /**
     * The URL that is displayed.
     */
    @NonNull
    private final String url;

    /**
     * Creates a link with a specified type and URL.
     *
     * @param type the type of the link
     * @param url the URL to be displayed
     */
    public ServerLink(@NonNull LinkType type, @NonNull String url)
    {
        this.type = type;
        this.label = null;
        this.url = url;
    }

    /**
     * Creates a link with a label and URL.
     *
     * @param label the label to be displayed
     * @param url the URL to be displayed
     */
    public ServerLink(@NonNull BaseComponent label, @NonNull String url)
    {
        this.type = null;
        this.label = label;
        this.url = url;
    }

    public enum LinkType
    {

        REPORT_BUG,
        COMMUNITY_GUIDELINES,
        SUPPORT,
        STATUS,
        FEEDBACK,
        COMMUNITY,
        WEBSITE,
        FORUMS,
        NEWS,
        ANNOUNCEMENTS;
    }
}
