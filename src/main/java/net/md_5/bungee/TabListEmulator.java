package net.md_5.bungee;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.PacketC9PlayerListItem;

public class TabListEmulator extends Thread {
    //Should we make these configurable?
    public static final int PING_THRESHOLD = 10;
    public static final int UPDATE_INTERVAL = 1000;
    
    private Map<String, UserConnection> connections;
    private Map<UserConnection, Integer> aliveConnections = new LinkedHashMap<UserConnection, Integer>();
    private boolean stop;
    
    public TabListEmulator(Map<String, UserConnection> connections)
    {
        this.connections = connections;
    }
	
	@Override
    public void run()
	{
        while(!stop) {
            Map<UserConnection, Integer> alive = new LinkedHashMap<UserConnection, Integer>();
            Collection<UserConnection> cons = connections.values();
            for(UserConnection con : cons)
            {
                Integer lastPing = aliveConnections.remove(con);
            	int ping = con.getPing();
            	if(lastPing != null && ping-PING_THRESHOLD <= lastPing && ping+PING_THRESHOLD >= lastPing) {
            		alive.put(con, lastPing);
            		continue;
            	}
            	broadcast(new PacketC9PlayerListItem(con.username, true, ping), cons);
            	alive.put(con, ping);
            }
            
            for(UserConnection con : aliveConnections.keySet())
            {
            	broadcast(new PacketC9PlayerListItem(con.username, false, 0), cons);
            }
            
            aliveConnections = alive;
            
            try
            {
            	Thread.sleep(UPDATE_INTERVAL);
            }
            catch (InterruptedException e) {}
        }
	}
	
    private void broadcast(DefinedPacket packet, Collection<UserConnection> cons)
    {
        for(UserConnection con2 : cons)
        {
            con2.packetQueue.add(packet);
        }
    }

    public void safeStop()
    {
        interrupt();
        this.stop = true;
    }
}
