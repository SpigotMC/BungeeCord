package net.md_5.bungee.api.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.repository.supplier.RepositorySystemSupplier;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;

class LibraryLoader
{

    private static final String REPOSITORY_PROPERTY = "net.md_5.bungee.api.plugin.centralURL";
    private final Logger logger;
    private final RepositorySystem repository;
    private final RepositorySystemSession session;
    private final List<RemoteRepository> repositories;

    public LibraryLoader(Logger logger)
    {
        this.logger = logger;

        this.repository = new RepositorySystemSupplier().getRepositorySystem();
        RepositorySystemSession.SessionBuilder sessionBuilder = this.repository.createSessionBuilder();

        sessionBuilder.setChecksumPolicy( RepositoryPolicy.CHECKSUM_POLICY_FAIL );
        sessionBuilder.withLocalRepositories( new LocalRepository( new File( "libraries" ).toPath() ) );
        sessionBuilder.setTransferListener( new AbstractTransferListener()
        {
            @Override
            public void transferStarted(TransferEvent event) throws TransferCancelledException
            {
                logger.log( Level.INFO, "Downloading {0}", event.getResource().getRepositoryUrl() + event.getResource().getResourceName() );
            }
        } );

        // SPIGOT-7638: Add system properties,
        // since JdkVersionProfileActivator needs 'java.version' when a profile has the 'jdk' element
        // otherwise it will silently fail and not resolves the dependencies in the affected pom.
        sessionBuilder.setSystemProperties( System.getProperties() );
        this.session = sessionBuilder.build();

        this.repositories = repository.newResolutionRepositories( session, Arrays.asList( new RemoteRepository.Builder( "central", "default", System.getProperty( REPOSITORY_PROPERTY, "https://repo.maven.apache.org/maven2" ) ).build() ) );
    }

    public ClassLoader createLoader(PluginDescription desc)
    {
        if ( desc.getLibraries().isEmpty() )
        {
            return null;
        }
        logger.log( Level.INFO, "[{0}] Loading {1} libraries... please wait", new Object[]
        {
            desc.getName(), desc.getLibraries().size()
        } );

        List<Dependency> dependencies = new ArrayList<>();
        for ( String library : desc.getLibraries() )
        {
            Artifact artifact = new DefaultArtifact( library );
            Dependency dependency = new Dependency( artifact, null );

            dependencies.add( dependency );
        }

        DependencyResult result;
        try
        {
            result = repository.resolveDependencies( session, new DependencyRequest( new CollectRequest( (Dependency) null, dependencies, repositories ), null ) );
        } catch ( DependencyResolutionException ex )
        {
            throw new RuntimeException( "Error resolving libraries", ex );
        }

        List<URL> jarFiles = new ArrayList<>();
        for ( ArtifactResult artifact : result.getArtifactResults() )
        {
            File file = artifact.getArtifact().getFile();

            URL url;
            try
            {
                url = file.toURI().toURL();
            } catch ( MalformedURLException ex )
            {
                throw new AssertionError( ex );
            }

            jarFiles.add( url );
            logger.log( Level.INFO, "[{0}] Loaded library {1}", new Object[]
            {
                desc.getName(), file
            } );
        }

        URLClassLoader loader = new URLClassLoader( jarFiles.toArray( new URL[ 0 ] ) );

        return loader;
    }
}
