package net.md_5.bungee.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BuildVersionRetriever
{
    private static final Gson GSON = new GsonBuilder().create();

    private final Executor executor = Executors.newSingleThreadExecutor();
    private int cachedLatestBuildVersion;
    private long lastLatestBuildRetrieve;
    private CompletableFuture<Integer> currentRetrievingFuture;

    public synchronized CompletableFuture<Integer> retrieveLatestBuild()
    {
        // return the awaiting retrieving future is there is any right now
        if ( currentRetrievingFuture != null && !currentRetrievingFuture.isDone() )
        {
            return currentRetrievingFuture;
        }

        // cache result for 15 minutes
        if ( System.currentTimeMillis() < lastLatestBuildRetrieve + TimeUnit.MINUTES.toMillis( 15 ) )
        {
            // if retrieving failed this is 0, so we try again.
            if ( cachedLatestBuildVersion == 0 )
            {
                return retrieveLatestBuild0();
            }
            return CompletableFuture.completedFuture( cachedLatestBuildVersion );
        }

        return retrieveLatestBuild0();
    }

    private CompletableFuture<Integer> retrieveLatestBuild0()
    {
        CompletableFuture<Integer> completableFuture = currentRetrievingFuture = new CompletableFuture<>();
        executor.execute( () ->
        {
            lastLatestBuildRetrieve = System.currentTimeMillis();
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
