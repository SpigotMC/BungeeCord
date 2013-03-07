package net.md_5.bungee.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Configuration
{

    private static final char SEPARATOR = '.';
    private final Map<String, Object> self;
    private Map<String, Object> comments = new HashMap<>();
    private final Configuration defaults;

    private Map<String, Object> getHolder(String path, Map<String, Object> parent, boolean create)
    {
        return null;
    }

    private Object get(String path, Map<String, Object> holder)
    {
        int index = path.indexOf( SEPARATOR );
        String first, second;
        if ( index == -1 )
        {
            second = path;
        } else
        {
            first = path.substring( 0, index );
            second = path.substring( index + 1, path.length() );
        }
        return null;
    }

    /*------------------------------------------------------------------------*/
    @SuppressWarnings("unchecked")
    public <T> T get(String path, T def)
    {
        Object val = get( path, self );
        return ( val != null && val.getClass().isInstance( def ) ) ? (T) val : (T) defaults.get( path );
    }

    public Object get(String path)
    {
        return get( path, null );
    }

    public Object getDefault(String path)
    {
        return defaults.get( path );
    }

    public void set(String path, Object value, String comment)
    {
        String child = path.substring( path.indexOf( SEPARATOR ) + 1 );
        getHolder( path, self, true ).put( child, value );
        getHolder( path, comments, true ).put( child, value );
    }

    public void set(String path, Object value)
    {
        set( path, value, null );
    }

    /*------------------------------------------------------------------------*/
    public byte getByte(String path)
    {
        Object def = getDefault( path );
        return getByte( path, ( def instanceof Number ) ? ( (Number) def ).byteValue() : 0 );
    }

    public byte getByte(String path, byte def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).byteValue() : def;
    }

    public List<Byte> getByteList(String path)
    {
        List<?> list = getList( path );
        List<Byte> result = new ArrayList<>();

        for ( Object object : list )
        {
            if ( object instanceof Number )
            {
                result.add( ( (Number) object ).byteValue() );
            }
        }

        return result;
    }

    public short getShort(String path)
    {
        Object def = getDefault( path );
        return getShort( path, ( def instanceof Number ) ? ( (Number) def ).shortValue() : 0 );
    }

    public short getShort(String path, short def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).shortValue() : def;
    }

    public List<Short> getShortList(String path)
    {
        List<?> list = getList( path );
        List<Short> result = new ArrayList<>();

        for ( Object object : list )
        {
            if ( object instanceof Number )
            {
                result.add( ( (Number) object ).shortValue() );
            }
        }

        return result;
    }

    public int getInt(String path)
    {
        Object def = getDefault( path );
        return getInt( path, ( def instanceof Number ) ? ( (Number) def ).intValue() : 0 );
    }

    public int getInt(String path, int def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).intValue() : def;
    }

    public List<Integer> getIntList(String path)
    {
        List<?> list = getList( path );
        List<Integer> result = new ArrayList<>();

        for ( Object object : list )
        {
            if ( object instanceof Number )
            {
                result.add( ( (Number) object ).intValue() );
            }
        }

        return result;
    }

    public long getLong(String path)
    {
        Object def = getDefault( path );
        return getLong( path, ( def instanceof Number ) ? ( (Number) def ).longValue() : 0 );
    }

    public long getLong(String path, long def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).longValue() : def;
    }

    public List<Long> getLongList(String path)
    {
        List<?> list = getList( path );
        List<Long> result = new ArrayList<>();

        for ( Object object : list )
        {
            if ( object instanceof Number )
            {
                result.add( ( (Number) object ).longValue() );
            }
        }

        return result;
    }

    public float getFloat(String path)
    {
        Object def = getDefault( path );
        return getFloat( path, ( def instanceof Number ) ? ( (Number) def ).floatValue() : 0 );
    }

    public float getFloat(String path, float def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).floatValue() : def;
    }

    public List<Float> getFloatList(String path)
    {
        List<?> list = getList( path );
        List<Float> result = new ArrayList<>();

        for ( Object object : list )
        {
            if ( object instanceof Number )
            {
                result.add( ( (Number) object ).floatValue() );
            }
        }

        return result;
    }

    public double getDouble(String path)
    {
        Object def = getDefault( path );
        return getDouble( path, ( def instanceof Number ) ? ( (Number) def ).doubleValue() : 0 );
    }

    public double getDouble(String path, double def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).doubleValue() : def;
    }

    public List<Double> getDoubleList(String path)
    {
        List<?> list = getList( path );
        List<Double> result = new ArrayList<>();

        for ( Object object : list )
        {
            if ( object instanceof Number )
            {
                result.add( ( (Number) object ).doubleValue() );
            }
        }

        return result;
    }

    public boolean getBoolean(String path)
    {
        Object def = getDefault( path );
        return getBoolean( path, ( def instanceof Boolean ) ? (Boolean) def : false );
    }

    public boolean getBoolean(String path, boolean def)
    {
        Object val = get( path, def );
        return ( val instanceof Boolean ) ? (Boolean) val : def;
    }

    public List<Boolean> getBooleanList(String path)
    {
        List<?> list = getList( path );
        List<Boolean> result = new ArrayList<>();

        for ( Object object : list )
        {
            if ( object instanceof Boolean )
            {
                result.add( (Boolean) object );
            }
        }

        return result;
    }

    public char getChar(String path)
    {
        Object def = getDefault( path );
        return getChar( path, ( def instanceof Character ) ? (Character) def : '\u0000' );
    }

    public char getChar(String path, char def)
    {
        Object val = get( path, def );
        return ( val instanceof Character ) ? (Character) val : def;
    }

    public List<Character> getCharList(String path)
    {
        List<?> list = getList( path );
        List<Character> result = new ArrayList<>();

        for ( Object object : list )
        {
            if ( object instanceof Character )
            {
                result.add( (Character) object );
            }
        }

        return result;
    }

    public String getString(String path)
    {
        Object def = getDefault( path );
        return getString( path, ( def instanceof String ) ? (String) def : "" );
    }

    public String getString(String path, String def)
    {
        Object val = get( path, def );
        return ( val instanceof String ) ? (String) val : def;
    }

    public List<String> getStringList(String path)
    {
        List<?> list = getList( path );
        List<String> result = new ArrayList<>();

        for ( Object object : list )
        {
            if ( object instanceof String )
            {
                result.add( (String) object );
            }
        }

        return result;
    }

    /*------------------------------------------------------------------------*/
    public List<?> getList(String path)
    {
        Object def = getDefault( path );
        return getList( path, ( def instanceof List<?> ) ? (List<?>) def : Collections.EMPTY_LIST );
    }

    public List<?> getList(String path, List<?> def)
    {
        Object val = get( path, def );
        return ( val instanceof List<?> ) ? (List<?>) val : def;
    }
}
