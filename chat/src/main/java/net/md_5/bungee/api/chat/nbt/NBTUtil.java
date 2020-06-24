package net.md_5.bungee.api.chat.nbt;

import java.util.regex.Pattern;
import se.llbit.nbt.ByteArrayTag;
import se.llbit.nbt.ByteTag;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.DoubleTag;
import se.llbit.nbt.FloatTag;
import se.llbit.nbt.IntArrayTag;
import se.llbit.nbt.IntTag;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.LongArrayTag;
import se.llbit.nbt.LongTag;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.ShortTag;
import se.llbit.nbt.StringTag;
import se.llbit.nbt.Tag;

public class NBTUtil
{

    /**
     * Pattern to test whether String needs escaping
     */
    private static final Pattern PATTERN = Pattern.compile( "[A-Za-z0-9._+-]+" );

    public static CompoundTag fromString(String string)
    {
        // TODO:
        return null;
    }

    public static String toString(Tag tag)
    {
        if ( tag instanceof NamedTag )
        {
            return toString( ( (NamedTag) tag ).getTag() );
        } else if ( tag instanceof CompoundTag )
        {
            StringBuilder builder = new StringBuilder();
            builder.append( '{' );

            for ( NamedTag next : ( CompoundTag ) tag )
            {
                if ( builder.length() != 1 )
                {
                    builder.append( ',' );
                }

                if ( PATTERN.matcher( next.name ).matches() )
                {
                    builder.append( next.name );
                } else
                {
                    // escape the name
                    StringBuilder builder1 = new StringBuilder( "\"" );

                    for ( int i = 0; i < next.name.length(); ++i )
                    {
                        char ch = next.name.charAt( i );
                        if ( ch == '\\' || ch == '"' )
                        {
                            builder1.append( '\\' );
                        }

                        builder1.append( ch );
                    }

                    builder1.append( '"' );
                    builder.append( builder1 );
                }

                builder.append( ':' ).append( toString( next ) );
            }

            return builder.append( '}' ).toString();
        } else if ( tag instanceof IntTag )
        {
            return Integer.toString( ( (IntTag) tag ).value );
        } else if ( tag instanceof ByteTag )
        {
            return ( (ByteTag) tag ).value + "b";
        } else if ( tag instanceof DoubleTag )
        {
            return ( (DoubleTag) tag ).value + "d";
        } else if ( tag instanceof FloatTag )
        {
            return ( (FloatTag) tag ).value + "f";
        } else if ( tag instanceof LongTag )
        {
            return ( (LongTag) tag ).value + "L";
        } else if ( tag instanceof ShortTag )
        {
            return ( (ShortTag) tag ).value + "s";
        } else if ( tag instanceof StringTag )
        {
            StringTag stringTag = (StringTag) tag;

            StringBuilder builder = new StringBuilder( "\"" );

            for ( int i = 0; i < stringTag.value.length(); ++i )
            {
                char ch = stringTag.value.charAt( i );
                if ( ch == '\\' || ch == '"' )
                {
                    builder.append( '\\' );
                }
                builder.append( ch );
            }
            return builder.append( '"' ).toString();
        } else if ( tag instanceof ListTag )
        {
            ListTag list = (ListTag) tag;

            StringBuilder builder = new StringBuilder();
            builder.append( '[' );

            for ( int i = 0; i < list.items.size(); ++i )
            {
                if ( i != 0 )
                {
                    builder.append( ',' );
                }

                builder.append( toString( list.items.get( i ) ) );
            }

            return builder.append( ']' ).toString();
        } else if ( tag instanceof ByteArrayTag )
        {
            ByteArrayTag arrayTag = (ByteArrayTag) tag;

            StringBuilder builder = new StringBuilder( "[B;" );
            for ( int i = 0; i < arrayTag.value.length; ++i )
            {
                if ( i != 0 )
                {
                    builder.append( ',' );
                }
                builder.append( arrayTag.value[ i ] ).append( 'B' );
            }

            return builder.append( ']' ).toString();
        } else if ( tag instanceof IntArrayTag )
        {
            IntArrayTag arrayTag = (IntArrayTag) tag;

            StringBuilder builder = new StringBuilder( "[I;" );
            for ( int i = 0; i < arrayTag.value.length; ++i )
            {
                if ( i != 0 )
                {
                    builder.append( ',' );
                }
                builder.append( arrayTag.value[ i ] );
            }

            return builder.append( ']' ).toString();
        } else if ( tag instanceof LongArrayTag )
        {
            LongArrayTag arrayTag = (LongArrayTag) tag;

            StringBuilder builder = new StringBuilder( "[L;" );
            for ( int i = 0; i < arrayTag.value.length; ++i )
            {
                if ( i != 0 )
                {
                    builder.append( ',' );
                }
                builder.append( arrayTag.value[ i ] ).append( 'L' );
            }

            return builder.append( ']' ).toString();
        } // TODO: handle item properly

        throw new UnsupportedOperationException( "Unimplemented tag type " + tag.tagName() + " (" + tag.tagName() + "|" + tag.getClass().getSimpleName() + ")" );
    }
}
