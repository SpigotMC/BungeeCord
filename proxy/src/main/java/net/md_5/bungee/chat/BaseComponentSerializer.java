package net.md_5.bungee.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

import java.util.Arrays;

public class BaseComponentSerializer
{

    protected void deserialize(JsonObject object, BaseComponent component, JsonDeserializationContext context)
    {
        if ( object.has( "color" ) )
        {
            component.setColor( ChatColor.valueOf( object.get( "color" ).getAsString().toUpperCase() ) );
        }
        if ( object.has( "bold" ) )
        {
            component.setBold( object.get( "bold" ).getAsBoolean() );
        }
        if ( object.has( "italic" ) )
        {
            component.setItalic( object.get( "italic" ).getAsBoolean() );
        }
        if ( object.has( "underlined" ) )
        {
            component.setUnderlined( object.get( "underlined" ).getAsBoolean() );
        }
        if ( object.has( "strikethrough" ) )
        {
            component.setUnderlined( object.get( "strikethrough" ).getAsBoolean() );
        }
        if ( object.has( "obfuscated" ) )
        {
            component.setUnderlined( object.get( "obfuscated" ).getAsBoolean() );
        }
        if ( object.has( "extra" ) )
        {
            component.setExtra( Arrays.asList( (BaseComponent[]) context.deserialize( object.get( "extra" ), BaseComponent[].class ) ) );
        }

        //Events
        if ( object.has( "clickEvent" ) )
        {
            JsonObject event = object.getAsJsonObject( "clickEvent" );
            component.setClickEvent( new ClickEvent(
                    ClickEvent.Action.valueOf( event.get( "action" ).getAsString().toUpperCase() ),
                    event.get( "value" ).getAsString() ) );
        }
        if ( object.has( "hoverEvent" ) )
        {
            JsonObject event = object.getAsJsonObject( "hoverEvent" );
            HoverEvent hoverEvent = new HoverEvent();
            hoverEvent.setAction( HoverEvent.Action.valueOf( event.get( "action" ).getAsString().toUpperCase() ) );
            Object res = context.deserialize( event.get( "value" ), BaseComponent.class );
            if ( res instanceof String )
            {
                hoverEvent.setValue( (String) res );
            } else
            {
                hoverEvent.setValue( (BaseComponent) res );
            }
            component.setHoverEvent( hoverEvent );
        }
    }

    protected void serialize(JsonObject object, BaseComponent component, JsonSerializationContext context)
    {
        if ( component.getColorRaw() != null )
        {
            object.addProperty( "color", component.getColorRaw().getName() );
        }
        if ( component.isBoldRaw() != null )
        {
            object.addProperty( "bold", component.isBoldRaw() );
        }
        if ( component.isItalicRaw() != null )
        {
            object.addProperty( "italic", component.isItalicRaw() );
        }
        if ( component.isUnderlinedRaw() != null )
        {
            object.addProperty( "underlined", component.isUnderlinedRaw() );
        }
        if ( component.isStrikethroughRaw() != null )
        {
            object.addProperty( "strikethrough", component.isStrikethroughRaw() );
        }
        if ( component.isObfuscatedRaw() != null )
        {
            object.addProperty( "obfuscated", component.isObfuscatedRaw() );
        }

        if ( component.getExtra() != null )
        {
            object.add( "extra", context.serialize( component.getExtra() ) );
        }

        //Events
        if ( component.getClickEvent() != null )
        {
            JsonObject clickEvent = new JsonObject();
            clickEvent.addProperty( "action", component.getClickEvent().getAction().toString().toLowerCase() );
            clickEvent.addProperty( "value", component.getClickEvent().getValue() );
        }
        if ( component.getHoverEvent() != null )
        {
            JsonObject clickEvent = new JsonObject();
            clickEvent.addProperty( "action", component.getHoverEvent().getAction().toString().toLowerCase() );
            clickEvent.add( "value", context.serialize( component.getHoverEvent().getValue() ) );
        }
    }
}
