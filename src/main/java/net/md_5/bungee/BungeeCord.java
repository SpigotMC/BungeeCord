package net.md_5.bungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static net.md_5.bungee.Logger.$;
import net.md_5.bungee.command.Command;
import net.md_5.bungee.command.CommandEnd;
import net.md_5.bungee.command.CommandSender;
import net.md_5.bungee.command.ConsoleCommandSender;

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
    /**
     * Fully qualified connections.
     */
    public Map<String, UserConnection> connections = new ConcurrentHashMap<>();
    /**
     * Registered commands.
     */
    private Map<String, Command> commandMap = new HashMap<>();

    {
        commandMap.put("end", new CommandEnd());
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Util.hex(15));
        instance = new BungeeCord();
        $().info("Enabled BungeeCord version " + instance.version);
        instance.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (instance.isRunning) {
            String line = br.readLine();
            if (line != null) {
                boolean handled = instance.dispatchCommand(line, ConsoleCommandSender.instance);
                if (!handled) {
                    System.err.println("Command not found");
                }
            }
        }
    }

    public boolean dispatchCommand(String commandLine, CommandSender sender) {
        String[] split = commandLine.trim().split(" ");
        String commandName = split[0].toLowerCase();
        if (commandMap.containsKey(commandName)) {
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            Command c = commandMap.get(commandName);
            try {
                c.execute(sender, args);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "An error occurred while executing this command!");
                System.err.println("----------------------- [Start of command error] -----------------------");
                ex.printStackTrace();
                System.err.println("----------------------- [End of command error] -----------------------");
            }
            return true;
        } else {
            return false;
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

        $().info("Disconnecting " + connections.size() + " connections");
        for (UserConnection user : connections.values()) {
            user.disconnect("Proxy restarting, brb.");
        }

        $().info("Saving reconnect locations");
        saveThread.interrupt();
        try {
            saveThread.join();
        } catch (InterruptedException ex) {
        }

        $().info("Thank you and goodbye");
    }

    public void setSocketOptions(Socket socket) throws IOException {
        socket.setSoTimeout(config.timeout);
        socket.setTrafficClass(0x18);
        socket.setTcpNoDelay(true);
    }
}
