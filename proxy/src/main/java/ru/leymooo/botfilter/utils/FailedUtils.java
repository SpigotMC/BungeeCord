package ru.leymooo.botfilter.utils;

import com.google.common.collect.Sets;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.config.Settings;



public class FailedUtils
{

    private static final Path out = new File( "BotFilter", "failed.txt" ).toPath();
    private static final Set<String> writeQueue = Sets.newConcurrentHashSet();

    public static void addIpToQueue(String ip, PacketUtils.KickType reason)
    {
        if ( Settings.IMP.SAVE_FAILED_IPS_TO_FILE && reason != PacketUtils.KickType.COUNTRY )
        {
            writeQueue.add( ip + "|" + reason.name() + "|" + System.currentTimeMillis() );
        }
    }

    public static void flushQueue()
    {
        if ( writeQueue.isEmpty() )
        {
            return;
        }
        try
        {
            List<String> outLines = new ArrayList<>( writeQueue );
            writeQueue.clear();
            Files.write( out, outLines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND );
        } catch ( Exception e )
        {
            BungeeCord.getInstance().getLogger().log( Level.WARNING, "[BotFilter] Could not save failed ips to file", e );
        }
    }
}
