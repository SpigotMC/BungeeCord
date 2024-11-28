package net.md_5.bungee.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BuildVersionRetriever
{
    private static final Gson GSON = new GsonBuilder().create();
    private final Executor executor = Executors.newSingleThreadExecutor();

    private int cachedLatestBuildVersion;
    private Instant lastLatestBuildRetrieve;

    public CompletableFuture<Integer> retrieveLatestBuild()
    {
        // cache result for 5 minutes
        if ( lastLatestBuildRetrieve != null && Instant.now().isBefore( lastLatestBuildRetrieve.plusSeconds( 300 ) ) )
        {
            return CompletableFuture.completedFuture( cachedLatestBuildVersion );
        }

        lastLatestBuildRetrieve = Instant.now();
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        executor.execute( () ->
        {
            try
            {
                URL restApi = new URL( "https://ci.md-5.net/job/BungeeCord/api/json" );
                URLConnection connection = restApi.openConnection();

                connection.setConnectTimeout( 15000 );
                connection.setReadTimeout( 15000 );

                JsonObject jsonObject = GSON.fromJson( new InputStreamReader( connection.getInputStream() ), JsonObject.class );
                jsonObject = (JsonObject) jsonObject.get( "lastSuccessfulBuild" );
                completableFuture.complete( cachedLatestBuildVersion = jsonObject.get( "number" ).getAsInt() );
            } catch ( Exception exception )
            {
                completableFuture.completeExceptionally( exception );
            }
        } );
        return completableFuture;
    }
}
