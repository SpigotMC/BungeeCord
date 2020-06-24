package net.md_5.bungee.api.chat;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.chat.nbt.NbtEntity;
import net.md_5.bungee.api.chat.nbt.NbtItem;

@Getter
@ToString
@EqualsAndHashCode
public final class HoverEvent
{

    private final Action action;
    private final List<Content<?>> contents;

    @Deprecated
    public HoverEvent(Action action, BaseComponent[] value)
    {
        this( action );
        this.addText( value );
    }

    public HoverEvent(Action action)
    {
        this( action, new ArrayList<>() );
    }

    public HoverEvent(Action action, List<Content<?>> contents)
    {
        this.action = action;
        this.contents = contents;
    }

    public void addContents(List<Content<?>> contents)
    {
        this.contents.addAll( contents );
    }

    public void addText(BaseComponent[] text)
    {
        this.contents.add( new ContentText( text ) );
    }

    public void addItem(NbtItem nbt)
    {
        this.contents.add( new ContentItem( nbt ) );
    }

    public void addEntity(NbtEntity nbt)
    {
        this.contents.add( new ContentEntity( nbt ) );
    }

    public enum Action
    {

        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM,
        SHOW_ENTITY
    }

    @Getter
    public static abstract class Content<V>
    {

        private final Action action;
        private final V value;

        public Content(Action action, V value)
        {
            this.action = action;
            this.value = value;
        }
    }

    public static class ContentItem extends Content<NbtItem>
    {

        public ContentItem(NbtItem value)
        {
            super( Action.SHOW_ITEM, value );
        }
    }

    public static class ContentEntity extends Content<NbtEntity>
    {

        public ContentEntity(NbtEntity value)
        {
            super( Action.SHOW_ENTITY, value );
        }

        @Override
        public Action getAction()
        {
            return Action.SHOW_ENTITY;
        }
    }

    public static class ContentText extends Content<BaseComponent[]>
    {

        public ContentText(BaseComponent[] value)
        {
            super( Action.SHOW_TEXT, value );
        }

        @Override
        public Action getAction()
        {
            return Action.SHOW_TEXT;
        }
    }
}
