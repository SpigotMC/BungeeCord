package ru.leymooo.botfilter.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.protocol.ProtocolConstants;
import se.llbit.nbt.ByteTag;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.DoubleTag;
import se.llbit.nbt.FloatTag;
import se.llbit.nbt.IntTag;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.LongTag;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.StringTag;
import se.llbit.nbt.Tag;

//Автор: CatCoder, https://vk.com/catcoder
//Изменил: BoomEaro
@RequiredArgsConstructor
@Getter
public enum DimensionCreator
{
    OVERWORLD( "minecraft:overworld", 0, false, true, 0.0f,
            "minecraft:infiniburn_overworld", false, true, true,
            "minecraft:overworld", true, 0, 0,
            256, 1.0f, false, false, 0, 256 );

    private final String key;
    private final int id;

    private final boolean piglinSafe;

    private final boolean natural;
    private final float ambientLight;

    private final String infiniburn;
    private final boolean respawnAnchorWorks;
    private final boolean hasSkylight;
    private final boolean bedWorks;
    private final String effects;
    private final boolean hasRaids;
    private final int monster_spawn_light_level;
    private final int monster_spawn_block_light_limit;
    private final int logicalHeight;
    private final float coordinateScale;
    private final boolean ultrawarm;
    private final boolean hasCeiling;

    private final int minY;
    private final int height;
    public Tag getFullCodec(int protocolVersion)
    {
        CompoundTag attributes = encodeAttributes( protocolVersion );

        if ( protocolVersion <= ProtocolConstants.MINECRAFT_1_16_1 )
        {
            CompoundTag dimensions = new CompoundTag();
            dimensions.add( "dimension", new ListTag( Tag.TAG_COMPOUND, Collections.singletonList( attributes ) ) );

            return new NamedTag( "", dimensions );
        }

        CompoundTag dimensionData = new CompoundTag();

        dimensionData.add( "name", new StringTag( key ) );
        dimensionData.add( "id", new IntTag( id ) );
        dimensionData.add( "element", attributes );

        CompoundTag dimensions = new CompoundTag();
        dimensions.add( "type", new StringTag( "minecraft:dimension_type" ) );
        dimensions.add( "value", new ListTag( Tag.TAG_COMPOUND, Collections.singletonList( dimensionData ) ) );

        CompoundTag root = new CompoundTag();
        root.add( "minecraft:dimension_type", dimensions );
        root.add( "minecraft:worldgen/biome", createBiomeRegistry() );

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            root.add( "minecraft:chat_type", createChatRegistry() );
        }

