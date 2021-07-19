package net.md_5.bungee.protocol.packet;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
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
import net.md_5.bungee.protocol.ProtocolConstants;

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
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
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
                ArgumentRegistry.write( argumentNode.getType(), buf, protocolVersion );

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
    public static class ArgumentRegistry
    {

        private static final Map<String, ArgumentSerializer> PROVIDERS = new HashMap<>();
        private static final Map<Class<?>, ProperArgumentSerializer<?>> PROPER_PROVIDERS = new HashMap<>();
        private static final Map<String, ProtocolConversionProvider> CONVERSION_PROVIDERS = new HashMap<>();
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
        private static final ProperArgumentSerializer<BoolArgumentType> BOOLEAN = new ProperArgumentSerializer<BoolArgumentType>()
        {
            @Override
            protected BoolArgumentType read(ByteBuf buf)
            {
                return BoolArgumentType.bool();
            }

            @Override
            protected void write(ByteBuf buf, BoolArgumentType t)
            {
            }

            @Override
            protected String getKey()
            {
                return "brigadier:bool";
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
        private static final ProperArgumentSerializer<FloatArgumentType> FLOAT = new ProperArgumentSerializer<FloatArgumentType>()
        {
            @Override
            protected FloatArgumentType read(ByteBuf buf)
            {
                byte flags = buf.readByte();
                float min = ( flags & 0x1 ) != 0 ? buf.readFloat() : -Float.MAX_VALUE;
                float max = ( flags & 0x2 ) != 0 ? buf.readFloat() : Float.MAX_VALUE;

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

            @Override
            protected String getKey()
            {
                return "brigadier:float";
            }
        };
        private static final ProperArgumentSerializer<DoubleArgumentType> DOUBLE = new ProperArgumentSerializer<DoubleArgumentType>()
        {
            @Override
            protected DoubleArgumentType read(ByteBuf buf)
            {
                byte flags = buf.readByte();
                double min = ( flags & 0x1 ) != 0 ? buf.readDouble() : -Double.MAX_VALUE;
                double max = ( flags & 0x2 ) != 0 ? buf.readDouble() : Double.MAX_VALUE;

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

            @Override
            protected String getKey()
            {
                return "brigadier:double";
            }
        };
        private static final ProperArgumentSerializer<IntegerArgumentType> INTEGER = new ProperArgumentSerializer<IntegerArgumentType>()
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

            @Override
            protected String getKey()
            {
                return "brigadier:integer";
            }
        };
        private static final ProperArgumentSerializer<LongArgumentType> LONG = new ProperArgumentSerializer<LongArgumentType>()
        {
            @Override
            protected LongArgumentType read(ByteBuf buf)
            {
                byte flags = buf.readByte();
                long min = ( flags & 0x1 ) != 0 ? buf.readLong() : Long.MIN_VALUE;
                long max = ( flags & 0x2 ) != 0 ? buf.readLong() : Long.MAX_VALUE;

                return LongArgumentType.longArg( min, max );
            }

            @Override
            protected void write(ByteBuf buf, LongArgumentType t)
            {
                boolean hasMin = t.getMinimum() != Long.MIN_VALUE;
                boolean hasMax = t.getMaximum() != Long.MAX_VALUE;

                buf.writeByte( binaryFlag( hasMin, hasMax ) );
                if ( hasMin )
                {
                    buf.writeLong( t.getMinimum() );
                }
                if ( hasMax )
                {
                    buf.writeLong( t.getMaximum() );
                }
            }

            @Override
            protected String getKey()
            {
                return "brigadier:long";
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
            PROVIDERS.put( "brigadier:bool", BOOLEAN );
            PROPER_PROVIDERS.put( BoolArgumentType.class, BOOLEAN );

            PROVIDERS.put( "brigadier:float", FLOAT );
            PROPER_PROVIDERS.put( FloatArgumentType.class, FLOAT );

            PROVIDERS.put( "brigadier:double", DOUBLE );
            PROPER_PROVIDERS.put( DoubleArgumentType.class, DOUBLE );

            PROVIDERS.put( "brigadier:integer", INTEGER );
            PROPER_PROVIDERS.put( IntegerArgumentType.class, INTEGER );

            PROVIDERS.put( "brigadier:long", LONG ); // 1.14+
            PROPER_PROVIDERS.put( LongArgumentType.class, LONG );
            CONVERSION_PROVIDERS.put( "brigadier:long", ( originalType, protocolVersion ) ->
            {
                if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
                {
                    LongArgumentType type = (LongArgumentType) originalType;
                    int min = (int) Math.max( Integer.MIN_VALUE, Math.min( Integer.MAX_VALUE, type.getMinimum() ) );
                    int max = (int) Math.max( Integer.MIN_VALUE, Math.min( Integer.MAX_VALUE, type.getMaximum() ) );
                    return IntegerArgumentType.integer( min, max );
                }
                return originalType;
            } );

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

            PROVIDERS.put( "minecraft:nbt_compound_tag", VOID ); // 1.14+, replaces minecraft:nbt
            CONVERSION_PROVIDERS.put( "minecraft:nbt_compound_tag", ( originalType, protocolVersion ) ->
            {
                if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
                {
                    return minecraftNBT();
                }
                return originalType;
            } );

            PROVIDERS.put( "minecraft:nbt_tag", VOID ); // 1.14+
            CONVERSION_PROVIDERS.put( "minecraft:nbt_tag", ( originalType, protocolVersion ) ->
            {
                if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
                {
                    return minecraftNBT();
                }
                return originalType;
            } );

            PROVIDERS.put( "minecraft:nbt", VOID ); // 1.13
            CONVERSION_PROVIDERS.put( "minecraft:nbt", ( originalType, protocolVersion ) ->
            {
                if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_14 )
                {
                    return minecraftNBTCompoundTag();
                }
                return originalType;
            } );

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

            PROVIDERS.put( "minecraft:time", VOID ); // 1.14+
            CONVERSION_PROVIDERS.put( "minecraft:time", ( originalType, protocolVersion ) ->
            {
                if ( protocolVersion < ProtocolConstants.MINECRAFT_1_14 )
                {
                    return StringArgumentType.word();
                }
                return originalType;
            } );

            PROVIDERS.put( "minecraft:uuid", VOID ); // 1.16+
            CONVERSION_PROVIDERS.put( "minecraft:uuid", ( originalType, protocolVersion ) ->
            {
                if ( protocolVersion < ProtocolConstants.MINECRAFT_1_16 )
                {
                    return StringArgumentType.word();
                }
                return originalType;
            } );

            PROVIDERS.put( "minecraft:test_argument", VOID ); // 1.16+, debug
            PROVIDERS.put( "minecraft:test_class", VOID ); // 1.16+, debug

            PROVIDERS.put( "minecraft:angle", VOID ); // 1.16.2+
            CONVERSION_PROVIDERS.put( "minecraft:angle", ( originalType, protocolVersion ) ->
            {
                if ( protocolVersion < ProtocolConstants.MINECRAFT_1_16_2 )
                {
                    return FloatArgumentType.floatArg();
                }
                return originalType;
            } );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:entity}.
         * @param singleEntity if the argument restrict to only one entity
         * @param onlyPlayers if the argument restrict to players only
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftEntity(boolean singleEntity, boolean onlyPlayers)
        {
            byte flags = 0;
            if ( singleEntity )
            {
                flags |= 1;
            }
            if ( onlyPlayers )
            {
                flags |= 2;
            }

            return minecraftArgumentType( "minecraft:entity", flags );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:game_profile}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftGameProfile()
        {
            return minecraftArgumentType( "minecraft:game_profile", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:block_pos}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftBlockPos()
        {
            return minecraftArgumentType( "minecraft:block_pos", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:column_pos}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftColumnPos()
        {
            return minecraftArgumentType( "minecraft:column_pos", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:vec3}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftVec3()
        {
            return minecraftArgumentType( "minecraft:vec3", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:vec2}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftVec2()
        {
            return minecraftArgumentType( "minecraft:vec2", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:block_state}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftBlockState()
        {
            return minecraftArgumentType( "minecraft:block_state", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:block_predicate}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftBlockPredicate()
        {
            return minecraftArgumentType( "minecraft:block_predicate", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:item_stack}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftItemStack()
        {
            return minecraftArgumentType( "minecraft:item_stack", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:item_predicate}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftItemPredicate()
        {
            return minecraftArgumentType( "minecraft:item_predicate", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:color}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftColor()
        {
            return minecraftArgumentType( "minecraft:color", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:component}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftComponent()
        {
            return minecraftArgumentType( "minecraft:component", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:message}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftMessage()
        {
            return minecraftArgumentType( "minecraft:message", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:nbt_compound_tag}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftNBTCompoundTag()
        {
            return minecraftArgumentType( "minecraft:nbt_compound_tag", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:nbt_tag}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftNBTTag()
        {
            return minecraftArgumentType( "minecraft:nbt_tag", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:nbt}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftNBT()
        {
            return minecraftArgumentType( "minecraft:nbt", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:nbt_path}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftNBTPath()
        {
            return minecraftArgumentType( "minecraft:nbt_path", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:objective}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftObjective()
        {
            return minecraftArgumentType( "minecraft:objective", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:objective_criteria}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftObjectiveCriteria()
        {
            return minecraftArgumentType( "minecraft:objective_criteria", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:operation}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftOperation()
        {
            return minecraftArgumentType( "minecraft:operation", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:particle}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftParticle()
        {
            return minecraftArgumentType( "minecraft:particle", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:rotation}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftRotation()
        {
            return minecraftArgumentType( "minecraft:rotation", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:scoreboard_slot}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftScoreboardSlot()
        {
            return minecraftArgumentType( "minecraft:scoreboard_slot", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:score_holder}.
         * @param allowMultiple if the argument allows multiple entities
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftScoreHolder(boolean allowMultiple)
        {
            byte flags = 0;
            if ( allowMultiple )
            {
                flags |= 1;
            }

            return minecraftArgumentType( "minecraft:score_holder", flags );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:swizzle}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftSwizzle()
        {
            return minecraftArgumentType( "minecraft:swizzle", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:team}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftTeam()
        {
            return minecraftArgumentType( "minecraft:team", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:item_slot}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftItemSlot()
        {
            return minecraftArgumentType( "minecraft:item_slot", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:resource_location}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftResourceLocation()
        {
            return minecraftArgumentType( "minecraft:resource_location", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:mob_effect}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftMobEffect()
        {
            return minecraftArgumentType( "minecraft:mob_effect", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:function}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftFunction()
        {
            return minecraftArgumentType( "minecraft:function", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:entity_anchor}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftEntityAnchor()
        {
            return minecraftArgumentType( "minecraft:entity_anchor", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:int_range}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftIntRange()
        {
            return minecraftArgumentType( "minecraft:int_range", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:float_range}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftFloatRange()
        {
            return minecraftArgumentType( "minecraft:float_range", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:item_enchantment}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftItemEnchantment()
        {
            return minecraftArgumentType( "minecraft:item_enchantment", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:entity_summon}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftEntitySummon()
        {
            return minecraftArgumentType( "minecraft:entity_summon", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:dimension}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftDimension()
        {
            return minecraftArgumentType( "minecraft:dimension", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:time}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftTime()
        {
            return minecraftArgumentType( "minecraft:time", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:uuid}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftUUID()
        {
            return minecraftArgumentType( "minecraft:uuid", null );
        }

        /**
         * Returns the Minecraft ArgumentType {@code minecraft:angle}.
         * @return an ArgumentType instance
         */
        public static ArgumentType<?> minecraftAngle()
        {
            return minecraftArgumentType( "minecraft:angle", null );
        }

        private static ArgumentType<?> minecraftArgumentType(String key, Object rawValue)
        {
            ArgumentSerializer reader = PROVIDERS.get( key );
            Preconditions.checkArgument( reader != null, "No provider for argument " + key );

            return new DummyType( key, reader, rawValue );
        }

        private static ArgumentType<?> read(String key, ByteBuf buf)
        {
            ArgumentSerializer reader = PROVIDERS.get( key );
            Preconditions.checkArgument( reader != null, "No provider for argument " + key );

            Object val = reader.read( buf );
            return val != null && PROPER_PROVIDERS.containsKey( val.getClass() ) ? (ArgumentType<?>) val : new DummyType( key, reader, val );
        }

        private static void write(ArgumentType<?> arg, ByteBuf buf, int protocolVersion)
        {
            arg = convertToVersion( arg, protocolVersion );

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

        // Convert the provided argument type to a new one compatible with the provided protocol version
        private static ArgumentType<?> convertToVersion(ArgumentType<?> arg, int protocolVersion)
        {
            ProperArgumentSerializer proper = PROPER_PROVIDERS.get( arg.getClass() );
            String key;
            if ( proper != null )
            {
                key = proper.getKey();
            } else
            {
                Preconditions.checkArgument( arg instanceof DummyType, "Non dummy arg " + arg.getClass() );
                key = ( (DummyType) arg ).key;
            }

            ProtocolConversionProvider converter = CONVERSION_PROVIDERS.get( key );

            if ( converter != null )
            {
                return converter.convert( arg, protocolVersion );
            }
            return arg;
        }

        private interface ProtocolConversionProvider
        {
            public ArgumentType<?> convert(ArgumentType<?> originalType, int protocolVersion);
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
        /**
         * Tells the client to ask suggestions to the server.
         */
        public static final SuggestionProvider ASK_SERVER = new DummyProvider( "minecraft:ask_server" );

        /**
         * Tells the client to suggest all the available recipes. The suggestions are stored client side.
         */
        public static final SuggestionProvider ALL_RECIPES = new DummyProvider( "minecraft:all_recipes" );

        /**
         * Tells the client to suggest all the available sounds. The suggestions are stored client side.
         */
        public static final SuggestionProvider AVAILABLE_SOUNDS = new DummyProvider( "minecraft:available_sounds" );

        /**
         * Tells the client to suggest all the available biomes. The suggestions are stored client side.
         */
        public static final SuggestionProvider AVAILABLE_BIOMES = new DummyProvider( "minecraft:available_biomes" );

        /**
         * Tells the client to suggest all the available entities. The suggestions are stored client side.
         */
        public static final SuggestionProvider SUMMONABLE_ENTITIES = new DummyProvider( "minecraft:summonable_entities" );
        private static final Map<String, SuggestionProvider<DummyProvider>> PROVIDERS = new HashMap<>();

        static
        {
            PROVIDERS.put( "minecraft:ask_server", ASK_SERVER );
            PROVIDERS.put( "minecraft:all_recipes", ALL_RECIPES );
            PROVIDERS.put( "minecraft:available_sounds", AVAILABLE_SOUNDS );
            PROVIDERS.put( "minecraft:available_biomes", AVAILABLE_BIOMES );
            PROVIDERS.put( "minecraft:summonable_entities", SUMMONABLE_ENTITIES );
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
            Preconditions.checkNotNull( provider );
            if ( provider instanceof DummyProvider )
            {
                return ( (DummyProvider) provider ).key;
            }

            return ( (DummyProvider) ASK_SERVER ).key;
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
