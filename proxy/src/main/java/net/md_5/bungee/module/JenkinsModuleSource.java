package net.md_5.bungee.module;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import lombok.Data;
import net.md_5.bungee.Util;

@Data
public class JenkinsModuleSource implements ModuleSource
{

    @Override
    public void retrieve(ModuleSpec module, ModuleVersion version)
    {
        System.out.println( "Attempting to Jenkins download module " + module.getName() + " v" + version.getBuild() );
        try
        {
            URL website = new URL( "http://ci.md-5.net/job/BungeeCord/" + version.getBuild() + "/artifact/module/" + module.getName().replace( '_', '-' ) + "/target/" + module.getName() + ".jar" );
            URLConnection con = website.openConnection();
            // 15 second timeout at various stages
            con.setConnectTimeout( 15000 );
            con.setReadTimeout( 15000 );

            Files.write( ByteStreams.toByteArray( con.getInputStream() ), module.getFile() );
            System.out.println( "Download complete" );
        } catch ( IOException ex )
        {
            System.out.println( "Failed to download: " + Util.exception( ex ) );
        }
    }
}