        return new NamedTag( "", root );
    }
    public Tag getAttributes(int protocolVersion)
    {
        return new NamedTag( "", encodeAttributes( protocolVersion ) );
    }

    private CompoundTag encodeAttributes(int protocolVersion)
    {
        Map<String, SpecificTag> attributes = new HashMap<>();

        // 1.16 - 1.16.1
        attributes.put( "name", new StringTag( key ) );
        //
        attributes.put( "natural", new ByteTag( natural ? 1 : 0 ) );
        attributes.put( "has_skylight", new ByteTag( hasSkylight ? 1 : 0 ) );
        attributes.put( "has_ceiling", new ByteTag( hasCeiling ? 1 : 0 ) );
        // 1.16 - 1.16.1
        attributes.put( "fixed_time", new LongTag( 10_000 ) );
        attributes.put( "shrunk", new ByteTag( 0 ) );
        //
        attributes.put( "ambient_light", new FloatTag( ambientLight ) );
        attributes.put( "ultrawarm", new ByteTag( ultrawarm ? 1 : 0 ) );
        attributes.put( "has_raids", new ByteTag( hasRaids ? 1 : 0 ) );
        attributes.put( "respawn_anchor_works", new ByteTag( respawnAnchorWorks ? 1 : 0 ) );
        attributes.put( "bed_works", new ByteTag( bedWorks ? 1 : 0 ) );
        attributes.put( "piglin_safe", new ByteTag( piglinSafe ? 1 : 0 ) );
        attributes.put( "infiniburn", new StringTag( infiniburn ) );
        attributes.put( "logical_height", new ByteTag( logicalHeight ) );

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2 )
        {
            attributes.remove( "name" ); // removed
            attributes.remove( "fixed_time" ); // removed
            attributes.remove( "shrunk" ); // removed

            attributes.put( "effects", new StringTag( effects ) ); // added
            attributes.put( "coordinate_scale", new FloatTag( coordinateScale ) ); // added
        }

        attributes.put( "height", new IntTag( height ) );
        attributes.put( "min_y", new IntTag( minY ) );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_18_2 )
        {
            attributes.put( "infiniburn", new StringTag( "#" + infiniburn ) ); // added
        }

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            attributes.put( "monster_spawn_light_level", new IntTag( monster_spawn_light_level ) );
            attributes.put( "monster_spawn_block_light_limit", new IntTag( monster_spawn_block_light_limit ) );
        }
        CompoundTag tag = new CompoundTag();

        for ( Map.Entry<String, SpecificTag> entry : attributes.entrySet() )
        {
            tag.add( entry.getKey(), entry.getValue() );
        }

        return tag;
    }

    private CompoundTag createBiomeRegistry()
    {
        CompoundTag root = new CompoundTag();
        root.add( "type", new StringTag( "minecraft:worldgen/biome" ) );
        root.add( "value", new ListTag( Tag.TAG_COMPOUND, Arrays.asList( encodeBiome( Biome.PLAINTS ), encodeBiome( Biome.SWAMP ), encodeBiome( Biome.SWAMP_HILLS ) ) ) );
        return root;
    }

    private CompoundTag createChatRegistry()
    {
        CompoundTag root = new CompoundTag();
        root.add( "type", new StringTag( "minecraft:chat_type" ) );
        CompoundTag systemChat = new CompoundTag();
        systemChat.add( "name", new StringTag( "minecraft:system" ) );
        systemChat.add( "id", new IntTag( 1 ) );
        CompoundTag element = new CompoundTag();
        element.add( "chat", new CompoundTag() );
        CompoundTag narration = new CompoundTag();
        narration.add( "priority", new StringTag( "system" ) );
        element.add( "narration", narration );
        systemChat.add( "element", element );
        root.add( "value", new ListTag( Tag.TAG_COMPOUND, Arrays.asList( systemChat ) ) );
        return root;
    }

    private CompoundTag encodeBiome(Biome biome)
    {
        CompoundTag biomeTag = new CompoundTag();

        biomeTag.add( "name", new StringTag( biome.name ) );
        biomeTag.add( "id", new IntTag( biome.id ) );

        CompoundTag element = new CompoundTag();
        element.add( "precipitation", new StringTag( biome.precipitation ) );
        element.add( "depth", new FloatTag( biome.depth ) );
        element.add( "temperature", new FloatTag( biome.temperature ) );
        element.add( "scale", new FloatTag( biome.scale ) );
        element.add( "downfall", new FloatTag( biome.downfall ) );
        element.add( "category", new StringTag( biome.category ) );

        CompoundTag effects = new CompoundTag();
        effects.add( "sky_color", new IntTag( biome.sky_color ) );
        effects.add( "water_fog_color", new IntTag( biome.water_fog_color ) );
        effects.add( "fog_color", new IntTag( biome.fog_color ) );
        effects.add( "water_color", new IntTag( biome.water_color ) );
        if ( biome.grass_color_modiefer != null )
        {
            effects.add( "grass_color_modifier", new StringTag( biome.grass_color_modiefer ) );
        }
        if ( biome.foliage_color != Integer.MIN_VALUE )
        {
            effects.add( "foliage_color", new IntTag( biome.foliage_color ) );
        }

        CompoundTag moodSound = new CompoundTag();
        moodSound.add( "tick_delay", new IntTag( biome.tick_delay ) );
        moodSound.add( "offset", new DoubleTag( biome.offset ) );
        moodSound.add( "block_search_extent", new IntTag( biome.block_search_extent ) );
        moodSound.add( "sound", new StringTag( biome.sound ) );

        effects.add( "mood_sound", moodSound );

        element.add( "effects", effects );
        biomeTag.add( "element", element );
        return biomeTag;
    }

    public static class Biome
    {

        public static Biome PLAINTS = new Biome( "minecraft:plains", 1, "rain", 0.125f, 0.8f, 0.05f, 0.4f, "plains", 7907327, 329011,
                12638463, 4159204, 6000, 2.0d, 8, "minecraft:ambient.cave", null, Integer.MIN_VALUE );

        public static Biome SWAMP = new Biome( "minecraft:swamp", 6, "rain", -0.2f, 0.8f, 0.1f, 0.9f, "swamp", 7907327, 2302743,
                12638463, 6388580, 6000, 2.0d, 8, "minecraft:ambient.cave", "swamp", 6975545 );

        public static Biome SWAMP_HILLS = new Biome( "minecraft:swamp_hills", 134, "rain", -0.1f, 0.8f, 0.3f, 0.9f, "swamp", 7907327, 2302743,
                12638463, 6388580, 6000, 2.0d, 8, "minecraft:ambient.cave", "swamp", 6975545 );

        public final String name;
        public final int id;
        //elements
        public final String precipitation;
        public final float depth;
        public final float temperature;
        public final float scale;
        public final float downfall;
        public final String category;
        //effects
        public final int sky_color;
        public final int water_fog_color;
        public final int fog_color;
        public final int water_color;
        //mood sound
        public final int tick_delay;
        public final double offset;
        public final int block_search_extent;
        public final String sound;
        public final String grass_color_modiefer;
        public final int foliage_color;

        public Biome(String name, int id, String precipitation, float depth, float temperature,
                     float scale, float downfall, String category, int sky_color, int water_fog_color,
                     int fog_color,
                     int water_color, int tick_delay, double offset, int block_search_extent, String sound,
                     String grass_color_modifier, int foliage_color)
        {
            this.name = name;
            this.id = id;
            this.precipitation = precipitation;
            this.depth = depth;
            this.temperature = temperature;
            this.scale = scale;
            this.downfall = downfall;
            this.category = category;
            this.sky_color = sky_color;
            this.water_fog_color = water_fog_color;
            this.fog_color = fog_color;
            this.water_color = water_color;
            this.tick_delay = tick_delay;
            this.offset = offset;
            this.block_search_extent = block_search_extent;
            this.sound = sound;
            this.grass_color_modiefer = grass_color_modifier;
            this.foliage_color = foliage_color;
        }
    }
}
