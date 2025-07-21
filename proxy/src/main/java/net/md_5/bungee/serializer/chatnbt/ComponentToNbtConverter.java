package net.md_5.bungee.serializer.chatnbt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEventCustom;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.dialog.chat.ShowDialogClickEvent;
import net.md_5.bungee.chat.ChatVersion;
import net.md_5.bungee.chat.ClickEventSerializer;
import net.md_5.bungee.nbt.Tag;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.nbt.type.ByteTag;
import net.md_5.bungee.nbt.type.CompoundTag;
import net.md_5.bungee.nbt.type.IntTag;
import net.md_5.bungee.nbt.type.ListTag;
import net.md_5.bungee.nbt.type.StringTag;

public class ComponentToNbtConverter
{
    public static CompoundTag toNbt(BaseComponent component)
    {
        CompoundTag nbt = baseComponentToNbt( component, ChatVersion.V1_21_5 );
        if ( component instanceof TextComponent )
        {
            nbt.put( "text", new StringTag( ( (TextComponent) component ).getText() ) );
        } else if ( component instanceof TranslatableComponent )
        {
            TranslatableComponent trans = (TranslatableComponent) component;
            nbt.put( "translate", new StringTag( trans.getTranslate() ) );
            List<BaseComponent> with = trans.getWith();
            if ( with != null )
            {
                nbt.put( "with", toNbt( with ) );
            }
        }
        return nbt;
    }

    private static ListTag toNbt(List<BaseComponent> components)
    {
        ListTag nbtList = new ListTag( new ArrayList<>( components.size() ), Tag.COMPOUND );
        components.stream()
                .map( baseComponent -> baseComponentToNbt( baseComponent, ChatVersion.V1_21_5 ) )
                .forEach( nbt -> nbtList.getValue().add( nbt ) );
        return nbtList;
    }

    public static ListTag toNbt(BaseComponent[] components)
    {
        ListTag nbtList = new ListTag( new ArrayList<>( components.length ), Tag.COMPOUND );
        Arrays.stream( components )
                .map( baseComponent -> baseComponentToNbt( baseComponent, ChatVersion.V1_21_5 ) )
                .forEach( nbt -> nbtList.getValue().add( nbt ) );
        return nbtList;
    }

    private static CompoundTag baseComponentToNbt(BaseComponent component, ChatVersion version)
    {
        CompoundTag nbt = new CompoundTag( new HashMap<>() );
        styleToNbt( component.getStyle(), nbt );
        putStringIfNotNull( nbt, "insertion", component.getInsertion() );
        ClickEvent clickEvent = component.getClickEvent();
        if ( clickEvent != null )
        {
            switch ( version )
            {
                case V1_21_5:
                    nbt.put( "click_event", clickEventToNbt( clickEvent, ClickEventSerializer.ClickType.NEW ) );
                    break;
                case V1_16:
                    nbt.put( "clickEvent", clickEventToNbt( clickEvent, ClickEventSerializer.ClickType.OLD ) );
                    break;
                default:
                    throw new IllegalArgumentException( "Unknown version " + version );
            }
        }
        HoverEvent hoverEvent = component.getHoverEvent();
        if ( hoverEvent != null )
        {
            CompoundTag nbtHover = hoverEventToNbt( hoverEvent, version );
            switch ( version )
            {
                case V1_21_5:
                    nbt.put( "hover_event", nbtHover );
                    break;
                case V1_16:
                    nbt.put( "hoverEvent", nbtHover );
                    break;
                default:
                    throw new IllegalArgumentException( "Unknown version " + version );
            }
        }
        List<BaseComponent> extras = component.getExtra();
        if ( extras != null && !extras.isEmpty() )
        {
            ArrayList<TypedTag> extrasList = new ArrayList<>();
            ListTag extrasNbt = new ListTag( extrasList, Tag.COMPOUND );
            extras.forEach( extra -> extrasList.add( baseComponentToNbt( extra, version ) ) );
            nbt.put( "extra", extrasNbt );
        }
        return nbt;
    }

