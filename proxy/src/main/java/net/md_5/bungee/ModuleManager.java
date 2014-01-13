package net.md_5.bungee;

import java.io.File;
import java.util.List;
import net.md_5.bungee.api.ProxyServer;

public class ModuleManager
{

    private class ModuleSpec
    {
    }

    public void load(ProxyServer proxy) throws Exception
    {
        String version = proxy.getVersion();
        version = "git:BungeeCord-Proxy:1.7-SNAPSHOT:\"93cf50b\":1337";

        int lastColon = version.lastIndexOf( ':' );
        int secondLastColon = version.lastIndexOf( ':', lastColon - 1 );
        String buildNumber = version.substring( lastColon + 1, version.length() );
        String gitCommit = version.substring( secondLastColon + 1, lastColon ).replaceAll( "\"", "" );

        File moduleDirectory = new File( "modules" );
        moduleDirectory.mkdir();

        List<ModuleSpec> modules = null;

        // TODO: Use filename filter here and in PluginManager
        for ( File file : moduleDirectory.listFiles() )
        {
            if ( file.isFile() && file.getName().endsWith( ".jar" ) )
            {

            }
        }
    }
}
