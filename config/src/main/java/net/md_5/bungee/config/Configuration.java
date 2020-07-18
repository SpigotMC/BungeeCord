package net.md_5.bungee.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Configuration
{

    private static final char SEPARATOR = '.';
    final Map<String, Object> self;
    private final Configuration defaults;

    public Configuration()
    {
        this( null );
    }

    public Configuration(@Nullable Configuration defaults)
    {
        this( new LinkedHashMap<String, Object>(), defaults );
    }

    Configuration(Map<?, ?> map, Configuration defaults)
    {
        this.self = new LinkedHashMap<>();
        this.defaults = defaults;

        for ( Map.Entry<?, ?> entry : map.entrySet() )
        {
            String key = ( entry.getKey() == null ) ? "null" : entry.getKey().toString();

            if ( entry.getValue() instanceof Map )
            {
                this.self.put( key, new Configuration( (Map) entry.getValue(), ( defaults == null ) ? null : defaults.getSection( key ) ) );
            } else
            {
                this.self.put( key, entry.getValue() );
            }
        }
    }

    @NotNull
    @Contract(pure = true)
    private Configuration getSectionFor(@NotNull String path)
    {
        int index = path.indexOf( SEPARATOR );
        if ( index == -1 )
        {
            return this;
        }

        String root = path.substring( 0, index );
        Object section = self.get( root );
        if ( section == null )
        {
            section = new Configuration( ( defaults == null ) ? null : defaults.getSection( root ) );
            self.put( root, section );
        }

        return (Configuration) section;
    }

    @NotNull
    @Contract(pure = true)
    private String getChild(@NotNull String path)
    {
        int index = path.indexOf( SEPARATOR );
        return ( index == -1 ) ? path : path.substring( index + 1 );
    }

    /*------------------------------------------------------------------------*/
    @Nullable
    @Contract(value = "!null, !null -> !null; !null, null -> _", pure = true)
    @SuppressWarnings("unchecked")
    public <T> T get(@NotNull String path, @Nullable T def)
    {
        Configuration section = getSectionFor( path );
        Object val;
        if ( section == this )
        {
            val = self.get( path );
        } else
        {
            val = section.get( getChild( path ), def );
        }

        if ( val == null && def instanceof Configuration )
        {
            self.put( path, def );
        }

        return ( val != null ) ? (T) val : def;
    }

    @Contract(pure = true)
    public boolean contains(@NotNull String path)
    {
        return get( path, null ) != null;
    }

    @Nullable
    @Contract(pure = true)
    public Object get(@NotNull String path)
    {
        return get( path, getDefault( path ) );
    }

    @Nullable
    public Object getDefault(@NotNull String path)
    {
        return ( defaults == null ) ? null : defaults.get( path );
    }

    @Contract(mutates = "this")
    public void set(@NotNull String path, @Nullable Object value)
    {
        if ( value instanceof Map )
        {
            value = new Configuration( (Map) value, ( defaults == null ) ? null : defaults.getSection( path ) );
        }

        Configuration section = getSectionFor( path );
        if ( section == this )
        {
            if ( value == null )
            {
                self.remove( path );
            } else
            {
                self.put( path, value );
            }
        } else
        {
            section.set( getChild( path ), value );
        }
    }

    /*------------------------------------------------------------------------*/
    @NotNull
    @Contract(pure = true)
    public Configuration getSection(@NotNull String path)
    {
        Object def = getDefault( path );
        return (Configuration) get( path, ( def instanceof Configuration ) ? def : new Configuration( ( defaults == null ) ? null : defaults.getSection( path ) ) );
    }

    /**
     * Gets keys, not deep by default.
     *
     * @return top level keys for this section
     */
    @NotNull
    @Contract(pure = true)
    public Collection<String> getKeys()
    {
        return new LinkedHashSet<>( self.keySet() );
    }

    /*------------------------------------------------------------------------*/
    @Contract(pure = true)
    public byte getByte(@NotNull String path)
    {
        Object def = getDefault( path );
        return getByte( path, ( def instanceof Number ) ? ( (Number) def ).byteValue() : 0 );
    }

    @Contract(pure = true)
    public byte getByte(@NotNull String path, byte def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).byteValue() : def;
    }

    @Contract(pure = true)
    public List<Byte> getByteList(@NotNull String path)
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

    @Contract(pure = true)
    public short getShort(@NotNull String path)
    {
        Object def = getDefault( path );
        return getShort( path, ( def instanceof Number ) ? ( (Number) def ).shortValue() : 0 );
    }

    @Contract(pure = true)
    public short getShort(@NotNull String path, short def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).shortValue() : def;
    }

    @NotNull
    @Contract(pure = true)
    public List<Short> getShortList(@NotNull String path)
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

    @Contract(pure = true)
    public int getInt(@NotNull String path)
    {
        Object def = getDefault( path );
        return getInt( path, ( def instanceof Number ) ? ( (Number) def ).intValue() : 0 );
    }

    @Contract(pure = true)
    public int getInt(@NotNull String path, int def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).intValue() : def;
    }

    @NotNull
    @Contract(pure = true)
    public List<Integer> getIntList(@NotNull String path)
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

    @Contract(pure = true)
    public long getLong(@NotNull String path)
    {
        Object def = getDefault( path );
        return getLong( path, ( def instanceof Number ) ? ( (Number) def ).longValue() : 0 );
    }

    @Contract(pure = true)
    public long getLong(@NotNull String path, long def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).longValue() : def;
    }

    @NotNull
    @Contract(pure = true)
    public List<Long> getLongList(@NotNull String path)
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

    @Contract(pure = true)
    public float getFloat(@NotNull String path)
    {
        Object def = getDefault( path );
        return getFloat( path, ( def instanceof Number ) ? ( (Number) def ).floatValue() : 0 );
    }

    @Contract(pure = true)
    public float getFloat(@NotNull String path, float def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).floatValue() : def;
    }

    @NotNull
    @Contract(pure = true)
    public List<Float> getFloatList(@NotNull String path)
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

    @Contract(pure = true)
    public double getDouble(@NotNull String path)
    {
        Object def = getDefault( path );
        return getDouble( path, ( def instanceof Number ) ? ( (Number) def ).doubleValue() : 0 );
    }

    @Contract(pure = true)
    public double getDouble(@NotNull String path, double def)
    {
        Object val = get( path, def );
        return ( val instanceof Number ) ? ( (Number) val ).doubleValue() : def;
    }

    @NotNull
    @Contract(pure = true)
    public List<Double> getDoubleList(@NotNull String path)
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

    @Contract(pure = true)
    public boolean getBoolean(@NotNull String path)
    {
        Object def = getDefault( path );
        return getBoolean( path, ( def instanceof Boolean ) ? (Boolean) def : false );
    }

    @Contract(pure = true)
    public boolean getBoolean(@NotNull String path, boolean def)
    {
        Object val = get( path, def );
        return ( val instanceof Boolean ) ? (Boolean) val : def;
    }

    @NotNull
    @Contract(pure = true)
    public List<Boolean> getBooleanList(@NotNull String path)
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

    @Contract(pure = true)
    public char getChar(@NotNull String path)
    {
        Object def = getDefault( path );
        return getChar( path, ( def instanceof Character ) ? (Character) def : '\u0000' );
    }

    @Contract(pure = true)
    public char getChar(@NotNull String path, char def)
    {
        Object val = get( path, def );
        return ( val instanceof Character ) ? (Character) val : def;
    }

    @NotNull
    @Contract(pure = true)
    public List<Character> getCharList(@NotNull String path)
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

    @NotNull
    @Contract(pure = true)
    public String getString(@NotNull String path)
    {
        Object def = getDefault( path );
        return getString( path, ( def instanceof String ) ? (String) def : "" );
    }

    @Nullable
    @Contract(value = "!null, null -> _;!null, !null -> !null", pure = true)
    public String getString(@NotNull String path, @Nullable String def)
    {
        Object val = get( path, def );
        return ( val instanceof String ) ? (String) val : def;
    }

    @NotNull
    @Contract(pure = true)
    public List<String> getStringList(@NotNull String path)
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
    @NotNull
    @Contract(pure = true)
    public List<?> getList(@NotNull String path)
    {
        Object def = getDefault( path );
        return getList( path, ( def instanceof List<?> ) ? (List<?>) def : Collections.EMPTY_LIST );
    }

    @Nullable
    @Contract(value = "!null, !null -> !null; !null, null -> _; null, _ -> fail", pure = true)
    public List<?> getList(@NotNull String path, @Nullable List<?> def)
    {
        Object val = get( path, def );
        return ( val instanceof List<?> ) ? (List<?>) val : def;
    }
}
