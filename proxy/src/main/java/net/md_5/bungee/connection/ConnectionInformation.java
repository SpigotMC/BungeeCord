package net.md_5.bungee.connection;

import net.md_5.bungee.api.connection.Information;

import java.util.HashMap;
import java.util.Map;

public class ConnectionInformation implements Information
{
    private final Map< String, Object > objectMap = new HashMap<>();

    @Override
    public Object getObject( String key )
    {
        return objectMap.get( key );
    }

    @Override
    public String getString( String key )
    {
        return ( String ) objectMap.get( key );
    }

    @Override
    public int getInt( String key )
    {
        return ( Integer ) objectMap.get( key );
    }

    @Override
    public boolean geBoolean( String key )
    {
        return ( Boolean ) objectMap.get( key );
    }

    @Override
    public long getLong( String key )
    {
        return ( Long ) objectMap.get( key );
    }

    @Override
    public double getDouble( String key )
    {
        return ( Double ) objectMap.get( key );
    }

    @Override
    public float getFloat( String key )
    {
        return ( Float ) objectMap.get( key );
    }

    @Override
    public Map< String, Object > getObjectMap()
    {
        return objectMap;
    }

    @Override
    public void set( String key, Object object )
    {
        objectMap.put( key, object );
    }
}
