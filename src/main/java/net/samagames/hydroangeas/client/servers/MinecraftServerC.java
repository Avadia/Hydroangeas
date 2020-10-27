package net.samagames.hydroangeas.client.servers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.PowerAction;
import com.mattmalec.pterodactyl4j.application.entities.*;
import com.mattmalec.pterodactyl4j.application.managers.ServerAction;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.client.HydroangeasClient;
import net.samagames.hydroangeas.common.data.MinecraftServer;
import net.samagames.hydroangeas.common.protocol.intranet.MinecraftServerIssuePacket;
import net.samagames.hydroangeas.common.protocol.intranet.MinecraftServerSyncPacket;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static net.samagames.hydroangeas.Hydroangeas.getLogger;

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
public class MinecraftServerC extends MinecraftServer {
    private final HydroangeasClient instance;

    private ApplicationServer server;
    private Allocation allocation;

    public MinecraftServerC(HydroangeasClient instance,
                            MinecraftServerSyncPacket serverInfos) {
        super(serverInfos.getMinecraftUUID(),
                serverInfos.getGame(),
                serverInfos.getMap(),
                serverInfos.getMinSlot(),
                serverInfos.getMaxSlot(),
                serverInfos.getOptions(),
                serverInfos.getStartupOptions()
        );
        this.instance = instance;

        this.coupaingServer = serverInfos.isCoupaingServer();

        this.hubID = serverInfos.getHubID();

        this.templateID = serverInfos.getTemplateID();

        this.timeToLive = serverInfos.getTimeToLive();

        this.weight = serverInfos.getWeight();
    }

    public boolean makeServer() {
        if (this.instance.getPanelManager().getServerAllocations().isEmpty()) {
            Hydroangeas.getLogger().log(Level.SEVERE, "Can't make the server " + getServerName() + "! No allocation available!");
            instance.getConnectionManager().sendPacket(new MinecraftServerIssuePacket(this.instance.getClientUUID(), this.getServerName(), MinecraftServerIssuePacket.Type.MAKE));
            return false;
        }
        allocation = this.instance.getPanelManager().getServerAllocations().remove(0);

        Location location = this.instance.getPanelManager().getAdminPanel().retrieveLocationById("1").execute();
        Nest nest = this.instance.getPanelManager().getAdminPanel().retrieveNestById("5").execute();
        Egg egg = this.instance.getPanelManager().getAdminPanel().retrieveEggById(nest, "15").execute();
        User owner = this.instance.getPanelManager().getAdminPanel().retrieveUserById("8").execute();
        Map<String, String> variables = new HashMap<>();
        variables.put("PRODUCTION", Hydroangeas.production + "");
        Set<String> portRange = new HashSet<>();
        portRange.add(allocation.getPort());
        StringBuilder startupCommand = new StringBuilder(egg.getStartupCommand());
        startupCommand.append(" auth:").append(Hydroangeas.getInstance().getConfiguration().getJsonConfiguration().get("nexus-user").getAsString())
                .append("¤").append(Hydroangeas.getInstance().getConfiguration().getJsonConfiguration().get("nexus-password").getAsString());
        JsonObject startupOptions = this.getStartupOptions().getAsJsonObject();
        for (String plugin : startupOptions.get("plugins").getAsString().split(";"))
            startupCommand.append(" plugin:").append(plugin.replaceAll(":", "¤"));
        startupCommand.append(" config:").append(startupOptions.get("configs").getAsString());
        startupCommand.append(" data:").append(Hydroangeas.getInstance().getConfiguration().redisIp)
                .append("¤").append(Hydroangeas.getInstance().getConfiguration().redisPort)
                .append("¤").append(Hydroangeas.getInstance().getConfiguration().redisPassword)
                .append("¤").append(Hydroangeas.getInstance().getConfiguration().sqlIp)
                .append("¤").append(Hydroangeas.getInstance().getConfiguration().sqlPort)
                .append("¤").append(Hydroangeas.getInstance().getConfiguration().sqlName)
                .append("¤").append(Hydroangeas.getInstance().getConfiguration().sqlUser)
                .append("¤").append(Hydroangeas.getInstance().getConfiguration().sqlPassword);
        startupCommand.append(" game:").append(this.templateID)
                .append("¤").append(this.map)
                .append("¤").append(this.minSlot)
                .append("¤").append(this.maxSlot);
        JsonObject options = this.getOptions().getAsJsonObject();
        for (Map.Entry<String, JsonElement> option : options.entrySet())
            startupCommand.append("¤").append(option.getKey()).append("-=>").append(option.getValue().toString());
        startupCommand.append(" port:").append(allocation.getPort());
        startupCommand.append(" api:").append(getServerName())
                .append("¤").append(Hydroangeas.getInstance().getConfiguration().getJsonConfiguration().get("slack").getAsString());

        ServerAction createServerAction = this.instance.getPanelManager().getAdminPanel().createServer();
        StringBuilder name = new StringBuilder("Avadia - Minecraft - ");
        if (Hydroangeas.production)
            name.append("PROD - ");
        name.append(this.getServerName());
        createServerAction.setName(name.toString())
                .setCPU(400L)
                .setMemory(startupOptions.get("RAM").getAsLong(), DataType.MB)
                .setSwap(startupOptions.get("swap").getAsLong(), DataType.MB)
                .setIO(600L)
                .setDescription("Created on " + Instant.now().toString())
                .setOwner(owner)
                .setEgg(egg)
                .setLocations(Collections.singleton(location))
                .setAllocations(0L)
                .setDatabases(0L)
                .setBackups(0L)
                .setDisk(startupOptions.get("disk").getAsLong(), DataType.MB)
                .setDockerImage(egg.getDockerImage())
                .setDedicatedIP(false)
                .setPortRange(portRange)
                //TODO After bug is fixed enable this
                .startOnCompletion(false)
                .setEnvironment(variables)
                .setStartupCommand(startupCommand.toString());
        try {
            server = createServerAction.build().execute();
            return true;
        } catch (Exception e) {
            Hydroangeas.getLogger().log(Level.SEVERE, "Can't make the server " + getServerName() + "!", e);
            instance.getConnectionManager().sendPacket(new MinecraftServerIssuePacket(this.instance.getClientUUID(), this.getServerName(), MinecraftServerIssuePacket.Type.MAKE));
            this.instance.getPanelManager().getServerAllocations().add(allocation);
            return false;
        }
    }

