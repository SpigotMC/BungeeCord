package net.md_5.bungee.api.chat.nbt;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Pattern;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.IntTag;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.StringTag;

public class NBTUtil
{

    public static CompoundTag nbtFromString(String string)
    {
        // TODO Deserialisation
        return null;
    }

    public static String nbtAsString(CompoundTag tag)
    {
        return nbtAsString( tag, new StringBuilder() );
    }

    public static String nbtAsString(CompoundTag tag, StringBuilder builder)
    {
        builder.append( '{' );

        // NBT lib has no tag#getItems method :(
        List<NamedTag> list = Lists.newArrayList( tag.iterator() );

        for ( NamedTag next : list )
        {
            if ( builder.length() != 1 )
            {
                builder.append( ',' );
            }

            builder.append( handleEscape( next.name ) ).append( ':' ).append( NBTUtil.toString( next.tag ) );
        }

        return builder.append( '}' ).toString();
    }

    private static final Pattern PATTERN = Pattern.compile( "[A-Za-z0-9._+-]+" );

    protected static String handleEscape(String string)
    {
        return PATTERN.matcher( string ).matches() ? string : escape( string );
    }

    private static String escape(String string)
    {
        StringBuilder stringbuilder = new StringBuilder( "\"" );

        for ( int i = 0; i < string.length(); ++i )
        {
            char ch = string.charAt( i );
            if ( ch == '\\' || ch == '"' )
            {
                stringbuilder.append( '\\' );
            }

            stringbuilder.append( ch );
        }

        return stringbuilder.append( '"' ).toString();
    }

    private static String toString(SpecificTag tag)
    {
        if ( tag.isCompoundTag() )
        {
            return nbtAsString( (CompoundTag) tag );
        } else if ( tag instanceof StringTag )
        {
            return tag.extraInfo().substring( 2 ); // remove ': '
            //return ( (StringTag) tag ).value;
        } else if ( tag instanceof IntTag )
        {
            return tag.extraInfo().substring( 2 ); // remove ': '
            //return ( (IntTag) tag ).value;
        } else if ( tag instanceof ListTag )
        {
            StringBuilder builder = new StringBuilder();
            ListTag list = (ListTag) tag;
            for ( SpecificTag tag1 : list.items )
            {
                if ( builder.length() != 0 )
                {
                    builder.append( ',' );
                }
                builder.append( toString( tag1 ) );
            };
            return builder.toString();
        } else
        {
            throw new UnsupportedOperationException( "Unimplemented tag type " + tag.tagName() + " (" + tag.tagType() + ")" );
        }
    }
}
