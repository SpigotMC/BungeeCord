package net.md_5.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;

public class CommandBuilder
{

    private String name = null;
    private String permission = null;
    private String[] aliases = null;
    private CommandExecutor commandExecutor = null;

    /**
     * Construct a new builder with no name, permissions, aliases or executor.
     */
    public CommandBuilder() {}

    /**
     * Construct a new builder with no permissions, aliases or executor.
     *
     * @param name the name of this command.
     */
    public CommandBuilder(String name)
    {
        this.name = name;
    }

    /**
     * Change or set the command's name.
     *
     * @param name the name of this command
     * @return This command builder
     */
    public CommandBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Change or set the command's permission.
     *
     * @param permission the permission node required to execute this command,
     *                   null or empty string allows it to be executed by everyone
     * @return This command builder
     */
    public CommandBuilder setPermission(String permission)
    {
        this.permission = permission;
        return this;
    }

    /**
     * Change or set the command's aliases.
     *
     * @param aliases aliases which map back to this command
     * @return This command builder
     */
    public CommandBuilder setAliases(String... aliases)
    {
        this.aliases = aliases;
        return this;
    }

    /**
     * Change or set the command's executor.
     *
     * @param commandExecutor the executor of this command
     * @return This command builder
     */
    public CommandBuilder setExecutor(CommandExecutor commandExecutor)
    {
        this.commandExecutor = commandExecutor;
        return this;
    }

    /**
     * Build/Create the command about the overdo variables.
     *
     * @return the command
     */
    public Command create()
    {
        if (name == null)
        {
            throw new RuntimeException("The name can't be null.");
        }
        if (commandExecutor == null)
        {
            throw new RuntimeException("The commandExecutor can't be null.");
        }
        return new Command(name, permission, aliases == null ? new String[0] : aliases)
        {
            @Override
            public void execute(CommandSender sender, String[] args)
            {
                commandExecutor.execute(sender, args);
            }
        };
    }

}
