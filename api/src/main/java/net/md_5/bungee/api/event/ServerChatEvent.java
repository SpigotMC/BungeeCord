package net.md_5.bungee.api.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Event called when a server sends a message to a player.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ServerChatEvent extends TargetedEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    @Getter
    @Setter
    private boolean cancelled;

    /**
     * Text contained in this chat.
     */
    private BaseComponent[] message = null;
    private String messageJson = null;

    /**
     * Type of message/display location
     */
    @Getter
    @Setter
    private ChatMessageType position;

    public ServerChatEvent(Connection sender, Connection receiver, String msgJson, ChatMessageType position)
    {
        super( sender, receiver );
        this.messageJson = msgJson;
        this.position = position;
    }

    public ServerChatEvent(Connection sender, Connection receiver, BaseComponent[] msg, ChatMessageType position)
    {
        super( sender, receiver );
        this.message = msg;
        this.position = position;
    }

    // The getters and setters in this class are configured in such a way to perform lazy conversion
    // between BaseComponent and Json types, but only if required, to prevent unnecessary computation

    public synchronized BaseComponent[] getMessage()
    {
        // Because the BaseComponent representation of a message is mutable,
        // this class has no way of knowing if the Json is the same as the
        // object view so at the cost of a potential extra serialisation, we
        // trash the Json for the sake of consistency.

        if ( message != null )
        {
            messageJson = null;
            return message;
        }
        if ( messageJson != null )
        {
            message = ComponentSerializer.parse( messageJson );
            messageJson = null;
            return message;
        }

        return null;
    }

    public synchronized void setMessage(BaseComponent[] msg)
    {
        this.message = msg;
        this.messageJson = null;
    }

    public synchronized String getMessageJson()
    {
        if ( messageJson != null )
            return messageJson;
        if ( message != null )
            return messageJson = ComponentSerializer.toString( message );

        return null;
    }

    public synchronized void setMessageJson(String msgJson)
    {
        this.message = null;
        this.messageJson = msgJson;
    }
}
