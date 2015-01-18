package net.md_5.bungee.api.plugin;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DummyPlugin extends Plugin
{

    public static final DummyPlugin INSTANCE = new DummyPlugin();
}
