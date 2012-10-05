package net.md_5.bungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static net.md_5.bungee.Logger.$;

public class BungeeCord {

    /**
     * Current software instance.
     */
    public static BungeeCord instance;
    /**
     * Current operation state.
     */
    public volatile boolean isRunning;
    /**
     * Configuration.
     */
    public final Configuration config = new Configuration();
    /**
     * Thread pool.
     */
    public final ExecutorService threadPool = Executors.newCachedThreadPool();
    /**
     * locations.yml save thread.
     */
    private final ReconnectSaveThread saveThread = new ReconnectSaveThread();
    /**
     * Server socket listener.
     */
    private ListenThread listener;
    /**
     * Current version.
     */
    private String version = (getClass().getPackage().getImplementationVersion() == null) ? "unknown" : getClass().getPackage().getImplementationVersion();

    public static void main(String[] args) throws IOException {
        System.out.println(Util.hex(15));
        instance = new BungeeCord();
        $().info("Enabled BungeeCord version " + instance.version);
        instance.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (instance.isRunning) {
            String line = br.readLine();
            if (line != null) {
                if (line.equals("end")) {
                    instance.stop();
                }
            }
        }
    }

    public void start() throws IOException {
        config.load();
        isRunning = true;

        InetSocketAddress addr = Util.getAddr(config.bindHost);
        listener = new ListenThread(addr);
        listener.start();

        saveThread.start();
        $().info("Listening on " + addr);
    }

    public void stop() {
        this.isRunning = false;

        $().info("Closing listen thread");
        try {
            listener.socket.close();
            listener.join();
        } catch (InterruptedException | IOException ex) {
            $().severe("Could not close listen thread");
        }

        $().info("Closing pending connections");
        threadPool.shutdown();

        $().info("Disconnecting " + "x" + " connections");
        // TODO: Kick everyone

        $().info("Saving reconnect locations");
        saveThread.interrupt();
        try {
            saveThread.join();
        } catch (InterruptedException ex) {
        }

        $().info("Thank you and goodbye");
    }

    public int getOnlinePlayers() {
        return 123;
    }

    public void setSocketOptions(Socket socket) throws IOException {
        socket.setSoTimeout(config.timeout);
        socket.setTrafficClass(0x18);
        socket.setTcpNoDelay(true);
    }
}
