package net.md_5.bungee.api.plugin;

import lombok.Data;

@Data
public class PluginDescription
{

    private final String name;
    private final String main;
    private final String version;
    private final String author;
}
