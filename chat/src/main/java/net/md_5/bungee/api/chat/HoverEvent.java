package net.md_5.bungee.api.chat;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class HoverEvent
{

    private final Action action;
    private final List<Content> contents;
    @Setter
    private boolean legacy = false;

    public HoverEvent(Action action, Content... contents)
    {
        Preconditions.checkArgument( contents.length != 0, "Must contain at least one content" );
        this.action = action;
        this.contents = new ArrayList<>();
        for ( Content it : contents )
        {
            addContent( it );
        }
    }

    /**
     * Legacy constructor to create hover event.
     *
     * @param action the action
     * @param value the value
     * @deprecated {@link #HoverEvent(Action, Content[])}
     */
    @Deprecated
    public HoverEvent(Action action, BaseComponent[] value)
    {
        // Old plugins may have somehow hacked BaseComponent[] into
        // anything other than SHOW_TEXT action. Ideally continue support.
        this( action, new ContentText( value ) );
        this.legacy = true;
    }

    public void addContent(Content content)
    {
        Preconditions.checkArgument( !legacy || contents.size() == 0, "Legacy HoverEvent may not have more than one content" );
        content.assertAction( action );
        contents.add( content );
    }

    @ToString
    @EqualsAndHashCode
    public abstract static class Content<V>
    {

        abstract Action requiredAction();

        public void assertAction(Action input)
        {
            if ( input != requiredAction() )
            {
                throw new IllegalArgumentException( "Action " + input + " not compatible! Expected " + requiredAction() );
            }
        }
    }

    @Getter
    @ToString
    public static class ContentText extends Content<BaseComponent[]>
    {

        private final BaseComponent[] value;

        public ContentText(BaseComponent[] value)
        {
            this.value = value;
        }

        @Override
        Action requiredAction()
        {
            return Action.SHOW_TEXT;
        }

        @Override
        public boolean equals(Object o)
        {
            return o instanceof ContentText && Arrays.equals( value, ( (ContentText) o ).value );
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode( value );
        }

        public static class Serializer implements JsonSerializer<ContentText>, JsonDeserializer<ContentText>
        {

            @Override
            public ContentText deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
            {
                if ( element.isJsonArray() )
                {
                    return new ContentText( context.deserialize( element, BaseComponent[].class ) );
                } else
                {
                    return new ContentText( new BaseComponent[]
                    {
                        context.deserialize( element, BaseComponent.class )
                    } );
                }
            }

            @Override
            public JsonElement serialize(ContentText content, Type type, JsonSerializationContext context)
            {
                return context.serialize( content.getValue() );
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    @EqualsAndHashCode(callSuper = true)
    public static class ContentEntity extends Content<String[]>
    {

        private final String type;
        private final String id;
        private final BaseComponent[] name;

        @Override
        Action requiredAction()
        {
            return Action.SHOW_ENTITY;
        }

        public static class Serializer implements JsonSerializer<ContentEntity>, JsonDeserializer<ContentEntity>
        {

            @Override
            public ContentEntity deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject value = element.getAsJsonObject();

                BaseComponent[] name;
                if ( value.get( "name" ).isJsonArray() )
                {
                    name = context.deserialize( value.get( "name" ), BaseComponent[].class );
                } else
                {
                    name = new BaseComponent[]
                    {
                        context.deserialize( value.get( "name" ), BaseComponent.class )
                    };
                }

                return new ContentEntity(
                        value.get( "type" ).getAsString(),
                        value.get( "id" ).getAsString(),
                        name
                );
            }

            @Override
            public JsonElement serialize(ContentEntity content, Type type, JsonSerializationContext context)
            {
                JsonObject object = new JsonObject();
                object.addProperty( "type", content.getType() );
                object.addProperty( "id", content.getId() );
                object.add( "name", context.serialize( content.getName() ) );
                return object;
            }
        }
    }

    // TODO: Support for other the other Action's (ContentEntity|ContentItem)

    public enum Action
    {

        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM,
        SHOW_ENTITY
    }

    public static Class<?> getClass(HoverEvent.Action action, boolean array)
    {
        if ( action == HoverEvent.Action.SHOW_TEXT )
        {
            return ( array ) ? HoverEvent.ContentText[].class : HoverEvent.ContentText.class;
        } else if ( action == HoverEvent.Action.SHOW_ENTITY )
        {
            return ( array ) ? HoverEvent.ContentEntity[].class : HoverEvent.ContentEntity.class;
        } else
        {
            return null;
        }
    }
}
