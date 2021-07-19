package net.md_5.bungee.api.event;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.plugin.TabExecutor;

/**
 * Event called when a downstream server (on 1.13+) sends the command structure
 * to a player, but before BungeeCord adds the dummy command nodes of
 * registered commands.
 * <p>
 * BungeeCord will not overwrite the modifications made by the listeners.
 *
 * <h2>Usage example</h2>
 * Here is a usage example of this event, to declare a command structure.
 * This illustrates the commands /server and /send of Bungee.
 * <pre>
 * event.getRoot().addChild( LiteralArgumentBuilder.&lt;CommandSender&gt;literal( "server" )
 *         .requires( sender -&gt; sender.hasPermission( "bungeecord.command.server" ) )
 *         .executes( a -&gt; 0 )
 *         .then( RequiredArgumentBuilder.argument( "serverName", StringArgumentType.greedyString() )
 *                 .suggests( SuggestionRegistry.ASK_SERVER )
 *         )
 *         .build()
 * );
 * event.getRoot().addChild( LiteralArgumentBuilder.&lt;CommandSender&gt;literal( "send" )
 *         .requires( sender -&gt; sender.hasPermission( "bungeecord.command.send" ) )
 *         .then( RequiredArgumentBuilder.argument( "playerName", StringArgumentType.word() )
 *                 .suggests( SuggestionRegistry.ASK_SERVER )
 *                 .then( RequiredArgumentBuilder.argument( "serverName", StringArgumentType.greedyString() )
 *                         .suggests( SuggestionRegistry.ASK_SERVER )
 *                 )
 *         )
 *         .build()
 * );
 * </pre>
 *
 * <h2>Flag a {@link CommandNode} as executable or not</h2>
 * The implementation of a {@link com.mojang.brigadier.Command Command} used in
 * {@link ArgumentBuilder#executes(com.mojang.brigadier.Command)} will never be
 * executed. This will only tell to the client if the current node is
 * executable or not.
 * <ul>
 *     <li>
 *         {@code builder.executes(null)} (default) to mark the node as not
 *         executable.
 *     </li>
 *     <li>
 *         {@code builder.executes(a -> 0)}, or any non null argument, to mark
 *         the node as executable (the child arguments are displayed as
 *         optional).
 *     </li>
 * </ul>
 *
 * <h2>{@link CommandNode}’s suggestions management</h2>
 * The implementation of a SuggestionProvider used in
 * {@link RequiredArgumentBuilder#suggests(SuggestionProvider)} will never be
 * executed. This will only tell to the client how to deal with the
 * auto-completion of the argument.
 * <ul>
 *     <li>
 *         {@code builder.suggests(null)} (default) to disable auto-completion
 *         for this argument.
 *     </li>
 *     <li>
 *         {@code builder.suggests(SuggestionRegistry.ALL_RECIPES)} to suggest
 *         Minecraft’s recipes.
 *     </li>
 *     <li>
 *         {@code builder.suggests(SuggestionRegistry.AVAILABLE_SOUNDS)} to
 *         suggest Minecraft’s default sound identifiers.
 *     </li>
 *     <li>
 *         {@code builder.suggests(SuggestionRegistry.SUMMONABLE_ENTITIES)} to
 *         suggest Minecraft’s default summonable entities identifiers.
 *     </li>
 *     <li>
 *         {@code builder.suggests(SuggestionRegistry.ASK_SERVER)}, or any
 *         other non null argument, to make the Minecraft client ask
 *         auto-completion to the server. Any specified implementation of
 *         {@link SuggestionProvider} will never be executed.
 *     </li>
 * </ul>
 *
 * <h2>Argument types</h2>
 * When building a new argument command node using
 * {@link RequiredArgumentBuilder#argument(String, ArgumentType)}, you have to
 * specify an {@link ArgumentType}. You can use all subclasses of
 * {@link ArgumentType} provided with brigadier (for instance,
 * {@link StringArgumentType} or {@link IntegerArgumentType}), or call any
 * {@code ArgumentRegistry.minecraft*()} methods to use a {@code minecraft:*}
 * argument type.
 *
 * <h2>Limitations with brigadier API</h2>
 * This event is only used for the client to show command syntax, suggest
 * sub-commands and color the arguments in the chat box. The command execution
 * needs to be implemented using {@link PluginManager#registerCommand(Plugin,
 * Command)} and the server-side tab-completion using {@link TabCompleteEvent}
 * or {@link TabExecutor}.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommandsDeclareEvent extends TargetedEvent
{
    /**
     * Wether or not the command tree is modified by this event.
     *
     * If this value is set to true, BungeeCord will ensure that the
     * modifications made in the command tree, will be sent to the player.
     * If this is false, the modifications may not be taken into account.
     *
     * When calling {@link #getRoot()}, this value is automatically set
     * to true.
     */
    @Setter(value = AccessLevel.NONE)
    private boolean modified = false;

    /**
     * The root command node of the command structure that will be send to the
     * player.
     */
    private final RootCommandNode<CommandSender> root;

    public CommandsDeclareEvent(Connection sender, Connection receiver, RootCommandNode<CommandSender> root)
    {
        super( sender, receiver );
        this.root = root;
    }

    /**
     * The root command node of the command structure that will be send to the
     * player.
     * @return The root command node
     */
    public RootCommandNode<CommandSender> getRoot()
    {
        modified = true;
        return root;
    }
}
