package net.samagames.hydroangeas.client.servers;

import net.samagames.hydroangeas.client.HydroangeasClient;
import net.samagames.hydroangeas.client.packets.HelloClientPacket;
import net.samagames.hydroangeas.client.packets.MinecraftServerEndPacket;
import net.samagames.hydroangeas.client.schedulers.ServerCheckerThread;
import net.samagames.hydroangeas.common.informations.MinecraftServerInfos;
import net.samagames.hydroangeas.common.protocol.MinecraftServerIssuePacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ServerManager
{
    private final HydroangeasClient instance;
    private final HashMap<UUID, MinecraftServer> servers;
    private final HashMap<UUID, ScheduledFuture> scheduledFuture;
    private final ArrayList<Integer> portsUsed;
    private final ScheduledExecutorService scheduler;

    public ServerManager(HydroangeasClient instance)
    {
        this.instance = instance;
        this.servers = new HashMap<>();
        this.scheduledFuture = new HashMap<>();
        this.portsUsed = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(12);
    }

    public void newServer(MinecraftServerInfos serverInfos)
    {
        int port = instance.getServerManager().getAvailablePort();
        MinecraftServer server = new MinecraftServer(this.instance, serverInfos, port);

        if(!server.makeServer())
        {
            new MinecraftServerIssuePacket(this.instance.getClientUUID(), serverInfos, MinecraftServerIssuePacket.Type.MAKE).send();
            return;
        }

        if(!server.startServer())
        {
            new MinecraftServerIssuePacket(this.instance.getClientUUID(), serverInfos, MinecraftServerIssuePacket.Type.START).send();
            return;
        }

        this.servers.put(serverInfos.getUUID(), server);
        this.portsUsed.add(port);
        this.scheduledFuture.put(serverInfos.getUUID(), this.scheduler.scheduleAtFixedRate(new ServerCheckerThread(this.instance, server), 10, 10, TimeUnit.SECONDS));

        this.instance.log(Level.INFO, "New server started -> Game (" + serverInfos.getGame() + ") & Map (" + serverInfos.getMap() + ")");

        new HelloClientPacket(this.instance).send();
    }

    public void stopAll()
    {
        for(MinecraftServer server : this.servers.values())
        {
            if(!server.stopServer())
            {
                new MinecraftServerIssuePacket(this.instance, server.getServerInfos(), MinecraftServerIssuePacket.Type.STOP).send();
                return;
            }
        }
    }

    public void onServerStop(MinecraftServer server)
    {
        this.scheduledFuture.get(server.getServerInfos().getUUID()).cancel(true);
        this.scheduledFuture.remove(server.getServerInfos().getUUID());
        this.servers.remove(server.getServerInfos().getUUID());
        this.portsUsed.remove(this.portsUsed.indexOf(server.getPort()));

        new HelloClientPacket(this.instance).send();
        new MinecraftServerEndPacket(server).send();
    }

    public int getAvailablePort()
    {
        String portString = "20";

        for(int i = 0; i < 3; i++)
            portString += new Random().nextInt(9);

        int port = Integer.valueOf(portString);

        if(this.portsUsed.contains(port))
            return this.getAvailablePort();

        this.portsUsed.add(port);

        return port;
    }

    public MinecraftServer getServerByUUID(UUID uuid)
    {
        if(this.servers.containsKey(uuid))
            return this.servers.get(uuid);
        else
            return null;
    }

    public HashMap<UUID, MinecraftServer> getServers()
    {
        return this.servers;
    }
}
