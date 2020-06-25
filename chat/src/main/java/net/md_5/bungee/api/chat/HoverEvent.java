package net.md_5.bungee.api.chat;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
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
        if ( action == Action.SHOW_TEXT )
        {
            Preconditions.checkArgument( ( content instanceof ContentText ), "Content type incompatible with action" );
        }

        contents.add( content );
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    public abstract static class Content<V>
    {

        protected final V value;

        protected Content(V value)
        {
            this.value = value;
        }

        public static class Serializer implements JsonSerializer<Content>, JsonDeserializer<Content>
        {

            @Override
            public Content deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
            {
                // TODO: Better way of doing this.
                // because when we add more content types the element might not be a BaseComponent array
                return new ContentText( context.deserialize( element, BaseComponent[].class ) );
            }

            @Override
            public JsonElement serialize(Content content, Type type, JsonSerializationContext context)
            {
                return context.serialize( content.value );
            }
        }
    }

    @ToString
    public static class ContentText extends Content<BaseComponent[]>
    {

        public ContentText(BaseComponent[] value)
        {
            super( value );
        }

        @Override
        public boolean equals(Object o)
        {
            return o instanceof ContentText && Arrays.equals( getValue(), ( (ContentText) o ).getValue() );
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode( value );
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
}
