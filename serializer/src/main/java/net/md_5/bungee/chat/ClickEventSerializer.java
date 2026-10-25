package net.md_5.bungee.chat;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Locale;
import lombok.Data;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEventCustom;
import net.md_5.bungee.api.dialog.chat.ShowDialogClickEvent;

@Data
public class ClickEventSerializer
{

    public static final ClickEventSerializer OLD = new ClickEventSerializer( ClickType.OLD );
    public static final ClickEventSerializer NEW = new ClickEventSerializer( ClickType.NEW );
    public static final ClickEventSerializer DIALOG = new ClickEventSerializer( ClickType.DIALOG );
    //
    private final ClickType type;

    public enum ClickType
    {
        OLD, NEW, DIALOG;
    }

    public ClickEvent deserialize(JsonObject clickEvent, JsonDeserializationContext context) throws JsonParseException
    {
        ClickEvent.Action action = ClickEvent.Action.valueOf( clickEvent.get( ( type == ClickType.DIALOG ) ? "type" : "action" ).getAsString().toUpperCase( Locale.ROOT ) );
        switch ( type )
        {
            case NEW:
            case DIALOG:
                switch ( action )
                {
                    case OPEN_URL:
                        return new ClickEvent( action, clickEvent.get( "url" ).getAsString() );
                    case RUN_COMMAND:
                    case SUGGEST_COMMAND:
                        return new ClickEvent( action, clickEvent.get( "command" ).getAsString() );
                    case CHANGE_PAGE:
                        int page = clickEvent.get( "page" ).getAsInt();
                        Preconditions.checkArgument( page >= 0, "Page number has to be positive" );
                        return new ClickEvent( action, Integer.toString( page ) );
                    case SHOW_DIALOG:
                        return context.deserialize( clickEvent.get( "dialog" ), ShowDialogClickEvent.class );
                    case CUSTOM:
                        return new ClickEventCustom( clickEvent.get( "id" ).getAsString(), ( clickEvent.has( "payload" ) ) ? clickEvent.get( "payload" ).getAsString() : null );
                    default:
                        return new ClickEvent( action, ( clickEvent.has( "value" ) ) ? clickEvent.get( "value" ).getAsString() : "" );
                }
            case OLD:
                return new ClickEvent( action, ( clickEvent.has( "value" ) ) ? clickEvent.get( "value" ).getAsString() : "" );
            default:
                throw new IllegalArgumentException( "Unknown serializer type" );
        }
    }

    public JsonElement serialize(ClickEvent src, JsonSerializationContext context)
    {
        JsonObject clickEvent = new JsonObject();
        String actionName = src.getAction().toString().toLowerCase( Locale.ROOT );
        clickEvent.addProperty( ( type == ClickType.DIALOG ) ? "type" : "action", actionName.toLowerCase( Locale.ROOT ) );
        switch ( type )
        {
            case NEW:
            case DIALOG:
                ClickEvent.Action action = ClickEvent.Action.valueOf( actionName.toUpperCase( Locale.ROOT ) );
                switch ( action )
                {
                    case OPEN_URL:
                        clickEvent.addProperty( "url", src.getValue() );
                        break;
                    case RUN_COMMAND:
                    case SUGGEST_COMMAND:
                        clickEvent.addProperty( "command", src.getValue() );
                        break;
                    case CHANGE_PAGE:
                        clickEvent.addProperty( "page", Integer.parseInt( src.getValue() ) );
                        break;
                    case SHOW_DIALOG:
                        clickEvent.add( "dialog", context.serialize( src ) );
                        break;
                    case CUSTOM:
                        ClickEventCustom custom = (ClickEventCustom) src;
                        clickEvent.addProperty( "id", custom.getValue() );
                        if ( custom.getPayload() != null )
                        {
                            clickEvent.addProperty( "payload", custom.getPayload() );
                        }
                        break;
                    default:
                        clickEvent.addProperty( "value", src.getValue() );
                        break;
                }
                break;
            case OLD:
                clickEvent.addProperty( "value", src.getValue() );
                break;
            default:
                throw new IllegalArgumentException( "Unknown serializer type" );
        }

        return clickEvent;
    }
}
