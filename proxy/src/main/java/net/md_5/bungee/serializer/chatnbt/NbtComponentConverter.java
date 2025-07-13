package net.md_5.bungee.serializer.chatnbt;

import com.google.common.base.Preconditions;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEventCustom;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.ScoreComponent;
import net.md_5.bungee.api.chat.SelectorComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ClickEventSerializer;
import net.md_5.bungee.nbt.Tag;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.nbt.type.ByteArrayTag;
import net.md_5.bungee.nbt.type.ByteTag;
import net.md_5.bungee.nbt.type.CompoundTag;
import net.md_5.bungee.nbt.type.DoubleTag;
import net.md_5.bungee.nbt.type.FloatTag;
import net.md_5.bungee.nbt.type.IntArrayTag;
import net.md_5.bungee.nbt.type.IntTag;
import net.md_5.bungee.nbt.type.ListTag;
import net.md_5.bungee.nbt.type.LongArrayTag;
import net.md_5.bungee.nbt.type.LongTag;
import net.md_5.bungee.nbt.type.ShortTag;
import net.md_5.bungee.nbt.type.StringTag;

public class NbtComponentConverter
{
    /**
     * Converts a NBT tag to a BaseComponent.
     *
     * @param nbt the NBT tag to convert
     * @return a BaseComponent representing the NBT tag
     */
    public static BaseComponent toComponent(TypedTag nbt)
    {
        switch ( nbt.getId() )
        {
            case Tag.END:
                return null; //TODO null or empty text component?
            case Tag.BYTE:
            case Tag.SHORT:
            case Tag.INT:
            case Tag.LONG:
            case Tag.FLOAT:
            case Tag.DOUBLE:
            case Tag.STRING:
                return new TextComponent( nbtToString( nbt ) );
            case Tag.BYTE_ARRAY:
                byte[] value = ( (ByteArrayTag) nbt ).getValue();
                BaseComponent[] extras = new BaseComponent[ value.length ];
                for ( int i = 0, valueLength = value.length; i < valueLength; i++ )
                {
                    extras[ i ] = new TextComponent( String.valueOf( value[ i ] ) );
                }
                return new TextComponent( extras );
            case Tag.LIST:
                ListTag list = (ListTag) nbt;
                return new TextComponent( list.getValue().stream().map( NbtComponentConverter::toComponent ).toArray( BaseComponent[]::new ) );
            case Tag.COMPOUND:
                return componentFromCompound( (CompoundTag) nbt );
            case Tag.INT_ARRAY:
                int[] intArray = ( (IntArrayTag) nbt ).getValue();
                BaseComponent[] intComponents = new BaseComponent[ intArray.length ];
                for ( int i = 0, length = intArray.length; i < length; i++ )
                {
                    intComponents[ i ] = new TextComponent( String.valueOf( intArray[ i ] ) );
                }
                return new TextComponent( intComponents );
            case Tag.LONG_ARRAY:
                long[] longArray = ( (LongArrayTag) nbt ).getValue();
                BaseComponent[] longComponents = new BaseComponent[ longArray.length ];
                for ( int i = 0, length = longArray.length; i < length; i++ )
                {
                    longComponents[ i ] = new TextComponent( String.valueOf( longArray[ i ] ) );
                }
                return new TextComponent( longComponents );
            default:
                throw new IllegalArgumentException( "Unsupported NBT type for component conversion: " + nbt.getId() );
        }
    }

