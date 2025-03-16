package net.md_5.bungee.nbt;

import net.md_5.bungee.nbt.limit.NbtLimiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class PlayerDataTest
{
    @Test
    public void testPlayerData() throws URISyntaxException, IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File file = new File( Objects.requireNonNull( classLoader.getResource( "playerdata" ) ).toURI() );
        FileInputStream fileInputStream = new FileInputStream( file );
        DataInputStream dis = new DataInputStream( fileInputStream );
        NamedTag namedTag = NamedTag.read( dis, NbtLimiter.unlimitedSize() );
        assertInstanceOf( CompoundTag.class, namedTag.getTag() );
        assertEquals( namedTag.getName(), "" );
    }
}
