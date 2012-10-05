package net.md_5.bungee.plugin;

public interface Cancellable {

    public void setCancelled(boolean cancelled);

    public boolean isCancelled();
}
