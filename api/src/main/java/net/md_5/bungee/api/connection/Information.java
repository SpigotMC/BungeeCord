package net.md_5.bungee.api.connection;

import java.util.Map;

public interface Information
{
    Object getObject( String key );

    String getString( String key );

    int getInt( String key );

    boolean geBoolean( String key );

    long getLong( String key );

    double getDouble( String key );

    float getFloat( String key );

    Map< String, Object > getObjectMap();

    void set( String key, Object object );
}
