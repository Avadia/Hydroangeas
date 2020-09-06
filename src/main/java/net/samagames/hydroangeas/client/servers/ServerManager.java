package net.samagames.hydroangeas.client.servers;

import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.client.HydroangeasClient;
import net.samagames.hydroangeas.common.protocol.intranet.MinecraftServerIssuePacket;
import net.samagames.hydroangeas.common.protocol.intranet.MinecraftServerSyncPacket;
import net.samagames.hydroangeas.common.protocol.intranet.MinecraftServerUpdatePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/*
 * This file is part of Hydroangeas.
 *
 * Hydroangeas is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hydroangeas is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hydroangeas.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ServerManager {
    private final HydroangeasClient instance;
    private final List<MinecraftServerC> servers = new ArrayList<>();

    public ServerManager(HydroangeasClient instance) {
        this.instance = instance;
    }

    public void newServer(MinecraftServerSyncPacket serverInfos) {
        try {
            //Check state of hydro
            checkTemplate(serverInfos.getTemplateID());

            MinecraftServerC server = new MinecraftServerC(this.instance, serverInfos);

            Hydroangeas.getLogger().info("Server creation !");

            this.servers.add(server);

            if (!server.makeServer()) {
                instance.getConnectionManager().sendPacket(new MinecraftServerIssuePacket(this.instance.getClientUUID(), serverInfos.getServerName(), MinecraftServerIssuePacket.Type.MAKE));
                instance.getConnectionManager().sendPacket(new MinecraftServerUpdatePacket(instance, server.getServerName(), MinecraftServerUpdatePacket.UType.END));
                server.stopServer();
                return;
            }
            if (!server.startServer()) {
                instance.getConnectionManager().sendPacket(new MinecraftServerIssuePacket(this.instance.getClientUUID(), serverInfos.getServerName(), MinecraftServerIssuePacket.Type.START));
                instance.getConnectionManager().sendPacket(new MinecraftServerUpdatePacket(instance, server.getServerName(), MinecraftServerUpdatePacket.UType.END));
                server.stopServer();
                return;
            }

            this.instance.log(Level.INFO, "New server started -> Game (" + serverInfos.getGame() + ") & Map (" + serverInfos.getMap() + ")");

            instance.getConnectionManager().sendPacket(new MinecraftServerUpdatePacket(instance, server.getServerName(), MinecraftServerUpdatePacket.UType.START));
            //Complete data of the server
            instance.getConnectionManager().sendPacket(new MinecraftServerSyncPacket(instance, server));
        } catch (Exception e) {
            e.printStackTrace();
            instance.getConnectionManager().sendPacket(new MinecraftServerIssuePacket(this.instance.getClientUUID(), serverInfos.getServerName(), MinecraftServerIssuePacket.Type.MAKE));
            instance.getConnectionManager().sendPacket(new MinecraftServerUpdatePacket(instance, serverInfos.getServerName(), MinecraftServerUpdatePacket.UType.END));
        }
    }

    public void stopAll() {
        ExecutorService service = Executors.newCachedThreadPool();
        for (MinecraftServerC server : servers)
            service.submit(() -> {
                server.stopServer();
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

        service.shutdown();

        while (!service.isTerminated()) {
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException ignored) {

            }
        }
    }

    public void onServerStop(MinecraftServerC server) {
        this.onServerStop(server, false);
    }

    public void onServerStop(MinecraftServerC server, boolean skipPacket) {
        if (!skipPacket)
            instance.getConnectionManager().sendPacket(new MinecraftServerUpdatePacket(instance, server.getServerName(), MinecraftServerUpdatePacket.UType.END));
        this.servers.remove(server);
        Hydroangeas.getLogger().info("Stopped server " + server.getServerName());
    }

    public int getWeightOfAllServers() {
        int w = 0;
        List<MinecraftServerC> servers = new ArrayList<>(this.servers);
        for (MinecraftServerC server : servers) {
            w += server.getWeight();
        }
        return w;
    }

    public MinecraftServerC getServerByName(String name) {
        for (MinecraftServerC server : servers) {
            if (server.getServerName().equals(name)) {
                return server;
            }
        }
        return null;
    }

    public MinecraftServerC getServerByUUID(UUID uuid) {
        for (MinecraftServerC server : servers) {
            if (server.getUUID().equals(uuid)) {
                return server;
            }
        }
        return null;
    }

    public List<MinecraftServerC> getServers() {
        return this.servers;
    }

    public void checkTemplate(String template) throws Exception {
        if (instance.getRestrictionMode().equals(HydroangeasClient.RestrictionMode.NONE))
            return;

        if (instance.getRestrictionMode().equals(HydroangeasClient.RestrictionMode.WHITELIST)) {
            if (!instance.getWhitelist().contains(template)) {
                throw new Exception("Try to start a server with template not whitelisted !");
            }
        }

        if (instance.getRestrictionMode().equals(HydroangeasClient.RestrictionMode.BLACKLIST)) {
            if (instance.getBlacklist().contains(template)) {
                throw new Exception("Try to start a server with a template blacklisted !");
            }
        }
    }
}
