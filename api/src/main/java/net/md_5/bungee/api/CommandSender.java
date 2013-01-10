package net.md_5.bungee.api;

import java.util.Collection;

public interface CommandSender
{

    public String getName();

    public void sendMessage(String message);

    public Collection<String> getGroups();

    public void addGroups(String... groups);

    public void removeGroups(String... groups);

    public boolean hasPermission(String permission);

    public boolean setPermission(String permission, boolean value);
}
