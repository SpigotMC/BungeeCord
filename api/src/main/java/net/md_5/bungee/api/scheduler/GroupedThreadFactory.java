package net.md_5.bungee.api.scheduler;

import java.util.concurrent.ThreadFactory;
import lombok.Data;
import net.md_5.bungee.api.plugin.Plugin;

@Data
@Deprecated
public class GroupedThreadFactory implements ThreadFactory
{

    private final ThreadGroup group;

    public static class BungeeGroup extends ThreadGroup
    {

        private BungeeGroup(String name)
        {
            super( name );
        }

    }

    public GroupedThreadFactory(Plugin plugin)
    {
        this.group = new BungeeGroup( plugin.getDescription().getName() );
    }

    @Override
    public Thread newThread(Runnable r)
    {
        return new Thread( group, r );
    }
}
