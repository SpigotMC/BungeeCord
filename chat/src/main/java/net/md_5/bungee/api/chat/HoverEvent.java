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
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class HoverEvent
{

    /**
     * The action of this event.
     */
    private final Action action;
    /**
     * List of contents to provide for this event.
     */
    private final List<Content> contents;
    /**
     * Returns whether this hover event is prior to 1.16
     */
    @Setter
    private boolean legacy = false;

    /**
     * Creates event with an action and a list of contents.
     *
     * @param action action of this event
     * @param contents array of contents, provide at least one
     */
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
        this.action = action;
        this.contents = new ArrayList<>( Collections.singletonList( new ContentText( value ) ) );
        this.legacy = true;
    }

    /**
     * Adds a content to this hover event.
     *
     * @param content the content add
     * @throws IllegalArgumentException if is a legacy component and already has
     * a content
     * @throws UnsupportedOperationException if content action does not match
     * hover event action
     */
    public void addContent(Content content) throws UnsupportedOperationException
    {
        Preconditions.checkArgument( !legacy || contents.size() == 0, "Legacy HoverEvent may not have more than one content" );
        content.assertAction( action );
        contents.add( content );
    }

    @ToString
    @EqualsAndHashCode
    public abstract static class Content
    {

        /**
         * Required action for this content type.
         *
         * @return action
         */
        abstract Action requiredAction();

        /**
         * Tests this content against an action
         *
         * @param input input to test
         * @throws UnsupportedOperationException if action incompatible
         */
        void assertAction(Action input) throws UnsupportedOperationException
        {
            if ( input != requiredAction() )
            {
                throw new UnsupportedOperationException( "Action " + input + " not compatible! Expected " + requiredAction() );
            }
        }
    }

    @Data
    @ToString
    public static class ContentText extends Content
    {

        /**
         * The value.
         *
         * May be a component or raw text depending on constructor used.
         */
        private Object value;

        public ContentText(BaseComponent[] value)
        {
            this.value = value;
        }

        public ContentText(String value)
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
            if ( value instanceof BaseComponent[] )
            {
                return o instanceof ContentText
                        && ( (ContentText) o ).value instanceof BaseComponent[]
                        && Arrays.equals( (BaseComponent[]) value, (BaseComponent[]) ( (ContentText) o ).value );
            } else
            {
                return value.equals( o );
            }
        }

        @Override
        public int hashCode()
        {
            return ( value instanceof BaseComponent[] ) ? Arrays.hashCode( (BaseComponent[]) value ) : value.hashCode();
        }

        public static class Serializer implements JsonSerializer<ContentText>, JsonDeserializer<ContentText>
        {

            @Override
            public ContentText deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
            {
                if ( element.isJsonArray() )
                {
                    return new ContentText( context.<BaseComponent[]>deserialize( element, BaseComponent[].class ) );
                } else if ( element.getAsJsonObject().isJsonPrimitive() )
                {
                    return new ContentText( element.getAsJsonObject().getAsJsonPrimitive().getAsString() );
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode(callSuper = true)
    public static class ContentEntity extends Content
    {

        /**
         * Namespaced entity ID.
         *
         * Will use 'minecraft:pig' if null.
         */
        private String type;
        /**
         * Entity UUID in hyphenated hexadecimal format.
         *
         * Should be valid UUID. TODO : validate?
         */
        @NonNull
        private String id;
        /**
         * Name to display as the entity.
         *
         * This is optional and will be hidden if null.
         */
        private BaseComponent name;

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

                return new ContentEntity(
                        ( value.has( "type" ) ) ? value.get( "type" ).getAsString() : null,
                        value.get( "id" ).getAsString(),
                        ( value.has( "name" ) ) ? context.deserialize( value.get( "name" ), BaseComponent.class ) : null
                );
            }

            @Override
            public JsonElement serialize(ContentEntity content, Type type, JsonSerializationContext context)
            {
                JsonObject object = new JsonObject();
                object.addProperty( "type", ( content.getType() != null ) ? content.getType() : "minecraft:pig" );
                object.addProperty( "id", content.getId() );
                if ( content.getName() != null )
                {
                    object.add( "name", context.serialize( content.getName() ) );
                }
                return object;
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode(callSuper = true)
    public static class ContentItem extends Content
    {

        /**
         * Namespaced item ID. Will use 'minecraft:air' if null.
         */
        private String id;
        /**
         * Optional. Size of the item stack.
         */
        private int count = -1;
        /**
         * Optional. Item tag.
         */
        private ItemTag tag;

        @Override
        Action requiredAction()
        {
            return Action.SHOW_ITEM;
        }

        public static class Serializer implements JsonSerializer<ContentItem>, JsonDeserializer<ContentItem>
        {

            @Override
            public ContentItem deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject value = element.getAsJsonObject();

                return new ContentItem(
                        ( value.has( "id" ) ) ? value.get( "id" ).getAsString() : null,
                        ( value.has( "Count" ) ) ? value.get( "Count" ).getAsInt() : -1,
                        ( value.has( "tag" ) ) ? context.deserialize( value.get( "tag" ), ItemTag.class ) : null
                );
            }

            @Override
            public JsonElement serialize(ContentItem content, Type type, JsonSerializationContext context)
            {
                JsonObject object = new JsonObject();
                object.addProperty( "id", ( content.getId() == null ) ? "minecraft:air" : content.getId() );
                if ( content.getCount() != -1 )
                {
                    object.addProperty( "Count", content.getCount() );
                }
                if ( content.getTag() != null )
                {
                    object.add( "tag", context.serialize( content.getTag() ) );
                }
                return object;
            }
        }
    }

    public enum Action
    {

        SHOW_TEXT,
        SHOW_ITEM,
        SHOW_ENTITY,
        /**
         * Removed since 1.12. Advancements instead simply use show_text. The ID
         * of an achievement or statistic to display. Example: new
         * ComponentText( "achievement.openInventory" )
         */
        @Deprecated
        SHOW_ACHIEVEMENT,
    }

    /**
     * Gets the appropriate {@link Content} class for an {@link Action} for the
     * GSON serialization
     *
     * @param action the action to get for
     * @param array if to return the arrayed class
     * @return the class
     */
    public static Class<?> getClass(HoverEvent.Action action, boolean array)
    {
        Preconditions.checkArgument( action != null, "action" );

        switch ( action )
        {
            case SHOW_TEXT:
                return ( array ) ? HoverEvent.ContentText[].class : HoverEvent.ContentText.class;
            case SHOW_ENTITY:
                return ( array ) ? HoverEvent.ContentEntity[].class : HoverEvent.ContentEntity.class;
            case SHOW_ITEM:
                return ( array ) ? HoverEvent.ContentItem[].class : HoverEvent.ContentItem.class;
            default:
                throw new UnsupportedOperationException( "Action '" + action.name() + " not supported" );
        }
    }
}
