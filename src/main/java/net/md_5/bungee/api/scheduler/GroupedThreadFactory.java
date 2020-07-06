package net.md_5.bungee.api.scheduler;

import lombok.Data;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.ThreadFactory;

@Data
@Deprecated
public class GroupedThreadFactory implements ThreadFactory {

    private final ThreadGroup group;

    public GroupedThreadFactory(Plugin plugin, String name) {
        this.group = new BungeeGroup(name);
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(group, r);
    }

    public static final class BungeeGroup extends ThreadGroup {

        private BungeeGroup(String name) {
            super(name);
        }

    }
}