    /**
     * Converts a NBT tag to a BaseComponent array.
     *
     * @param nbt the NBT tag to convert
     * @return an array of BaseComponents representing the NBT tag
     * @see #toComponent(net.md_5.bungee.nbt.TypedTag)
     * @deprecated BaseComponent arrays are deprecated in favor of using a single BaseComponent.
     */
    @Deprecated
    public static BaseComponent[] toComponentArray(TypedTag nbt)
    {
        switch ( nbt.getId() )
        {
            case Tag.END:
                return null; //TODO null or empty array or array with 1 empty text component?
            case Tag.BYTE:
            case Tag.SHORT:
            case Tag.INT:
            case Tag.LONG:
            case Tag.FLOAT:
            case Tag.DOUBLE:
            case Tag.STRING:
            case Tag.COMPOUND:
                return new BaseComponent[]{toComponent( nbt )};
            case Tag.BYTE_ARRAY:
                byte[] value = ( (ByteArrayTag) nbt ).getValue();
                BaseComponent[] convertedBytes = new BaseComponent[ value.length ];
                for ( int i = 0, valueLength = value.length; i < valueLength; i++ )
                {
                    convertedBytes[ i ] = new TextComponent( String.valueOf( value[ i ] ) );
                }
                return convertedBytes;
            case Tag.LIST:
                ListTag list = (ListTag) nbt;
                return list.getValue().stream().map( NbtComponentConverter::toComponent ).toArray( BaseComponent[]::new );
            case Tag.INT_ARRAY:
                int[] intArray = ( (IntArrayTag) nbt ).getValue();
                BaseComponent[] convertedInts = new BaseComponent[ intArray.length ];
                for ( int i = 0, length = intArray.length; i < length; i++ )
                {
                    convertedInts[ i ] = new TextComponent( String.valueOf( intArray[ i ] ) );
                }
                return convertedInts;
            case Tag.LONG_ARRAY:
                long[] longArray = ( (LongArrayTag) nbt ).getValue();
                BaseComponent[] convertedLongs = new BaseComponent[ longArray.length ];
                for ( int i = 0, length = longArray.length; i < length; i++ )
                {
                    convertedLongs[ i ] = new TextComponent( String.valueOf( longArray[ i ] ) );
                }
                return convertedLongs;
            default:
                throw new IllegalArgumentException( "Unsupported NBT type for component array conversion: " + nbt.getId() );
        }
    }

    private static String nbtToString(TypedTag nbt)
    {
        return nbtToString( nbt, false );
    }

    private static String nbtToString(TypedTag nbt, boolean nullAsEmptyString)
    {
        if ( nbt == null )
        {
            return nullAsEmptyString ? "" : null;
        }
        switch ( nbt.getId() )
        {
            case Tag.BYTE:
                return String.valueOf( ( (ByteTag) nbt ).getValue() );
            case Tag.SHORT:
                return String.valueOf( ( (ShortTag) nbt ).getValue() );
            case Tag.INT:
                return String.valueOf( ( (IntTag) nbt ).getValue() );
            case Tag.LONG:
                return String.valueOf( ( (LongTag) nbt ).getValue() );
            case Tag.FLOAT:
                return String.valueOf( ( (FloatTag) nbt ).getValue() );
            case Tag.DOUBLE:
                return String.valueOf( ( (DoubleTag) nbt ).getValue() );
            case Tag.STRING:
                return ( (StringTag) nbt ).getValue();
            default:
                throw new IllegalArgumentException( "Unsupported NBT type for string conversion: " + nbt.getId() );
        }
    }

    private static int nbtToInt(TypedTag nbt)
    {
        return nbtToInt( nbt, false );
    }

    private static int nbtToInt(TypedTag nbt, boolean handleNbtNumberStringSuffixes)
    {
        if ( nbt == null )
        {
            return 0;
        }
        switch ( nbt.getId() )
        {
            case Tag.BYTE:
                return ( (ByteTag) nbt ).getValue();
            case Tag.SHORT:
                return ( (ShortTag) nbt ).getValue();
            case Tag.INT:
                return ( (IntTag) nbt ).getValue();
            case Tag.LONG:
                return (int) ( (LongTag) nbt ).getValue();
            case Tag.FLOAT:
                return (int) ( (FloatTag) nbt ).getValue();
            case Tag.DOUBLE:
                return (int) ( (DoubleTag) nbt ).getValue();
            case Tag.STRING:
                String strValue = ( (StringTag) nbt ).getValue();
                if ( handleNbtNumberStringSuffixes )
                {
                    char last = strValue.charAt( strValue.length() - 1 );
                    // Check for all number suffixes
                    if ( last == 'b' || last == 's' || last == 'l' || last == 'f' || last == 'd' )
                    {
                        strValue = strValue.substring( 0, strValue.length() - 1 );
                    }
                }
                try
                {
                    return Integer.parseInt( strValue );
                } catch ( NumberFormatException e )
                {
                    throw new IllegalArgumentException( e );
                }
            default:
                throw new IllegalArgumentException( "Unsupported NBT type for int conversion: " + nbt.getId() );
        }
    }

