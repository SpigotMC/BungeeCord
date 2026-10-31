package net.md_5.bungee;

import net.md_5.bungee.api.plugin.Plugin;

public final class DummyPlugin extends Plugin
{

    public static final DummyPlugin INSTANCE = new DummyPlugin();

    private DummyPlugin()
    {
        super( null, null );
    }
}
