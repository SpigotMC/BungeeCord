package net.md_5.bungee.protocol.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.md_5.bungee.nbt.Tag;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.nbt.type.ByteArrayTag;
import net.md_5.bungee.nbt.type.ByteTag;
import net.md_5.bungee.nbt.type.CompoundTag;
import net.md_5.bungee.nbt.type.DoubleTag;
import net.md_5.bungee.nbt.type.EndTag;
import net.md_5.bungee.nbt.type.FloatTag;
import net.md_5.bungee.nbt.type.IntArrayTag;
import net.md_5.bungee.nbt.type.IntTag;
import net.md_5.bungee.nbt.type.ListTag;
import net.md_5.bungee.nbt.type.LongArrayTag;
import net.md_5.bungee.nbt.type.LongTag;
import net.md_5.bungee.nbt.type.ShortTag;
import net.md_5.bungee.nbt.type.StringTag;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagUtil
{

    private static byte nbtTypeOfJson(JsonElement json)
    {
        if ( json instanceof JsonPrimitive )
        {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
            if ( jsonPrimitive.isNumber() )
            {
                Number number = json.getAsNumber();

                if ( number instanceof Byte )
                {
                    return Tag.BYTE;
                } else if ( number instanceof Short )
                {
                    return Tag.SHORT;
                } else if ( number instanceof Integer )
                {
                    return Tag.INT;
                } else if ( number instanceof Long )
                {
                    return Tag.LONG;
                } else if ( number instanceof Float )
                {
                    return Tag.FLOAT;
                } else if ( number instanceof Double )
                {
                    return Tag.DOUBLE;
                }
            } else if ( jsonPrimitive.isString() )
            {
                return Tag.STRING;
            } else if ( jsonPrimitive.isBoolean() )
            {
                return Tag.BYTE;
            }
            throw new IllegalArgumentException( "Unknown JSON primitive: " + jsonPrimitive );
        } else if ( json instanceof JsonObject )
        {
            return Tag.COMPOUND;
        } else if ( json instanceof JsonArray )
        {
            JsonArray array = json.getAsJsonArray();

            Byte listType = null;
            for ( JsonElement jsonEl : array )
            {
                byte type = nbtTypeOfJson( jsonEl );
                if ( listType == null )
                {
                    listType = type;
                } else if ( listType != type )
                {
                    listType = Tag.COMPOUND;
                    break;
                }
            }

            if ( listType == null )
            {
                return Tag.LIST;
            }

            switch ( listType )
            {
                case Tag.BYTE:
                    return Tag.BYTE_ARRAY;
                case Tag.INT:
                    return Tag.INT_ARRAY;
                case Tag.LONG:
                    return Tag.LONG_ARRAY;
                default:
                    return Tag.LIST;
            }
        } else if ( json instanceof JsonNull )
        {
            return Tag.END;
        }

        throw new IllegalArgumentException( "Unknown JSON element: " + json );
    }

    public static TypedTag fromJson(JsonElement json)
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
                return new ByteTag( (byte) ( jsonPrimitive.getAsBoolean() ? 1 : 0 ) );
            }
            throw new IllegalArgumentException( "Unknown JSON primitive: " + jsonPrimitive );
        } else if ( json instanceof JsonObject )
        {
            CompoundTag compoundTag = new CompoundTag( new LinkedHashMap<>() );
            for ( Map.Entry<String, JsonElement> property : ( (JsonObject) json ).entrySet() )
            {
                compoundTag.getValue().put( property.getKey(), fromJson( property.getValue() ) );
            }

            return compoundTag;
        } else if ( json instanceof JsonArray )
        {
            List<JsonElement> jsonArray = ( (JsonArray) json ).asList();

            Byte listType = null;

            for ( JsonElement jsonEl : jsonArray )
            {
                byte type = nbtTypeOfJson( jsonEl );
                if ( listType == null )
                {
                    listType = type;
                } else if ( listType != type )
                {
                    listType = Tag.COMPOUND;
                    break;
                }
            }

            if ( listType == null || listType == Tag.END )
            {
                if ( !jsonArray.isEmpty() )
                {
                    throw new IllegalArgumentException( "Invalid end tag in json array: " + json );
                }

                return new ListTag( Collections.emptyList(), Tag.END );
            }

            TypedTag listTag;
            switch ( listType )
            {
                case Tag.BYTE:
                    byte[] bytes = new byte[ jsonArray.size() ];
                    for ( int i = 0; i < bytes.length; i++ )
                    {
                        bytes[i] = (Byte) ( jsonArray.get( i ) ).getAsNumber();
                    }

                    listTag = new ByteArrayTag( bytes );
                    break;
                case Tag.INT:
                    int[] ints = new int[ jsonArray.size() ];
                    for ( int i = 0; i < ints.length; i++ )
                    {
                        ints[i] = (Integer) ( jsonArray.get( i ) ).getAsNumber();
                    }

                    listTag = new IntArrayTag( ints );
                    break;
                case Tag.LONG:
                    long[] longs = new long[ jsonArray.size() ];
                    for ( int i = 0; i < longs.length; i++ )
                    {
                        longs[i] = (Long) ( jsonArray.get( i ) ).getAsNumber();
                    }

                    listTag = new LongArrayTag( longs );
                    break;
                default:
                    List<TypedTag> tagItems = new ArrayList<>( jsonArray.size() );

                    for ( JsonElement jsonEl : jsonArray )
                    {
                        TypedTag subTag = fromJson( jsonEl );
                        if ( listType == Tag.COMPOUND && !( subTag instanceof CompoundTag ) )
                        {
                            CompoundTag wrapper = new CompoundTag( new LinkedHashMap<>() );
                            wrapper.getValue().put( "", subTag );
                            subTag = wrapper;
                        }

                        tagItems.add( subTag );
                    }

                    listTag = new ListTag( tagItems, listType );
                    break;
            }

            return listTag;
        } else if ( json instanceof JsonNull )
        {
            return EndTag.INSTANCE;
        }

        throw new IllegalArgumentException( "Unknown JSON element: " + json );
    }

    public static JsonElement toJson(TypedTag tag)
    {
        switch ( tag.getId() )
        {
            case Tag.BYTE:
                return new JsonPrimitive( ( (ByteTag) tag ).getValue() );
            case Tag.SHORT:
                return new JsonPrimitive( ( (ShortTag) tag ).getValue() );
            case Tag.INT:
                return new JsonPrimitive( ( (IntTag) tag ).getValue() );
            case Tag.LONG:
                return new JsonPrimitive( ( (LongTag) tag ).getValue() );
            case Tag.FLOAT:
                return new JsonPrimitive( ( (FloatTag) tag ).getValue() );
            case Tag.DOUBLE:
                return new JsonPrimitive( ( (DoubleTag) tag ).getValue() );
            case Tag.BYTE_ARRAY:
                byte[] byteArray = ( (ByteArrayTag) tag ).getValue();

                JsonArray jsonByteArray = new JsonArray( byteArray.length );
                for ( byte b : byteArray )
                {
                    jsonByteArray.add( new JsonPrimitive( b ) );
                }

                return jsonByteArray;
            case Tag.STRING:
                return new JsonPrimitive( ( (StringTag) tag ).getValue() );
            case Tag.LIST:
                List<TypedTag> items = ( (ListTag) tag ).getValue();

                JsonArray jsonList = new JsonArray( items.size() );
                for ( TypedTag subTag : items )
                {
                    if ( subTag instanceof CompoundTag )
                    {
                        CompoundTag compound = (CompoundTag) subTag;
                        if ( compound.getValue().size() == 1 )
                        {
                            TypedTag first = compound.getValue().get( "" );
                            if ( first != null )
                            {
                                jsonList.add( toJson( first ) );
                                continue;
                            }
                        }
                    }

                    jsonList.add( toJson( subTag ) );
                }

                return jsonList;
            case Tag.COMPOUND:
                JsonObject jsonObject = new JsonObject();
                CompoundTag compoundTag = (CompoundTag) tag;
                compoundTag.getValue().forEach( (key, value) -> jsonObject.add( key, toJson( value ) ) );
                return jsonObject;
            case Tag.INT_ARRAY:
                int[] intArray = ( (IntArrayTag) tag ).getValue();

                JsonArray jsonIntArray = new JsonArray( intArray.length );
                for ( int i : intArray )
                {
                    jsonIntArray.add( new JsonPrimitive( i ) );
                }

                return jsonIntArray;
            case Tag.LONG_ARRAY:
                long[] longArray = ( (LongArrayTag) tag ).getValue();

                JsonArray jsonLongArray = new JsonArray( longArray.length );
                for ( long l : longArray )
                {
                    jsonLongArray.add( new JsonPrimitive( l ) );
                }

                return jsonLongArray;
            case Tag.END:
                return JsonNull.INSTANCE;
            default:
                throw new IllegalArgumentException( "Unknown NBT tag: " + tag );
        }
    }
}