    public boolean startServer() {
//                            "-Xmx" + maxRAM,
//                            "-Xms" + startupOptionsObj.get("minRAM").getAsString(),
//                            "-Xmn" + startupOptionsObj.get("edenRAM").getAsString(),
//                            "-XX:+UseG1GC",
//                            "-XX:+UnlockExperimentalVMOptions",
//                            "-XX:MaxGCPauseMillis=50",
//                            "-XX:+DisableExplicitGC",
//                            "-XX:G1HeapRegionSize=4M",
//                            "-XX:TargetSurvivorRatio=90",
//                            "-XX:G1NewSizePercent=50",
//                            "-XX:G1MaxNewSizePercent=80",
//                            "-XX:InitiatingHeapOccupancyPercent=10",
//                            "-XX:G1MixedGCLiveThresholdPercent=50",
//                            "-XX:+AggressiveOpts",
//                            "-XX:+UseLargePagesInMetaspace",
//                            "-Djava.net.preferIPv4Stack=true",
//                            "-Dcom.sun.management.jmxremote",
//                            "-Dcom.sun.management.jmxremote.port=" + (getPort() + 1),
//                            "-Dcom.sun.management.jmxremote.local.only=false",
//                            "-Dcom.sun.management.jmxremote.authenticate=false",
//                            "-Dcom.sun.management.jmxremote.ssl=false",

        getLogger().info("Starting server " + getServerName());
        //TODO After bug is fixed disable this
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!server.getContainer().isInstalled()) {
            server = this.instance.getPanelManager().getAdminPanel().retrieveServerById(server.getId()).execute();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ClientServer clientServer = this.instance.getPanelManager().getUserPanel().retrieveServerByIdentifier(server.getIdentifier()).execute();
        this.instance.getPanelManager().getUserPanel().setPower(clientServer, PowerAction.START).execute();

        Jedis jedis = Hydroangeas.getInstance().getDatabaseConnector().getResource();
        jedis.hset("servers", getServerName(), allocation.getIP() + ":" + allocation.getPort());
        jedis.close();
//        try {
//            if (server != null)
//                allocation = server.retrieveAllocation().execute();
//        } catch (Exception e) {
//            Hydroangeas.getLogger().log(Level.SEVERE, "Can't get allocation of server " + getServerName() + "!", e);
//        }
        //this.instance.log(Level.SEVERE, "Can't start the server " + getServerName() + "!");
        return true;
    }

    public void stopServer() {
        this.stopServer(false);
    }

    public void stopServer(boolean skipPacket) {
        instance.getServerManager().onServerStop(this, skipPacket);
        if (server != null) {
            this.instance.getPanelManager().getUserPanel().setPower(this.instance.getPanelManager().getUserPanel().retrieveServerByIdentifier(server.getIdentifier()).execute(), PowerAction.STOP).execute();
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                server.getController().delete(false).execute();
                if (allocation != null)
                    this.instance.getPanelManager().getServerAllocations().add(allocation);
            } catch (Exception e) {
                Hydroangeas.getLogger().log(Level.SEVERE, "Can't stop the server " + getServerName() + "!", e);
            }
        }
    }

    public HydroangeasClient getInstance() {
        return instance;
    }

    public ApplicationServer getServer() {
        return server;
    }

    public Allocation getAllocation() {
        return allocation;
    }
}
