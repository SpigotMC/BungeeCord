package net.md_5.bungee.api.chat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Click event which sends a custom payload to the server.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ClickEventCustom extends ClickEvent
{

    /**
     * The custom payload.
     */
    private final String payload;

    /**
     * @param id identifier for the event (lower case, no special characters)
     * @param payload custom payload
     */
    public ClickEventCustom(String id, String payload)
    {
        super( ClickEvent.Action.CUSTOM, id );
        this.payload = payload;
    }
}
