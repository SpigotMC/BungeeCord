package net.md_5.bungee.conf;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.command.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.util.CaseInsensitiveMap;
import org.junit.jupiter.api.Test;

class ConfigurationConcurrencyTest
{

    private static class DummyServerInfo implements ServerInfo
    {

        private final String name;

        DummyServerInfo( String name )
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public InetSocketAddress getAddress()
        {
            return new InetSocketAddress( "127.0.0.1", 25565 );
        }

        @Override
        public boolean isRestricted()
        {
            return false;
        }

        @Override
        public String getPermission()
        {
            return null;
        }

        @Override
        public boolean canAccess( CommandSender sender )
        {
            return true;
        }

        @Override
        public void sendData( String channel, byte[] data )
        {
        }

        @Override
        public boolean sendData( String channel, byte[] data, boolean queue )
        {
            return false;
        }

        @Override
        public void ping( Callback<ServerPing> callback )
        {
        }
    }

    private Configuration createConfigWithServers() throws Exception
    {
        Configuration config = new Configuration();
        Map<String, ServerInfo> initialServers = new HashMap<>();
        initialServers.put( "lobby", new DummyServerInfo( "lobby" ) );
        initialServers.put( "survival", new DummyServerInfo( "survival" ) );
        Field serversField = Configuration.class.getDeclaredField( "servers" );
        serversField.setAccessible( true );
        serversField.set( config, new CaseInsensitiveMap<>( initialServers ) );
        return config;
    }

    @Test
    void testConcurrentReadWrite() throws InterruptedException
    {
        Configuration config = createConfigWithServers();

        ExecutorService executor = Executors.newFixedThreadPool( 4 );
        CountDownLatch startLatch = new CountDownLatch( 1 );
        CountDownLatch doneLatch = new CountDownLatch( 8 );
        AtomicInteger errors = new AtomicInteger( 0 );

        Runnable reader = () ->
        {
            try
            {
                startLatch.await();
                for ( int i = 0; i < 1000; i++ )
                {
                    Map<String, ServerInfo> copy = config.getServersCopy();
                    assertNotNull( copy );
                    for ( ServerInfo si : copy.values() )
                    {
                        assertNotNull( si.getName() );
                    }
                    config.getServerInfo( "lobby" );
                }
            }
            catch ( Exception ex )
            {
                errors.incrementAndGet();
                ex.printStackTrace();
            }
            finally
            {
                doneLatch.countDown();
            }
        };

        Runnable writer = () ->
        {
            try
            {
                startLatch.await();
                for ( int i = 0; i < 500; i++ )
                {
                    ServerInfo info = new DummyServerInfo( "dynamic-" + i );
                    config.addServer( info );
                    config.getServerInfo( "dynamic-" + i );
                    config.removeServerNamed( "dynamic-" + i );
                }
            }
            catch ( Exception ex )
            {
                errors.incrementAndGet();
                ex.printStackTrace();
            }
            finally
            {
                doneLatch.countDown();
            }
        };

        executor.submit( reader );
        executor.submit( reader );
        executor.submit( writer );
        executor.submit( writer );
        executor.submit( reader );
        executor.submit( reader );
        executor.submit( writer );
        executor.submit( writer );

        startLatch.countDown();
        assertTrue( doneLatch.await( 30, TimeUnit.SECONDS ) );
        assertEquals( 0, errors.get() );
        executor.shutdown();
    }

    @Test
    void testSnapshotIsImmutable() throws Exception
    {
        Configuration config = createConfigWithServers();
        Map<String, ServerInfo> snapshot = config.getServersCopy();
        assertNotNull( snapshot );
        assertEquals( 2, snapshot.size() );

        config.addServer( new DummyServerInfo( "new_server" ) );
        assertEquals( 2, snapshot.size() );
        assertEquals( 3, config.getServersCopy().size() );
    }

    @Test
    void testServerLifecycle() throws Exception
    {
        Configuration config = createConfigWithServers();

        ServerInfo newServer = new DummyServerInfo( "test" );
        assertNull( config.addServer( newServer ) );
        assertEquals( newServer, config.getServerInfo( "test" ) );

        ServerInfo added = config.addServer( new DummyServerInfo( "test" ) );
        assertEquals( newServer.getName(), added.getName() );

        assertTrue( config.removeServersNamed( java.util.Collections.singletonList( "test" ) ) );
        assertNull( config.getServerInfo( "test" ) );

        assertFalse( config.removeServersNamed( java.util.Collections.singletonList( "nonexistent" ) ) );
    }
}