    private static BaseComponent componentFromCompound(CompoundTag nbt)
    {
        BaseComponent component = specificComponentFromCompound( nbt );
        component.setStyle( toStyle( nbt ) );

        Map<String, TypedTag> map = nbt.getValue();

        TypedTag insertion = map.get( "insertion" );
        if ( insertion != null )
        {
            component.setInsertion( nbtToString( insertion ) );
        }
        TypedTag clickEvent = map.get( "click_event" );
        boolean newClickEvent = clickEvent != null;
        if ( newClickEvent && clickEvent.getId() == Tag.COMPOUND )
        {
            CompoundTag clickEventCompound = (CompoundTag) clickEvent;
            ClickEventSerializer.ClickType clickType = newClickEvent ? ClickEventSerializer.ClickType.NEW : ClickEventSerializer.ClickType.OLD;
            component.setClickEvent( nbtToClickEvent( clickEventCompound, clickType ) );
        }
        TypedTag hoverEvent = map.get( "hover_event" );
        boolean newHoverEvent = hoverEvent != null;
        if ( !newHoverEvent )
        {
            hoverEvent = map.get( "hoverEvent" );
        }
        if ( hoverEvent != null && hoverEvent.getId() == Tag.COMPOUND )
        {
            CompoundTag hoverEventCompound = (CompoundTag) hoverEvent;
            component.setHoverEvent( nbtToHoverEvent( hoverEventCompound, newHoverEvent ) );
        }
        TypedTag extra = map.get( "extra" );
        if ( extra != null && extra.getId() == Tag.LIST )
        {
            ListTag list = (ListTag) extra;
            component.setExtra( list.getValue().stream().map( NbtComponentConverter::toComponent ).collect( Collectors.toList() ) );
        }
        return component;
    }

    private static HoverEvent nbtToHoverEvent(CompoundTag nbt, boolean newFormat)
    {
        Map<String, TypedTag> map = nbt.getValue();
        HoverEvent.Action action = HoverEvent.Action.valueOf( nbtToString( map.get( "action" ) ).toUpperCase( Locale.ROOT ) );

        if ( newFormat || map.containsKey( "contents" ) )
        {
            // value is only used for text in >= 1.21.5 (its inlined now)
            TypedTag contents = map.get( newFormat ? "value" : "contents" );
            if ( contents != null || ( newFormat && ( action == HoverEvent.Action.SHOW_ITEM || action == HoverEvent.Action.SHOW_ENTITY ) ) )
            {
                if ( contents == null )
                {
                    // this is the new inline for SHOW_ITEM and SHOW_ENTITY
                    contents = nbt;
                }
                List<Content> list;
                if ( contents.getId() == Tag.LIST )
                {
                    ListTag listTag = (ListTag) contents;
                    list = listTag.getValue().stream()
                            .map( elem -> nbtToHoverContent( elem, action ) )
                            .collect( Collectors.toList() );
                } else
                {
                    list = new ArrayList<>( 1 );
                    list.add( nbtToHoverContent( contents, action ) );
                }
                return new HoverEvent( action, list );
            }
        } else
        {
            TypedTag value = map.get( "value" );
            if ( value != null )
            {
                // Plugins previously had support to pass BaseComponent[] into any action.
                // If it is possible to be parsed as BaseComponent, attempt to parse as so.
                if ( value.getId() == Tag.LIST )
                {
                    BaseComponent[] components = NbtComponentConverter.toComponentArray( value );
                    if ( components != null )
                    {
                        return new HoverEvent( action, components );
                    }
                } else
                {
                    BaseComponent component = NbtComponentConverter.toComponent( value );
                    if ( component != null )
                    {
                        return new HoverEvent( action, new BaseComponent[]{component} );
                    }
                }
            } else
            {
                // throw new IllegalArgumentException( "Missing value for hover event" );
            }
        }
        return null;
    }

