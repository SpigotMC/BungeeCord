package net.md_5.bungee.api.plugin;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command that can be executed by a {@link CommandSender}.
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.NONE)
public abstract class Command
{

    @NotNull
    private final String name;
    @Nullable
    private final String permission;
    @NotNull
    private final String[] aliases;

    /**
     * Construct a new command with no permissions or aliases.
     *
     * @param name the name of this command
     */
    public Command(@NotNull String name)
    {
        this( name, null );
    }

    /**
     * Construct a new command.
     *
     * @param name primary name of this command
     * @param permission the permission node required to execute this command,
     * null or empty string allows it to be executed by everyone
     * @param aliases aliases which map back to this command
     */
    public Command(@NotNull String name, @Nullable String permission, @NotNull String... aliases)
    {
        Preconditions.checkArgument( name != null, "name" );
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
    }

    /**
     * Execute this command with the specified sender and arguments.
     *
     * @param sender the executor of this command
     * @param args arguments used to invoke this command
     */
    public abstract void execute(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Check if this command can be executed by the given sender.
     *
     * @param sender the sender to check
     * @return whether the sender can execute this
     */
    public boolean hasPermission(CommandSender sender)
    {
        return permission == null || permission.isEmpty() || sender.hasPermission( permission );
    }
}
