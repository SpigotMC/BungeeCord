package net.md_5.bungee.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.Data;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.util.Util;

@Data
public class BootstrapModuleSource implements ModuleSource
{

    @Override
    public void retrieve(ModuleSpec module, ModuleVersion version)
    {
        ProxyServer.getInstance().getLogger().info( "Attempting to extract module " + module.getName() );

        String modulePath = "META-INF/modules/bungeecord-module-" + module.getName().replace( '_', '-' ) + ".jar";
        boolean success = tryGet( getClass().getClassLoader(), modulePath, module.getFile() );

        if ( !success )
        {
            success = tryGet( getClass().getClassLoader().getParent(), modulePath, module.getFile() );
        }

        if ( !success )
        {
            ProxyServer.getInstance().getLogger().warning( "Failed to retrieve module " + module.getName() + ": not found?" );
        }
    }

    private boolean tryGet(ClassLoader classLoader, String path, File out)
    {
        try ( InputStream is = classLoader.getResourceAsStream( path ) )
        {
            if ( is == null )
            {
                return false;
            }

            Files.copy( is, out.toPath(), StandardCopyOption.REPLACE_EXISTING );
            ProxyServer.getInstance().getLogger().info( "Extraction complete" );

            return true;
        } catch ( IOException ex )
        {
            ProxyServer.getInstance().getLogger().warning( "Failed to extract: " + Util.exception( ex ) );
        }

        return false;
    }
}
