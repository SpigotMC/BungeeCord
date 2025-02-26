package net.md_5.bungee.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.StringTag;
import se.llbit.nbt.Tag;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagUtil
{

    public static SpecificTag fromJson(JsonElement json)
    {
        if ( json instanceof JsonPrimitive )
        {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            if ( jsonPrimitive.isNumber() )
            {
                Number number = json.getAsNumber();

                if ( number instanceof Byte )
                {
                    return new ByteTag( (Byte) number );
                } else if ( number instanceof Short )
                {
                    return new ShortTag( (Short) number );
                } else if ( number instanceof Integer )
                {
                    return new IntTag( (Integer) number );
                } else if ( number instanceof Long )
                {
                    return new LongTag( (Long) number );
                } else if ( number instanceof Float )
                {
                    return new FloatTag( (Float) number );
                } else if ( number instanceof Double )
                {
                    return new DoubleTag( (Double) number );
                }
            } else if ( jsonPrimitive.isString() )
            {
                return new StringTag( jsonPrimitive.getAsString() );
            } else if ( jsonPrimitive.isBoolean() )
            {
                return new ByteTag( jsonPrimitive.getAsBoolean() ? 1 : 0 );
            } else
            {
                throw new IllegalArgumentException( "Unknown JSON primitive: " + jsonPrimitive );
            }
        } else if ( json instanceof JsonObject )
        {
            CompoundTag compoundTag = new CompoundTag();
            for ( Map.Entry<String, JsonElement> property : ( (JsonObject) json ).entrySet() )
            {
                compoundTag.add( property.getKey(), fromJson( property.getValue() ) );
            }

            return compoundTag;
        } else if ( json instanceof JsonArray )
        {
            List<JsonElement> jsonArray = ( (JsonArray) json ).asList();

            Integer listType = null;

            for ( JsonElement jsonEl : jsonArray )
            {
                int type = fromJson( jsonEl ).tagType();
                if ( listType == null )
                {
                    listType = type;
                } else if ( listType != type )
                {
                    listType = Tag.TAG_COMPOUND;
                    break;
                }
            }

            if ( listType == null )
            {
                return new ListTag( Tag.TAG_END, Collections.emptyList() );
            }

            SpecificTag listTag;
            switch ( listType )
            {
                case Tag.TAG_BYTE:
                    byte[] bytes = new byte[ jsonArray.size() ];
                    for ( int i = 0; i < bytes.length; i++ )
                    {
                        bytes[i] = (Byte) ( (JsonPrimitive) jsonArray.get( i ) ).getAsNumber();
                    }

                    listTag = new ByteArrayTag( bytes );
                    break;
                case Tag.TAG_INT:
                    int[] ints = new int[ jsonArray.size() ];
                    for ( int i = 0; i < ints.length; i++ )
                    {
                        ints[i] = (Integer) ( (JsonPrimitive) jsonArray.get( i ) ).getAsNumber();
                    }

                    listTag = new IntArrayTag( ints );
                    break;
                case Tag.TAG_LONG:
                    long[] longs = new long[ jsonArray.size() ];
                    for ( int i = 0; i < longs.length; i++ )
                    {
                        longs[i] = (Long) ( (JsonPrimitive) jsonArray.get( i ) ).getAsNumber();
                    }

                    listTag = new LongArrayTag( longs );
                    break;
                default:
                    List<SpecificTag> tagItems = new ArrayList<>( jsonArray.size() );

                    for ( JsonElement jsonEl : jsonArray )
                    {
                        SpecificTag subTag = fromJson( jsonEl );
                        if ( listType == Tag.TAG_COMPOUND && !( subTag instanceof CompoundTag ) )
                        {
                            CompoundTag wrapper = new CompoundTag();
                            wrapper.add( "", subTag );
                            subTag = wrapper;
                        }

                        tagItems.add( subTag );
                    }

                    listTag = new ListTag( listType, tagItems );
                    break;
            }

            return listTag;
        } else if ( json instanceof JsonNull )
        {
            return Tag.END;
        }

        throw new IllegalArgumentException( "Unknown JSON element: " + json );
    }

    public static JsonElement toJson(SpecificTag tag)
    {
        switch ( tag.tagType() )
        {
            case Tag.TAG_BYTE:
                return new JsonPrimitive( (byte) ( (ByteTag) tag ).getData() );
            case Tag.TAG_SHORT:
                return new JsonPrimitive( ( (ShortTag) tag ).getData() );
            case Tag.TAG_INT:
                return new JsonPrimitive( ( (IntTag) tag ).getData() );
            case Tag.TAG_LONG:
                return new JsonPrimitive( ( (LongTag) tag ).getData() );
            case Tag.TAG_FLOAT:
                return new JsonPrimitive( ( (FloatTag) tag ).getData() );
            case Tag.TAG_DOUBLE:
                return new JsonPrimitive( ( (DoubleTag) tag ).getData() );
            case Tag.TAG_BYTE_ARRAY:
                byte[] byteArray = ( (ByteArrayTag) tag ).getData();

                JsonArray jsonByteArray = new JsonArray( byteArray.length );
                for ( byte b : byteArray )
                {
                    jsonByteArray.add( new JsonPrimitive( b ) );
                }

                return jsonByteArray;
            case Tag.TAG_STRING:
                return new JsonPrimitive( ( (StringTag) tag ).getData() );
            case Tag.TAG_LIST:
                List<SpecificTag> items = ( (ListTag) tag ).items;

                JsonArray jsonList = new JsonArray( items.size() );
                for ( SpecificTag subTag : items )
                {
                    if ( subTag instanceof CompoundTag )
                    {
                        CompoundTag compound = (CompoundTag) subTag;
                        if ( compound.size() == 1 )
                        {
                            SpecificTag first = (SpecificTag) compound.get( "" );
                            if ( !first.isError() )
                            {
                                jsonList.add( toJson( first ) );
                                continue;
                            }
                        }
                    }

                    jsonList.add( toJson( subTag ) );
                }

                return jsonList;
            case Tag.TAG_COMPOUND:
                JsonObject jsonObject = new JsonObject();
                for ( NamedTag subTag : (CompoundTag) tag )
                {
                    jsonObject.add( subTag.name(), toJson( subTag.getTag() ) );
                }

                return jsonObject;
            case Tag.TAG_INT_ARRAY:
                int[] intArray = ( (IntArrayTag) tag ).getData();

                JsonArray jsonIntArray = new JsonArray( intArray.length );
                for ( int i : intArray )
                {
                    jsonIntArray.add( new JsonPrimitive( i ) );
                }

                return jsonIntArray;
            case Tag.TAG_LONG_ARRAY:
                long[] longArray = ( (LongArrayTag) tag ).getData();

                JsonArray jsonLongArray = new JsonArray( longArray.length );
                for ( long l : longArray )
                {
                    jsonLongArray.add( new JsonPrimitive( l ) );
                }

                return jsonLongArray;
            default:
                throw new IllegalArgumentException( "Unknown NBT tag: " + tag );
        }
    }
}
