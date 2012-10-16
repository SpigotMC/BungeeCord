package net.md_5.bungee;

public enum Permission
{

    /**
     * Can access all commands.
     */
    ADMIN,
    /**
     * Can access commands which do not affect everyone.
     */
    MODERATOR,
    /**
     * Can access other commands.
     */
    DEFAULT;
}
