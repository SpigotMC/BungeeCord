package net.md_5.bungee.module;


import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import lombok.Data;
import net.md_5.bungee.Util;


@Data
public class TravisCiModuleSource implements ModuleSource
{
    
    @Override
    public void retrieve(ModuleSpec module, ModuleVersion version)
    {
        System.out.println("Attempting to download Tracis-CI module " + module.getName() + " v" + version.getBuild());
        try
        {
            URL website = new URL("https://github.com/HexagonMC/BungeeCord/releases/download/v" + version.getBuild() + "/" + module.getName() + ".jar");
            URLConnection con = website.openConnection();
            // 15 second timeout at various stages
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            
            Files.write(ByteStreams.toByteArray(con.getInputStream()), module.getFile());
            System.out.println("Download complete");
        }
        catch (IOException ex)
        {
            System.out.println("Failed to download: " + Util.exception(ex));
        }
    }
}
