package net.md_5.bungee.protocol.packet;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.netty.buffer.ByteBuf;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Commands extends DefinedPacket
{

    private static final int FLAG_TYPE = 0x3;
    private static final int FLAG_EXECUTABLE = 0x4;
    private static final int FLAG_REDIRECT = 0x8;
    private static final int FLAG_SUGGESTIONS = 0x10;
    //
    private static final int NODE_ROOT = 0;
    private static final int NODE_LITERAL = 1;
    private static final int NODE_ARGUMENT = 2;
    //
    private RootCommandNode root;

    @Override
    public void read(ByteBuf buf)
    {
        int nodeCount = readVarInt( buf );
        NetworkNode[] nodes = new NetworkNode[ nodeCount ];
        Deque<NetworkNode> nodeQueue = new ArrayDeque<>( nodes.length );

        for ( int i = 0; i < nodeCount; i++ )
        {
            byte flags = buf.readByte();
            int[] children = readVarIntArray( buf );
            int redirectNode = ( ( flags & FLAG_REDIRECT ) != 0 ) ? readVarInt( buf ) : 0;
            ArgumentBuilder argumentBuilder;

            switch ( flags & FLAG_TYPE )
            {
                case NODE_ROOT:
                    argumentBuilder = null;
                    break;
                case NODE_LITERAL:
                    argumentBuilder = LiteralArgumentBuilder.literal( readString( buf ) );
                    break;
                case NODE_ARGUMENT:
                    String name = readString( buf );
                    String parser = readString( buf );

                    argumentBuilder = RequiredArgumentBuilder.argument( name, ArgumentRegistry.read( parser, buf ) );

                    if ( ( flags & FLAG_SUGGESTIONS ) != 0 )
                    {
                        String suggster = readString( buf );
                        ( (RequiredArgumentBuilder) argumentBuilder ).suggests( SuggestionRegistry.getProvider( suggster ) );
                    }
                    break;
                default:
                    throw new IllegalArgumentException( "Unhandled node type " + flags );
            }

            NetworkNode node = new NetworkNode( argumentBuilder, flags, redirectNode, children );

            nodes[i] = node;
            nodeQueue.add( node );
        }

        boolean mustCycle;
        do
        {
            if ( nodeQueue.isEmpty() )
            {
                int rootIndex = readVarInt( buf );
                root = (RootCommandNode<?>) nodes[rootIndex].command;
                return;
            }

            mustCycle = false;

            for ( Iterator<NetworkNode> iter = nodeQueue.iterator(); iter.hasNext(); )
            {
                NetworkNode node = iter.next();
                if ( node.buildSelf( nodes ) )
                {
                    iter.remove();
                    mustCycle = true;
                }
            }
        } while ( mustCycle );

        throw new IllegalStateException( "Did not finish building root node" );
    }

    @Override
    public void write(ByteBuf buf)
    {
        Map<CommandNode, Integer> indexMap = new LinkedHashMap<>();
        Deque<CommandNode> nodeQueue = new ArrayDeque<>();
        nodeQueue.add( root );

        while ( !nodeQueue.isEmpty() )
        {
            CommandNode command = nodeQueue.pollFirst();

            if ( !indexMap.containsKey( command ) )
            {
                // Index the new node
                int currentIndex = indexMap.size();
                indexMap.put( command, currentIndex );

                // Queue children and redirect for processing
                nodeQueue.addAll( command.getChildren() );
                if ( command.getRedirect() != null )
                {
                    nodeQueue.add( command.getRedirect() );
                }
            }
        }

        // Write out size
        writeVarInt( indexMap.size(), buf );

        int currentIndex = 0;
        for ( Map.Entry<CommandNode, Integer> entry : indexMap.entrySet() )
        {
            // Using a LinkedHashMap, but sanity check this assumption
            Preconditions.checkState( entry.getValue() == currentIndex++, "Iteration out of order!" );

            CommandNode node = entry.getKey();
            byte flags = 0;

            if ( node.getRedirect() != null )
            {
                flags |= FLAG_REDIRECT;
            }
            if ( node.getCommand() != null )
            {
                flags |= FLAG_EXECUTABLE;
            }

            if ( node instanceof RootCommandNode )
            {
                flags |= NODE_ROOT;
            } else if ( node instanceof LiteralCommandNode )
            {
                flags |= NODE_LITERAL;
            } else if ( node instanceof ArgumentCommandNode )
            {
                flags |= NODE_ARGUMENT;
                if ( ( (ArgumentCommandNode) node ).getCustomSuggestions() != null )
                {
                    flags |= FLAG_SUGGESTIONS;
                }
            } else
            {
                throw new IllegalArgumentException( "Unhandled node type " + node );
            }

            buf.writeByte( flags );

            writeVarInt( node.getChildren().size(), buf );
            for ( CommandNode child : (Collection<CommandNode>) node.getChildren() )
            {
                writeVarInt( indexMap.get( child ), buf );
            }
            if ( node.getRedirect() != null )
            {
                writeVarInt( indexMap.get( node.getRedirect() ), buf );
            }

            if ( node instanceof LiteralCommandNode )
            {
                writeString( ( (LiteralCommandNode) node ).getLiteral(), buf );
            } else if ( node instanceof ArgumentCommandNode )
            {
                ArgumentCommandNode argumentNode = (ArgumentCommandNode) node;

                writeString( argumentNode.getName(), buf );
                ArgumentRegistry.write( argumentNode.getType(), buf );

                if ( argumentNode.getCustomSuggestions() != null )
                {
                    writeString( SuggestionRegistry.getKey( argumentNode.getCustomSuggestions() ), buf );
                }
            }
        }

        // Get, check, and write the root index (should be first)
        int rootIndex = indexMap.get( root );
        Preconditions.checkState( rootIndex == 0, "How did root not land up at index 0?!?" );
        writeVarInt( rootIndex, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Data
    private static class NetworkNode
    {

        private final ArgumentBuilder argumentBuilder;
        private final byte flags;
        private final int redirectNode;
        private final int[] children;
        private CommandNode command;

        private boolean buildSelf(NetworkNode[] otherNodes)
        {
            // First cycle
            if ( command == null )
            {
                // Root node is merely the root
                if ( argumentBuilder == null )
                {
                    command = new RootCommandNode();
                } else
                {
                    // Add the redirect
                    if ( ( flags & FLAG_REDIRECT ) != 0 )
                    {
                        if ( otherNodes[redirectNode].command == null )
                        {
                            return false;
                        }

                        argumentBuilder.redirect( otherNodes[redirectNode].command );
                    }

                    // Add dummy executable
                    if ( ( flags & FLAG_EXECUTABLE ) != 0 )
                    {
                        argumentBuilder.executes( new Command()
                        {
                            @Override
                            public int run(CommandContext context) throws CommandSyntaxException
                            {
                                return 0;
                            }
                        } );
                    }

                    // Build our self command
                    command = argumentBuilder.build();
                }
            }

            // Check that we have processed all children thus far
            for ( int childIndex : children )
            {
                if ( otherNodes[childIndex].command == null )
                {
                    // If not, we have to do another cycle
                    return false;
                }
            }

            for ( int childIndex : children )
            {
                CommandNode<?> child = otherNodes[childIndex].command;
                Preconditions.checkArgument( !( child instanceof RootCommandNode ), "Cannot have RootCommandNode as child" );

                command.addChild( child );
            }

            return true;
        }
    }

    @Data
    private static class ArgumentRegistry
    {

        private static final Map<String, ArgumentSerializer> PROVIDERS = new HashMap<>();
        private static final Map<Class<?>, ProperArgumentSerializer<?>> PROPER_PROVIDERS = new HashMap<>();
        //
        private static final ArgumentSerializer<Void> VOID = new ArgumentSerializer<Void>()
        {
            @Override
            protected Void read(ByteBuf buf)
            {
                return null;
            }

            @Override
            protected void write(ByteBuf buf, Void t)
            {
            }
        };
        private static final ArgumentSerializer<Boolean> BOOLEAN = new ArgumentSerializer<Boolean>()
        {
            @Override
            protected Boolean read(ByteBuf buf)
            {
                return buf.readBoolean();
            }

            @Override
            protected void write(ByteBuf buf, Boolean t)
            {
                buf.writeBoolean( t );
            }
        };
        private static final ArgumentSerializer<Byte> BYTE = new ArgumentSerializer<Byte>()
        {
            @Override
            protected Byte read(ByteBuf buf)
            {
                return buf.readByte();
            }

            @Override
            protected void write(ByteBuf buf, Byte t)
            {
                buf.writeByte( t );
            }
        };
        private static final ArgumentSerializer<FloatArgumentType> FLOAT = new ArgumentSerializer<FloatArgumentType>()
        {
            @Override
            protected FloatArgumentType read(ByteBuf buf)
            {
                byte flags = buf.readByte();
                float min = ( flags & 0x1 ) != 0 ? buf.readFloat() : -Float.MAX_VALUE;
                float max = ( flags & 0x2 ) != 0 ? buf.readFloat() : -Float.MAX_VALUE;

                return FloatArgumentType.floatArg( min, max );
            }

            @Override
            protected void write(ByteBuf buf, FloatArgumentType t)
            {
                boolean hasMin = t.getMinimum() != -Float.MAX_VALUE;
                boolean hasMax = t.getMaximum() != Float.MAX_VALUE;

                buf.writeByte( binaryFlag( hasMin, hasMax ) );
                if ( hasMin )
                {
                    buf.writeFloat( t.getMinimum() );
                }
                if ( hasMax )
                {
                    buf.writeFloat( t.getMaximum() );
                }
            }
        };
        private static final ArgumentSerializer<DoubleArgumentType> DOUBLE = new ArgumentSerializer<DoubleArgumentType>()
        {
            @Override
            protected DoubleArgumentType read(ByteBuf buf)
            {
                byte flags = buf.readByte();
                double min = ( flags & 0x1 ) != 0 ? buf.readDouble() : -Double.MAX_VALUE;
                double max = ( flags & 0x2 ) != 0 ? buf.readDouble() : -Double.MAX_VALUE;

                return DoubleArgumentType.doubleArg( min, max );
            }

            @Override
            protected void write(ByteBuf buf, DoubleArgumentType t)
            {
                boolean hasMin = t.getMinimum() != -Double.MAX_VALUE;
                boolean hasMax = t.getMaximum() != Double.MAX_VALUE;

                buf.writeByte( binaryFlag( hasMin, hasMax ) );
                if ( hasMin )
                {
                    buf.writeDouble( t.getMinimum() );
                }
                if ( hasMax )
                {
                    buf.writeDouble( t.getMaximum() );
                }
            }
        };
        private static final ArgumentSerializer<IntegerArgumentType> INTEGER = new ArgumentSerializer<IntegerArgumentType>()
        {
            @Override
            protected IntegerArgumentType read(ByteBuf buf)
            {
                byte flags = buf.readByte();
                int min = ( flags & 0x1 ) != 0 ? buf.readInt() : Integer.MIN_VALUE;
                int max = ( flags & 0x2 ) != 0 ? buf.readInt() : Integer.MAX_VALUE;

                return IntegerArgumentType.integer( min, max );
            }

            @Override
            protected void write(ByteBuf buf, IntegerArgumentType t)
            {
                boolean hasMin = t.getMinimum() != Integer.MIN_VALUE;
                boolean hasMax = t.getMaximum() != Integer.MAX_VALUE;

                buf.writeByte( binaryFlag( hasMin, hasMax ) );
                if ( hasMin )
                {
                    buf.writeInt( t.getMinimum() );
                }
                if ( hasMax )
                {
                    buf.writeInt( t.getMaximum() );
                }
            }
        };
        private static final ProperArgumentSerializer<StringArgumentType> STRING = new ProperArgumentSerializer<StringArgumentType>()
        {
            @Override
            protected StringArgumentType read(ByteBuf buf)
            {
                int val = readVarInt( buf );
                switch ( val )
                {
                    case 0:
                        return StringArgumentType.word();
                    case 1:
                        return StringArgumentType.string();
                    case 2:
                        return StringArgumentType.greedyString();
                    default:
                        throw new IllegalArgumentException( "Unknown string type " + val );
                }
            }

            @Override
            protected void write(ByteBuf buf, StringArgumentType t)
            {
                writeVarInt( t.getType().ordinal(), buf );
            }

            @Override
            protected String getKey()
            {
                return "brigadier:string";
            }
        };

        static
        {
            PROVIDERS.put( "brigadier:bool", VOID );
            PROVIDERS.put( "brigadier:float", FLOAT );
            PROVIDERS.put( "brigadier:double", DOUBLE );
            PROVIDERS.put( "brigadier:integer", INTEGER );

            PROVIDERS.put( "brigadier:string", STRING );
            PROPER_PROVIDERS.put( StringArgumentType.class, STRING );

            PROVIDERS.put( "minecraft:entity", BYTE );
            PROVIDERS.put( "minecraft:game_profile", VOID );
            PROVIDERS.put( "minecraft:block_pos", VOID );
            PROVIDERS.put( "minecraft:column_pos", VOID );
            PROVIDERS.put( "minecraft:vec3", VOID );
            PROVIDERS.put( "minecraft:vec2", VOID );
            PROVIDERS.put( "minecraft:block_state", VOID );
            PROVIDERS.put( "minecraft:block_predicate", VOID );
            PROVIDERS.put( "minecraft:item_stack", VOID );
            PROVIDERS.put( "minecraft:item_predicate", VOID );
            PROVIDERS.put( "minecraft:color", VOID );
            PROVIDERS.put( "minecraft:component", VOID );
            PROVIDERS.put( "minecraft:message", VOID );
            PROVIDERS.put( "minecraft:nbt_compound_tag", VOID ); // 1.14
            PROVIDERS.put( "minecraft:nbt_tag", VOID ); // 1.14
            PROVIDERS.put( "minecraft:nbt", VOID ); // 1.13
            PROVIDERS.put( "minecraft:nbt_path", VOID );
            PROVIDERS.put( "minecraft:objective", VOID );
            PROVIDERS.put( "minecraft:objective_criteria", VOID );
            PROVIDERS.put( "minecraft:operation", VOID );
            PROVIDERS.put( "minecraft:particle", VOID );
            PROVIDERS.put( "minecraft:rotation", VOID );
            PROVIDERS.put( "minecraft:scoreboard_slot", VOID );
            PROVIDERS.put( "minecraft:score_holder", BYTE );
            PROVIDERS.put( "minecraft:swizzle", VOID );
            PROVIDERS.put( "minecraft:team", VOID );
            PROVIDERS.put( "minecraft:item_slot", VOID );
            PROVIDERS.put( "minecraft:resource_location", VOID );
            PROVIDERS.put( "minecraft:mob_effect", VOID );
            PROVIDERS.put( "minecraft:function", VOID );
            PROVIDERS.put( "minecraft:entity_anchor", VOID );
            PROVIDERS.put( "minecraft:int_range", VOID );
            PROVIDERS.put( "minecraft:float_range", VOID );
            PROVIDERS.put( "minecraft:item_enchantment", VOID );
            PROVIDERS.put( "minecraft:entity_summon", VOID );
            PROVIDERS.put( "minecraft:dimension", VOID );
            PROVIDERS.put( "minecraft:time", VOID ); // 1.14
        }

        private static ArgumentType<?> read(String key, ByteBuf buf)
        {
            ArgumentSerializer reader = PROVIDERS.get( key );
            Preconditions.checkArgument( reader != null, "No provider for argument " + key );

            Object val = reader.read( buf );
            return val != null && PROPER_PROVIDERS.containsKey( val.getClass() ) ? (ArgumentType<?>) val : new DummyType( key, reader, val );
        }

        private static void write(ArgumentType<?> arg, ByteBuf buf)
        {
            ProperArgumentSerializer proper = PROPER_PROVIDERS.get( arg.getClass() );
            if ( proper != null )
            {
                writeString( proper.getKey(), buf );
                proper.write( buf, arg );
            } else
            {
                Preconditions.checkArgument( arg instanceof DummyType, "Non dummy arg " + arg.getClass() );

                DummyType dummy = (DummyType) arg;
                writeString( dummy.key, buf );
                dummy.serializer.write( buf, dummy.value );
            }
        }

        @Data
        private static class DummyType<T> implements ArgumentType<T>
        {

            private final String key;
            private final ArgumentSerializer<T> serializer;
            private final T value;

            @Override
            public T parse(StringReader reader) throws CommandSyntaxException
            {
                throw new UnsupportedOperationException( "Not supported." );
            }
        }

        private abstract static class ArgumentSerializer<T>
        {

            protected abstract T read(ByteBuf buf);

            protected abstract void write(ByteBuf buf, T t);
        }

        private abstract static class ProperArgumentSerializer<T> extends ArgumentSerializer<T>
        {

            protected abstract String getKey();
        }
    }

    @Data
    public static class SuggestionRegistry
    {

        public static final SuggestionProvider ASK_SERVER = new DummyProvider( "minecraft:ask_server" );
        private static final Map<String, SuggestionProvider<DummyProvider>> PROVIDERS = new HashMap<>();

        static
        {
            PROVIDERS.put( "minecraft:ask_server", ASK_SERVER );
            registerDummy( "minecraft:all_recipes" );
            registerDummy( "minecraft:available_sounds" );
            registerDummy( "minecraft:summonable_entities" );
        }

        private static void registerDummy(String name)
        {
            PROVIDERS.put( name, new DummyProvider( name ) );
        }

        private static SuggestionProvider<DummyProvider> getProvider(String key)
        {
            SuggestionProvider<DummyProvider> provider = PROVIDERS.get( key );
            Preconditions.checkArgument( provider != null, "Unknown completion provider " + key );

            return provider;
        }

        private static String getKey(SuggestionProvider<DummyProvider> provider)
        {
            Preconditions.checkArgument( provider instanceof DummyProvider, "Non dummy provider " + provider );

            return ( (DummyProvider) provider ).key;
        }

        @Data
        private static final class DummyProvider implements SuggestionProvider<DummyProvider>
        {

            private final String key;

            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<DummyProvider> context, SuggestionsBuilder builder) throws CommandSyntaxException
            {
                return builder.buildFuture();
            }
        }
    }

    private static byte binaryFlag(boolean first, boolean second)
    {
        byte ret = 0;

        if ( first )
        {
            ret = (byte) ( ret | 0x1 );
        }
        if ( second )
        {
            ret = (byte) ( ret | 0x2 );
        }

        return ret;
    }
}
