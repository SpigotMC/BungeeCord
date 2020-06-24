package net.md_5.bungee.api.plugin;

public final class DummyPlugin extends Plugin
{

    public static final DummyPlugin INSTANCE = new DummyPlugin();

    private DummyPlugin()
    {
        super( null, null );
    }
}
