package net.md_5.bungee;

public class ReconnectSaveThread extends Thread {

    public ReconnectSaveThread() {
        super("Location Save Thread");
        setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run() {
        while (BungeeCord.instance.isRunning) {
            try {
                Thread.sleep(5 * 1000 * 60); // 5 minutes
            } catch (InterruptedException ex) {
            }
            BungeeCord.instance.config.saveHosts();
        }
    }
}
