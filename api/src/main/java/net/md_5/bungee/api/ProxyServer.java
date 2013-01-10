package net.md_5.bungee.api;

import com.google.common.base.Preconditions;
import lombok.Getter;

public abstract class ProxyServer {

    @Getter
    private static ProxyServer instance;

    public static void setInstance(ProxyServer instance) {
        Preconditions.checkNotNull(instance, "Instance null");
        Preconditions.checkArgument(instance == null, "Instance already set");
        ProxyServer.instance = instance;
    }
}
