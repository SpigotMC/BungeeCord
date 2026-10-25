package net.md_5.bungee.nbt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import net.md_5.bungee.nbt.limit.NBTLimiter;
import net.md_5.bungee.nbt.type.CompoundTag;
import org.junit.jupiter.api.Test;

public class PlayerDataTest
{

    @Test
    public void testPlayerData() throws URISyntaxException, IOException
    {
        ClassLoader classLoader = PlayerDataTest.class.getClassLoader();
        File file = new File( Objects.requireNonNull( classLoader.getResource( "playerdata.nbt" ) ).toURI() );
        FileInputStream fileInputStream = new FileInputStream( file );
        DataInputStream dis = new DataInputStream( fileInputStream );
        NamedTag namedTag = Tag.readNamedTag( dis, NBTLimiter.unlimitedSize() );
        assertInstanceOf( CompoundTag.class, namedTag.getTag() );
        assertEquals( namedTag.getName(), "" );
    }
}