    private static CompoundTag hoverEventToNbt(HoverEvent hoverEvent, ChatVersion version)
    {
        CompoundTag nbtHover = new CompoundTag( new HashMap<>() );
        nbtHover.put( "action", new StringTag( hoverEvent.getAction().toString().toLowerCase( Locale.ROOT ) ) );
        if ( hoverEvent.isLegacy() )
        {
            CompoundTag hoverContentNbt = new CompoundTag( new HashMap<>() );
            hoverContentToNbt( hoverEvent.getContents().get( 0 ), hoverContentNbt, version );
            nbtHover.put( "value", hoverContentNbt );
        } else
        {
            List<Content> contents = hoverEvent.getContents();
            switch ( version )
            {
                case V1_21_5:
                    if ( hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM || hoverEvent.getAction() == HoverEvent.Action.SHOW_ENTITY )
                    {
                        if ( contents.size() == 1 )
                        {
                            // inline
                            hoverContentToNbt( contents.get( 0 ), nbtHover, version );
                        } else
                        {
                            nbtHover.put( "value", contentListToNbt( contents, version ) );
                        }
                    } else
                    {
                        nbtHover.put( "value", contentListToNbtSingleSpecial( contents, version ) );
                    }
                    break;
                case V1_16:
                    nbtHover.put( "contents", contentListToNbtSingleSpecial( contents, version ) );
                    break;
                default:
                    throw new IllegalArgumentException( "Unknown version " + version );
            }
        }
        return nbtHover;
    }

    private static TypedTag contentListToNbtSingleSpecial(List<Content> contents, ChatVersion version)
    {
        if ( contents.size() == 1 )
        {
            CompoundTag contentNbt = new CompoundTag( new HashMap<>() );
            hoverContentToNbt( contents.get( 0 ), contentNbt, version );
            return contentNbt;
        }
        return contentListToNbt( contents, version );
    }

    private static ListTag contentListToNbt(List<Content> contents, ChatVersion version)
    {
        ListTag listTag = new ListTag( new ArrayList<>( contents.size() ), Tag.COMPOUND );
        contents.stream()
                .map( content -> {
                    CompoundTag contentNbt = new CompoundTag( new HashMap<>() );
                    hoverContentToNbt( content, contentNbt, version );
                    return contentNbt;
                } )
                .forEach( listTag.getValue()::add );
        return listTag;
    }

    private static void hoverContentToNbt(Content contents, CompoundTag nbt, ChatVersion version)
    {
        if ( contents instanceof Text )
        {
            hoverTextToNbt( (Text) contents, nbt );
        } else if ( contents instanceof Item )
        {
            hoverItemToNbt( (Item) contents, nbt );
        } else if ( contents instanceof Entity )
        {
            hoverEntityToNbt( (Entity) contents, nbt, version );
        } else
        {
            throw new IllegalArgumentException( "Unknown hover content type: " + contents.getClass().getName() );
        }
    }

    private static void hoverTextToNbt(Text text, CompoundTag nbt)
    {
        Object value = text.getValue();
        if ( value instanceof BaseComponent )
        {
            nbt.put( "text", toNbt( (BaseComponent) value ) );
        } else if ( value instanceof BaseComponent[] )
        {
            nbt.put( "text", toNbt( (BaseComponent[]) value ) );
        } else
        {
            nbt.put( "text", new StringTag( value.toString() ) );
        }
    }

    private static void hoverItemToNbt(Item item, CompoundTag nbt)
    {
        String id = item.getId();
        if ( id == null )
        {
            id = "minecraft:air";
        }
        nbt.put( "id", new StringTag( id ) );
        if ( item.getCount() != -1 )
        {
            putIntIfNotNull( nbt, "Count", item.getCount() );
        }
        if ( item.getTag() != null )
        {
            nbt.put( "tag", new StringTag( item.getTag().getNbt() ) );
        }
    }

    private static void hoverEntityToNbt(Entity entity, CompoundTag nbt, ChatVersion version)
    {
        String id = entity.getType();
        if ( id == null )
        {
            id = "minecraft:pig";
        }
        switch ( version )
        {
            case V1_21_5:
                nbt.put( "id", new StringTag( id ) );
                nbt.put( "uuid", new StringTag( entity.getId() ) );
                break;
            case V1_16:
                nbt.put( "type", new StringTag( entity.getType() ) );
                nbt.put( "id", new StringTag( entity.getId() ) );
                break;
            default:
                throw new IllegalArgumentException( "Unknown version " + version );
        }
        if ( entity.getName() != null )
        {
            nbt.put( "name", toNbt( entity.getName() ) );
        }
    }

