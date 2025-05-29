package net.md_5.bungee.chat;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Content;

@RequiredArgsConstructor
public class BaseComponentSerializer
{

    protected final VersionedComponentSerializer serializer;

    protected void deserialize(JsonObject object, BaseComponent component, JsonDeserializationContext context)
    {
        component.applyStyle( context.deserialize( object, ComponentStyle.class ) );

        JsonElement insertion = object.get( "insertion" );
        if ( insertion != null )
        {
            component.setInsertion( insertion.getAsString() );
        }

        //Events
        JsonObject clickEvent;
        boolean newClickEvent = ( clickEvent = object.getAsJsonObject( "click_event" ) ) != null;
        if ( !newClickEvent )
        {
            clickEvent = object.getAsJsonObject( "clickEvent" );
        }
        if ( clickEvent != null )
        {
            if ( newClickEvent )
            {
                component.setClickEvent( ClickEventSerializer.NEW.deserialize( clickEvent, context ) );
            } else
            {
                component.setClickEvent( ClickEventSerializer.OLD.deserialize( clickEvent, context ) );
            }
        }

        JsonObject hoverEventJson;
        boolean newHoverEvent = ( hoverEventJson = object.getAsJsonObject( "hover_event" ) ) != null;
        if ( !newHoverEvent )
        {
            hoverEventJson = object.getAsJsonObject( "hoverEvent" );
        }

        if ( hoverEventJson != null )
        {
            HoverEvent hoverEvent = null;
            HoverEvent.Action action = HoverEvent.Action.valueOf( hoverEventJson.get( "action" ).getAsString().toUpperCase( Locale.ROOT ) );

            if ( newHoverEvent || hoverEventJson.has( "contents" ) )
            {
                // value is only used for text in >= 1.21.5 (its inlined now)
                JsonElement contents = hoverEventJson.get( newHoverEvent ? "value" : "contents" );
                if ( contents != null || ( newHoverEvent && ( action == HoverEvent.Action.SHOW_ITEM || action == HoverEvent.Action.SHOW_ENTITY ) ) )
                {
                    if ( contents == null )
                    {
                        // this is the new inline for SHOW_ITEM and SHOW_ENTITY
                        contents = hoverEventJson;
                    }
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
            } else
            {
                JsonElement value = hoverEventJson.get( "value" );
                if ( value != null )
                {
                    // Plugins previously had support to pass BaseComponent[] into any action.
                    // If the GSON is possible to be parsed as BaseComponent, attempt to parse as so.
                    BaseComponent[] components;
                    if ( value.isJsonArray() )
                    {
                        components = context.deserialize( value, BaseComponent[].class );
                    } else
                    {
                        components = new BaseComponent[]
                        {
                            context.deserialize( value, BaseComponent.class )
                        };
                    }
                    hoverEvent = new HoverEvent( action, components );
                }
            }

            if ( hoverEvent != null )
            {
                component.setHoverEvent( hoverEvent );
            }
        }

        JsonElement extra = object.get( "extra" );
        if ( extra != null )
        {
            component.setExtra( Arrays.asList( context.deserialize( extra, BaseComponent[].class ) ) );
        }
    }

    protected void serialize(JsonObject object, BaseComponent component, JsonSerializationContext context)
    {
        boolean first = false;
        if ( VersionedComponentSerializer.serializedComponents.get() == null )
        {
            first = true;
            VersionedComponentSerializer.serializedComponents.set( Collections.newSetFromMap( new IdentityHashMap<BaseComponent, Boolean>() ) );
        }
        try
        {
            Preconditions.checkArgument( !VersionedComponentSerializer.serializedComponents.get().contains( component ), "Component loop" );
            VersionedComponentSerializer.serializedComponents.get().add( component );

            ComponentStyleSerializer.serializeTo( component.getStyle(), object );

            if ( component.getInsertion() != null )
            {
                object.addProperty( "insertion", component.getInsertion() );
            }

            //Events
            if ( component.getClickEvent() != null )
            {
                switch ( serializer.getVersion() )
                {
                    case V1_21_5:
                        object.add( "click_event", ClickEventSerializer.NEW.serialize( component.getClickEvent(), context ) );
                        break;
                    case V1_16:
                        object.add( "clickEvent", ClickEventSerializer.OLD.serialize( component.getClickEvent(), context ) );
                        break;
                    default:
                        throw new IllegalArgumentException( "Unknown version " + serializer.getVersion() );
                }
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
                    switch ( serializer.getVersion() )
                    {
                        case V1_21_5:
                            if ( component.getHoverEvent().getAction() == HoverEvent.Action.SHOW_ITEM || component.getHoverEvent().getAction() == HoverEvent.Action.SHOW_ENTITY )
                            {
                                JsonObject inlined = context.serialize( ( component.getHoverEvent().getContents().size() == 1 )
                                        ? component.getHoverEvent().getContents().get( 0 ) : component.getHoverEvent().getContents() ).getAsJsonObject();
                                inlined.entrySet().forEach( entry -> hoverEvent.add( entry.getKey(), entry.getValue() ) );
                            } else
                            {
                                hoverEvent.add( "value", context.serialize( ( component.getHoverEvent().getContents().size() == 1 )
                                        ? component.getHoverEvent().getContents().get( 0 ) : component.getHoverEvent().getContents() ) );
                            }
                            break;
                        case V1_16:
                            hoverEvent.add( "contents", context.serialize( ( component.getHoverEvent().getContents().size() == 1 )
                                    ? component.getHoverEvent().getContents().get( 0 ) : component.getHoverEvent().getContents() ) );
                            break;
                        default:
                            throw new IllegalArgumentException( "Unknown version " + serializer.getVersion() );
                    }
                }
                switch ( serializer.getVersion() )
                {
                    case V1_21_5:
                        object.add( "hover_event", hoverEvent );
                        break;
                    case V1_16:
                        object.add( "hoverEvent", hoverEvent );
                        break;
                    default:
                        throw new IllegalArgumentException( "Unknown version " + serializer.getVersion() );
                }
            }

            if ( component.getExtra() != null )
            {
                object.add( "extra", context.serialize( component.getExtra() ) );
            }
        } finally
        {
            VersionedComponentSerializer.serializedComponents.get().remove( component );
            if ( first )
            {
                VersionedComponentSerializer.serializedComponents.set( null );
            }
        }
    }
}