    private static ClickEvent nbtToClickEvent(CompoundTag nbt, ClickEventSerializer.ClickType type)
    {
        Map<String, TypedTag> map = nbt.getValue();
        String actionKey = ( type == ClickEventSerializer.ClickType.DIALOG ) ? "type" : "action";
        ClickEvent.Action action = ClickEvent.Action.valueOf( nbtToString( map.get( actionKey ) ).toUpperCase( Locale.ROOT ) );
        if ( type == ClickEventSerializer.ClickType.OLD )
        {
            return new ClickEvent( action, nbtToString( map.get( "value" ), true ) );
        } else if ( type == ClickEventSerializer.ClickType.NEW || type == ClickEventSerializer.ClickType.DIALOG )
        {
            switch ( action )
            {
                case OPEN_URL:
                    return new ClickEvent( action, nbtToString( map.get( "url" ) ) );
                case RUN_COMMAND:
                case SUGGEST_COMMAND:
                    return new ClickEvent( action, nbtToString( map.get( "command" ) ) );
                case CHANGE_PAGE:
                    int page = Integer.parseInt( nbtToString( map.get( "page" ) ) );
                    Preconditions.checkArgument( page >= 0, "Page number has to be positive" );
                    return new ClickEvent( action, Integer.toString( page ) );
                case SHOW_DIALOG:
                    //TODO handle show dialog click event
                    return null;
                case CUSTOM:
                    return new ClickEventCustom( nbtToString( map.get( "id" ) ), nbtToString( map.get( "payload" ) ) );
                default:
                    return new ClickEvent( action, nbtToString( map.get( "value" ), true ) );
            }
        } else
        {
            throw new IllegalArgumentException( "Unknown serializer type" );
        }
    }

    private static BaseComponent specificComponentFromCompound(CompoundTag nbt)
    {
        Map<String, TypedTag> map = nbt.getValue();

        TypedTag translateTag = map.get( "translate" );
        if ( translateTag != null )
        {
            TranslatableComponent component = new TranslatableComponent( nbtToString( translateTag ) );
            TypedTag with = map.get( "with" );
            if ( with != null && with.getId() == Tag.LIST )
            {
                ListTag list = (ListTag) with;
                component.setWith( list.getValue().stream().map( NbtComponentConverter::toComponent ).collect( Collectors.toList() ) );
            }
            TypedTag fallback = map.get( "fallback" );
            if ( fallback != null && fallback.getId() == Tag.STRING )
            {
                component.setFallback( nbtToString( fallback ) );
            }
            return component;
        }
        TypedTag keybindTag = map.get( "keybind" );
        if ( keybindTag != null )
        {

            return new KeybindComponent( nbtToString( keybindTag ) );
        }
        TypedTag scoreTag = map.get( "score" );
        if ( scoreTag != null )
        {
            Map<String, TypedTag> scoreMap = ( (CompoundTag) scoreTag ).getValue();
            return new ScoreComponent(
                    nbtToString( scoreMap.get( "name" ) ),
                    nbtToString( scoreMap.get( "objective" ) ),
                    nbtToString( scoreMap.get( "value" ) ) );
        }
        TypedTag selectorTag = map.get( "selector" );
        if ( selectorTag != null )
        {
            SelectorComponent component = new SelectorComponent( nbtToString( selectorTag ) );
            TypedTag separator = map.get( "separator" );
            if ( separator != null )
            {
                component.setSeparator( toComponent( separator ) );
            }
            return component;
        }
        TextComponent component = new TextComponent();
        TypedTag tag = map.get( "text" );
        if ( tag != null )
        {
            component.setText( nbtToString( tag ) );
        }
        return component;
    }

