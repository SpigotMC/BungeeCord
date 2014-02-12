package net.md_5.bungee.module;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ModuleVersion
{

    private final String build;
    private final String git;

    public static ModuleVersion parse(String version)
    {
        int lastColon = version.lastIndexOf( ':' );
        int secondLastColon = version.lastIndexOf( ':', lastColon - 1 );

        if ( lastColon == -1 || secondLastColon == -1 )
        {
            return null;
        }

        String buildNumber = version.substring( lastColon + 1, version.length() );
        String gitCommit = version.substring( secondLastColon + 1, lastColon ).replaceAll( "\"", "" );

        if ( "unknown".equals( buildNumber ) || "unknown".equals( gitCommit ) )
        {
            return null;
        }

        return new ModuleVersion( buildNumber, gitCommit );
    }
}