    private static void styleToNbt(ComponentStyle style, CompoundTag nbt)
    {
        putBooleanIfNotNull( nbt, "bold", style.isBoldRaw() );
        putBooleanIfNotNull( nbt, "italic", style.isItalicRaw() );
        putBooleanIfNotNull( nbt, "underlined", style.isUnderlinedRaw() );
        putBooleanIfNotNull( nbt, "strikethrough", style.isStrikethroughRaw() );
        putBooleanIfNotNull( nbt, "obfuscated", style.isObfuscatedRaw() );
        if ( style.hasColor() && style.getColor().getColor() != null )
        {
            nbt.put( "color", new StringTag( style.getColor().getName() ) );
        }
        if ( style.hasShadowColor() )
        {
            nbt.put( "shadow_color", new IntTag( style.getShadowColor().getRGB() ) );
        }
        putStringIfNotNull( nbt, "font", style.getFont() );
    }

    private static CompoundTag clickEventToNbt(ClickEvent event, ClickEventSerializer.ClickType type)
    {
        CompoundTag nbt = new CompoundTag( new HashMap<>() );
        String actionName = event.getAction().toString().toLowerCase( Locale.ROOT );
        nbt.put( ( type == ClickEventSerializer.ClickType.DIALOG ) ? "type" : "action", new StringTag( actionName ) );
        switch ( type )
        {
            case NEW:
            case DIALOG:
                switch ( event.getAction() )
                {
                    case OPEN_URL:
                        nbt.put( "url", new StringTag( event.getValue() ) );
                        break;
                    case RUN_COMMAND:
                    case SUGGEST_COMMAND:
                        nbt.put( "command", new StringTag( event.getValue() ) );
                        break;
                    case CHANGE_PAGE:
                        int page = Integer.parseInt( event.getValue() );
                        if ( page < 0 )
                        {
                            throw new IllegalArgumentException( "Page number has to be positive" );
                        }
                        nbt.put( "page", new IntTag( page ) );
                        break;
                    case SHOW_DIALOG:
                        if ( event instanceof ShowDialogClickEvent ) {
                            showDialogClickEventToNbt( (ShowDialogClickEvent) event, nbt );
                        } else {
                            nbt.put( "dialog", new StringTag( event.getValue() ) );
                        }
                        break;
                    case CUSTOM:
                        ClickEventCustom custom = (ClickEventCustom) event;
                        nbt.put( "id", new StringTag( custom.getValue() ) );
                        if ( custom.getPayload() != null )
                        {
                            nbt.put( "payload", new StringTag( custom.getPayload() ) );
                        }
                        break;
                    default:
                        nbt.put( "value", new StringTag( event.getValue() ) );
                }
                break;
            case OLD:
                nbt.put( "value", new StringTag( event.getValue() ) );
                break;
            default:
                throw new IllegalArgumentException( "Unknown click event type: " + type );
        }
        return nbt;
    }

    private static void showDialogClickEventToNbt(ShowDialogClickEvent event, CompoundTag nbt)
    {
        if (event.getReference() != null) {
            nbt.put("reference", new StringTag(event.getReference()));
            return;
        }
        //TODO handle dialog content
    }

    private static void putBooleanIfNotNull(CompoundTag nbt, String key, Boolean value)
    {
        if ( value != null )
        {
            nbt.put( key, new ByteTag( value ? (byte) 1 : (byte) 0 ) );
        }
    }

    private static void putIntIfNotNull(CompoundTag nbt, String key, Integer value)
    {
        if ( value != null )
        {
            nbt.put( key, new IntTag( value ) );
        }
    }

    private static void putStringIfNotNull(CompoundTag nbt, String key, String value)
    {
        if ( value != null )
        {
            nbt.put( key, new StringTag( value ) );
        }
    }
}