    private static Content nbtToHoverContent(TypedTag nbt, HoverEvent.Action action)
    {
        switch ( action )
        {
            case SHOW_TEXT:
                return nbtToTextContent( nbt );
            case SHOW_ITEM:
                return nbtToItemContent( (CompoundTag) nbt );
            case SHOW_ENTITY:
                return nbtToEntityContent( (CompoundTag) nbt );
            default:
                throw new UnsupportedOperationException( "Action '" + action.name() + " not supported" );
        }
    }

    private static Text nbtToTextContent(TypedTag nbt)
    {
        if ( nbt == null )
        {
            return null;
        }
        switch ( nbt.getId() )
        {
            case Tag.BYTE:
            case Tag.SHORT:
            case Tag.INT:
            case Tag.LONG:
            case Tag.FLOAT:
            case Tag.DOUBLE:
            case Tag.STRING:
                return new Text( nbtToString( nbt ) );
            case Tag.LIST:
                ListTag list = (ListTag) nbt;
                BaseComponent[] components = list.getValue().stream().map( NbtComponentConverter::toComponent ).toArray( BaseComponent[]::new );
                return new Text( components );
            case Tag.COMPOUND:
                return new Text( toComponent( nbt ) );
            default:
                throw new IllegalArgumentException( "Unsupported NBT type for Text hover content: " + nbt.getId() );
        }
    }

    private static Entity nbtToEntityContent(CompoundTag nbt)
    {
        Map<String, TypedTag> map = nbt.getValue();

        TypedTag uuidTag_ = map.get( "uuid" );
        boolean newFormat = uuidTag_ != null;

        String idString;
        TypedTag uuidTag = newFormat ? uuidTag_ : map.get( "id" );
        if ( uuidTag == null )
        {
            throw new IllegalArgumentException( "Missing entity id/uuid in NBT" );
        }
        if ( uuidTag.getId() == Tag.INT_ARRAY )
        {
            int[] uuidArray = ( (IntArrayTag) uuidTag ).getValue();
            idString = new UUID( ( (long) uuidArray[ 0 ] << 32 ) | ( (long) uuidArray[ 1 ] & 0xFFFFFFFFL ), ( (long) uuidArray[ 2 ] << 32 ) | ( (long) uuidArray[ 3 ] & 0xFFFFFFFFL ) ).toString();
        } else
        {
            idString = nbtToString( uuidTag );
        }

        String type = null;
        TypedTag typeTag = map.get( newFormat ? "id" : "type" );
        if ( typeTag != null )
        {
            type = nbtToString( typeTag );
        }

        BaseComponent name = null;
        TypedTag nameTag = map.get( "name" );
        if ( nameTag != null )
        {
            name = toComponent( nameTag );
        }

        return new Entity( type, idString, name );
    }

    private static Item nbtToItemContent(CompoundTag nbt)
    {
        Map<String, TypedTag> map = nbt.getValue();

        // Read count (optional, default -1)
        int count = -1;
        TypedTag countTag = map.get( "Count" );
        if ( countTag != null )
        {
            count = nbtToInt( countTag, true );
        }

        // Read id (namespaced item id, may be null)
        String id = null;
        TypedTag idTag = map.get( "id" );
        if ( idTag != null )
        {
            id = nbtToString( idTag );
        }

        // Read tag (optional, ItemTag)
        ItemTag itemTag = null;
        TypedTag tagTag = map.get( "tag" );
        if ( tagTag != null && tagTag.getId() == Tag.STRING )
        {
            itemTag = ItemTag.ofNbt( ( (StringTag) tagTag ).getValue() );
        }

        return new Item( id, count, itemTag );
    }

