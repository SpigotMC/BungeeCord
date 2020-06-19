package net.md_5.bungee.api.chat.nbt;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.IntTag;
import se.llbit.nbt.StringTag;

/**
 * Used to show items within the {@link net.md_5.bungee.api.chat.HoverEvent} in the chat api.
 *
 * This creates dummy info and does not require any existing item on the server for use.
 *
 * For reference, see <a href="https://minecraft.gamepedia.com/Player.dat_format#Item_structure">Item structure</a>
 * For reference, see <a href="https://minecraft.gamepedia.com/Tutorials/Command_NBT_tags#Items">Command NBT Tags</a>
 */
@Data
@RequiredArgsConstructor
public class NbtItem
{

    /**
     * Namespaced item ID.
     */
    private final String id;
    /**
     * Tag data for this item.
     */
    private Tag tag;

    @Data
    @RequiredArgsConstructor
    public static class Tag
    {
        public boolean hasDisplay()
        {
            return display != null;
        }

        public NbtItem.Tag.Display getDisplay()
        {
            if ( display == null )
            {
                display = new Tag.Display();
            }
            return display;
        }

        /**
         * Display information for this item.
         */
        protected Display display;

        @Data
        @ToString
        public static class Display
        {

            protected String name;
            protected List<String> lore;
        }
    }

    public boolean hasTag()
    {
        return tag != null;
    }

    public NbtItem.Tag getTag()
    {
        if ( tag == null )
        {
            tag = new Tag();
        }
        return tag;
    }

    public CompoundTag asTag()
    {
        CompoundTag root = new CompoundTag();

        root.add( "id", new StringTag( getId() ) );
        root.add( "Count", new IntTag( 1 ) ); // any use allowing modification?

        if ( tag != null )
        {
            CompoundTag tag1 = new CompoundTag();
            if ( tag.display != null )
            {
                CompoundTag display = new CompoundTag();
                if ( tag.display.lore != null )
                {
                    display.add( "Name", new StringTag( tag.display.name + "!" ) );
                    /*
                    TODO Test Serialisation Properly
                    display.add( "Lore", new ListTag( 8, Arrays.asList(
                            new StringTag(  "Line1" ),
                            new StringTag( "Line2" )
                    ) ));*/
                }
                tag1.add( "display", display );
            }
            root.add( "tag", tag1 );
        }

        return root;
    }
}
