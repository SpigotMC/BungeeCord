package ru.leymooo.botfilter.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javolution.util.FastSet;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.botfilter.config.Settings;

/**
 *
 * @author Leymooo
 */
public class FailedIpUtils
{

    @Getter
    private boolean enabled = Settings.IMP.FAILED_IPS.MODE != 2;

    private final Logger logger = BungeeCord.getInstance().getLogger();

    private FastSet<String> failed = new FastSet<>();

    private Path failedFile;
    private static final Pattern IPADDRESS_PATTERN
            = Pattern.compile( "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])" );

    public FailedIpUtils()
    {
        if ( enabled )
        {
            loadFailedFromFile( failedFile = Paths.get( "BotFilter" + File.separatorChar + "failed-ips.txt" ) );
            logger.log( Level.INFO, "[BotFilter] Загружено {0} айпи из файла.", failed.size() );
        }
    }

    public boolean isFailed(InetAddress address)
    {
        return failed.contains( address.getHostAddress() );
    }

    public void addIp(String ip)
    {
        if ( enabled && !failed.contains( ip ) )
        {
            failed.add( ip );
            try
            {
                Files.write( failedFile, Arrays.asList( ip ), Charset.forName( "UTF-8" ), StandardOpenOption.APPEND, StandardOpenOption.CREATE );
            } catch ( IOException ex )
            {
                logger.log( Level.WARNING, "[BotFilter] Не могу айпи в файл", ex );
            }

        }
    }

    private void loadFailedFromFile(Path file)
    {
        try
        {
            File rfile = file.toFile();
            if ( !rfile.exists() )
            {
                rfile.createNewFile();
            }
            try ( Stream<String> lines = Files.lines( file, StandardCharsets.UTF_8 ) )
            {
                failed.addAll( lines.collect( Collectors.toSet() ) );
            }
        } catch ( IOException ex )
        {
            logger.log( Level.WARNING, "[BotFilter] Не могу загрузить прокси из файлов", ex );
        }
    }

    public void close()
    {
        failed.clear();
    }
}
