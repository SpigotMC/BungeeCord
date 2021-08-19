package net.md_5.bungee.chat;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Content;

public class BaseComponentSerializer
{

    protected void deserialize(JsonObject object, BaseComponent component, JsonDeserializationContext context)
    {
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
            component.setStrikethrough( object.get( "strikethrough" ).getAsBoolean() );
        }
        if ( object.has( "obfuscated" ) )
        {
            component.setObfuscated( object.get( "obfuscated" ).getAsBoolean() );
        }
        if ( object.has( "color" ) )
        {
            component.setColor( ChatColor.of( object.get( "color" ).getAsString() ) );
        }
        if ( object.has( "insertion" ) )
        {
            component.setInsertion( object.get( "insertion" ).getAsString() );
        }

        //Events
        if ( object.has( "clickEvent" ) )
        {
            JsonObject event = object.getAsJsonObject( "clickEvent" );
            component.setClickEvent( new ClickEvent(
                    ClickEvent.Action.valueOf( event.get( "action" ).getAsString().toUpperCase( Locale.ROOT ) ),
                    ( event.has( "value" ) ) ? event.get( "value" ).getAsString() : "" ) );
        }
        if ( object.has( "hoverEvent" ) )
        {
            JsonObject event = object.getAsJsonObject( "hoverEvent" );
            HoverEvent hoverEvent = null;
            HoverEvent.Action action = HoverEvent.Action.valueOf( event.get( "action" ).getAsString().toUpperCase( Locale.ROOT ) );

            for ( String type : Arrays.asList( "value", "contents" ) )
            {
                if ( !event.has( type ) )
                {
                    continue;
                }
                JsonElement contents = event.get( type );
                try
                {

                    // Plugins previously had support to pass BaseComponent[] into any action.
                    // If the GSON is possible to be parsed as BaseComponent, attempt to parse as so.
                    BaseComponent[] components;
                    if ( contents.isJsonArray() )
                    {
                        components = context.deserialize( contents, BaseComponent[].class );
                    } else
                    {
                        components = new BaseComponent[]
                        {
                            context.deserialize( contents, BaseComponent.class )
                        };
                    }
                    hoverEvent = new HoverEvent( action, components );
                } catch ( JsonParseException ex )
                {
                    Content[] list;
                    if ( contents.isJsonArray() )
                    {
                        list = context.deserialize( contents, HoverEvent.getClass( action, true ) );
                    } else
                    {
                        list = new Content[]
                        {
                            context.deserialize( contents, HoverEvent.getClass( action, false ) )
                        };
                    }
                    hoverEvent = new HoverEvent( action, new ArrayList<>( Arrays.asList( list ) ) );
                }

                // stop the loop as soon as either one is found
                break;
            }
            if ( hoverEvent != null )
            {
                component.setHoverEvent( hoverEvent );
            }
        }

        if ( object.has( "font" ) )
        {
            component.setFont( object.get( "font" ).getAsString() );
        }
        if ( object.has( "extra" ) )
        {
            component.setExtra( Arrays.asList( context.<BaseComponent[]>deserialize( object.get( "extra" ), BaseComponent[].class ) ) );
        }
    }

    protected void serialize(JsonObject object, BaseComponent component, JsonSerializationContext context)
    {
        boolean first = false;
        if ( ComponentSerializer.serializedComponents.get() == null )
        {
            first = true;
            ComponentSerializer.serializedComponents.set( Collections.newSetFromMap( new IdentityHashMap<BaseComponent, Boolean>() ) );
        }
        try
        {
            Preconditions.checkArgument( !ComponentSerializer.serializedComponents.get().contains( component ), "Component loop" );
            ComponentSerializer.serializedComponents.get().add( component );
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
            if ( component.getColorRaw() != null )
            {
                object.addProperty( "color", component.getColorRaw().getName() );
            }
            if ( component.getInsertion() != null )
            {
                object.addProperty( "insertion", component.getInsertion() );
            }

            //Events
            if ( component.getClickEvent() != null )
            {
                JsonObject clickEvent = new JsonObject();
                clickEvent.addProperty( "action", component.getClickEvent().getAction().toString().toLowerCase( Locale.ROOT ) );
                clickEvent.addProperty( "value", component.getClickEvent().getValue() );
                object.add( "clickEvent", clickEvent );
            }
            if ( component.getHoverEvent() != null )
            {
                JsonObject hoverEvent = new JsonObject();
                hoverEvent.addProperty( "action", component.getHoverEvent().getAction().toString().toLowerCase( Locale.ROOT ) );
                if ( component.getHoverEvent().isLegacy() )
                {
                    hoverEvent.add( "value", context.serialize( component.getHoverEvent().getContents().get( 0 ) ) );
                } else
                {
                    hoverEvent.add( "contents", context.serialize( ( component.getHoverEvent().getContents().size() == 1 )
                            ? component.getHoverEvent().getContents().get( 0 ) : component.getHoverEvent().getContents() ) );
                }
                object.add( "hoverEvent", hoverEvent );
            }

            if ( component.getFontRaw() != null )
            {
                object.addProperty( "font", component.getFontRaw() );
            }
            if ( component.getExtra() != null )
            {
                object.add( "extra", context.serialize( component.getExtra() ) );
            }
        } finally
        {
            ComponentSerializer.serializedComponents.get().remove( component );
            if ( first )
            {
                ComponentSerializer.serializedComponents.set( null );
            }
        }
    }
}