    private static Boolean toBoolean(TypedTag nbt)
    {
        if ( nbt == null )
        {
            return null;
        }
        switch ( nbt.getId() )
        {
            case Tag.BYTE:
                return ( (ByteTag) nbt ).getValue() != 0;
            case Tag.SHORT:
                return ( (ShortTag) nbt ).getValue() != 0;
            case Tag.INT:
                return ( (IntTag) nbt ).getValue() != 0;
            case Tag.LONG:
                return ( (LongTag) nbt ).getValue() != 0;
            case Tag.FLOAT:
                return ( (FloatTag) nbt ).getValue() != 0.0f;
            case Tag.DOUBLE:
                return ( (DoubleTag) nbt ).getValue() != 0.0;
            case Tag.STRING:
                String value = ( (StringTag) nbt ).getValue();
                return "1".equals( value ) || "true".equalsIgnoreCase( value );
            default:
                return false;
        }
    }

    @SneakyThrows(NumberFormatException.class)
    private static float toFloat(TypedTag nbt)
    {
        if ( nbt == null )
        {
            return 0.0f;
        }
        switch ( nbt.getId() )
        {
            case Tag.BYTE:
                return ( (ByteTag) nbt ).getValue();
            case Tag.SHORT:
                return ( (ShortTag) nbt ).getValue();
            case Tag.INT:
                return ( (IntTag) nbt ).getValue();
            case Tag.LONG:
                return (float) ( (LongTag) nbt ).getValue();
            case Tag.FLOAT:
                return ( (FloatTag) nbt ).getValue();
            case Tag.DOUBLE:
                return (float) ( (DoubleTag) nbt ).getValue();
            case Tag.STRING:
                String strValue = ( (StringTag) nbt ).getValue();
                char last = strValue.charAt( strValue.length() - 1 );
                // Check for all number suffixes
                if ( last == 'b' || last == 's' || last == 'l' || last == 'f' || last == 'd' )
                {
                    strValue = strValue.substring( 0, strValue.length() - 1 );
                }
                return Float.parseFloat( strValue );
            default:
                throw new IllegalArgumentException( "Unsupported NBT type for float conversion: " + nbt.getId() );
        }
    }

    public static ComponentStyle toStyle(TypedTag nbt)
    {
        if ( nbt == null || nbt.getId() != Tag.COMPOUND )
        {
            return null;
        }
        CompoundTag compound = (CompoundTag) nbt;
        Map<String, TypedTag> map = compound.getValue();
        ComponentStyle style = new ComponentStyle();
        style.setBold( toBoolean( map.get( "bold" ) ) );
        style.setItalic( toBoolean( map.get( "italic" ) ) );
        style.setUnderlined( toBoolean( map.get( "underlined" ) ) );
        style.setStrikethrough( toBoolean( map.get( "strikethrough" ) ) );
        style.setObfuscated( toBoolean( map.get( "obfuscated" ) ) );
        TypedTag colorTag = map.get( "color" );
        if ( colorTag != null && colorTag.getId() == Tag.STRING )
        {
            style.setColor( ChatColor.of( ( (StringTag) colorTag ).getValue() ) );
        }
        TypedTag shadowColorTag = map.get( "shadow_color" );
        if ( shadowColorTag != null )
        {
            switch ( shadowColorTag.getId() )
            {
                case Tag.BYTE:
                case Tag.SHORT:
                case Tag.INT:
                case Tag.LONG:
                case Tag.FLOAT:
                case Tag.DOUBLE:
                case Tag.STRING:
                    style.setShadowColor( new Color( nbtToInt( shadowColorTag ), true ) );
                    break;
                case Tag.LIST:
                    ListTag list = (ListTag) shadowColorTag;
                    style.setShadowColor( new Color(
                            toFloat( list.get( 0 ) ),
                            toFloat( list.get( 1 ) ),
                            toFloat( list.get( 2 ) ),
                            toFloat( list.get( 3 ) )
                    ) );
                    break;
            }
        }
        TypedTag fontTag = map.get( "font" );
        if ( fontTag != null && fontTag.getId() == Tag.STRING )
        {
            style.setFont( ( (StringTag) fontTag ).getValue() );
        }
        return style;
    }
}
