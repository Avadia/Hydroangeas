package net.samagames.hydroangeas.client.servers;

import com.google.gson.JsonObject;
import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.application.entities.*;
import com.mattmalec.pterodactyl4j.application.managers.ServerAction;
import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.client.HydroangeasClient;
import net.samagames.hydroangeas.common.data.MinecraftServer;
import net.samagames.hydroangeas.common.protocol.intranet.MinecraftServerIssuePacket;
import net.samagames.hydroangeas.common.protocol.intranet.MinecraftServerSyncPacket;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
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
        String tempPort = this.instance.getPorts().remove(0);

        Location location = this.instance.getPanelController().getAdminPanel().asApplication().retrieveLocationById("1").execute();
        Nest nest = this.instance.getPanelController().getAdminPanel().asApplication().retrieveNestById("8").execute();
        Egg egg = this.instance.getPanelController().getAdminPanel().asApplication().retrieveEggById(nest, "18").execute();
        User owner = this.instance.getPanelController().getAdminPanel().asApplication().retrieveUserById("11").execute();
        Map<String, String> variables = new HashMap<>();
        variables.put("PAPER_JARFILE", "paper.jar");
        variables.put("UPDATER_JARFILE", "updater.jar");
        variables.put("BUILD_NUMBER", "latest");
        variables.put("MINECRAFT_VERSION", "1.12.2");
        Set<String> portRange = new HashSet<>();
        portRange.add(tempPort);
        StringBuilder startupCommand = new StringBuilder(egg.getStartupCommand());
        JsonObject startupOptions = this.getStartupOptions().getAsJsonObject();
        for (String pl : startupOptions.get("plugins").getAsString().split(";"))
            startupCommand.append(" pl:").append(pl);
        startupCommand.append(" map:").append(this.map);
        startupCommand.append(" config:").append(startupOptions.get("configs").getAsString());
        startupCommand.append(" database:").append(getServerName())
                .append(":").append(Hydroangeas.getInstance().getConfiguration().redisIp)
                .append(":").append(Hydroangeas.getInstance().getConfiguration().redisPort)
                .append(":").append(Hydroangeas.getInstance().getConfiguration().redisPassword)
                .append(":").append(Hydroangeas.getInstance().getConfiguration().sqlURL)
                .append(":").append(Hydroangeas.getInstance().getConfiguration().sqlUser)
                .append(":").append(Hydroangeas.getInstance().getConfiguration().sqlPassword);
        startupCommand.append(" game:").append(this.templateID)
                .append(":").append(this.map)
                .append(":").append(this.minSlot)
                .append(":").append(this.maxSlot);
        startupCommand.append(" port:").append(tempPort);

        ServerAction createServerAction = this.instance.getPanelController().getAdminPanel().asApplication().createServer();

        try {
            server = createServerAction.setName("Minecraft - " + this.getServerName())
                    .setCPU(800L)
                    .setMemory(startupOptions.get("RAM").getAsLong(), DataType.MB)
                    .setSwap(startupOptions.get("swap").getAsLong(), DataType.MB)
                    .setDescription("Created on " + Instant.now().toString())
                    .setOwner(owner)
                    .setEgg(egg)
                    .setLocations(Collections.singleton(location))
                    .setAllocations(0L)
                    .setDatabases(0L)
                    .setDisk(startupOptions.get("disk").getAsLong(), DataType.MB)
                    .setDockerImage(egg.getDockerImage())
                    .setDedicatedIP(false)
                    .setPortRange(portRange)
                    .startOnCompletion(true)
                    .setEnvironment(variables)
                    .setStartupCommand(startupCommand.toString())
                    .build().execute();
            allocation = new Allocation() {
                @Override
                public String getIP() {
                    return Hydroangeas.getInstance().getConfiguration().getJsonConfiguration().get("node-ip").getAsString();
                }

                @Override
                public String getFullAddress() {
                    return getIP() + ":" + getPort();
                }

                @Override
                public String getAlias() {
                    return null;
                }

                @Override
                public String getPort() {
                    return tempPort;
                }

                @Override
                public boolean isAssigned() {
                    return true;
                }

                @Override
                public long getIdLong() {
                    return -1;
                }

                @Override
                public OffsetDateTime getCreationDate() {
                    return null;
                }

                @Override
                public OffsetDateTime getUpdatedDate() {
                    return null;
                }
            };
            return true;
        } catch (Exception e) {
            Hydroangeas.getLogger().log(Level.SEVERE, "Can't make the server " + getServerName() + "!", e);
            instance.getConnectionManager().sendPacket(new MinecraftServerIssuePacket(this.instance.getClientUUID(), this.getServerName(), MinecraftServerIssuePacket.Type.MAKE));
            this.instance.getPorts().add(tempPort);
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
//        try {
//            if (server != null)
//                allocation = server.retrieveAllocation().execute();
//        } catch (Exception e) {
//            Hydroangeas.getLogger().log(Level.SEVERE, "Can't get allocation of server " + getServerName() + "!", e);
//        }
        //this.instance.log(Level.SEVERE, "Can't start the server " + getServerName() + "!");
        return true;
    }

    public boolean stopServer() {
        if (server != null) {
            try {
                if (allocation == null)
                    allocation = server.retrieveAllocation().execute();
            } catch (Exception e) {
                Hydroangeas.getLogger().log(Level.SEVERE, "Can't reassign port of server " + getServerName() + "!", e);
            }
            try {
                server.getController().delete(false).execute();
            } catch (Exception e) {
                Hydroangeas.getLogger().log(Level.SEVERE, "Can't stop the server " + getServerName() + "! Try force...", e);
                try {
                    server.getController().delete(true).execute();
                } catch (Exception e1) {
                    Hydroangeas.getLogger().log(Level.SEVERE, "Can't force stop the server " + getServerName() + "!", e1);
                    return false;
                }
            }
            if (allocation != null)
                this.instance.getPorts().add(allocation.getPort());
        }
        instance.getServerManager().onServerStop(this);
        return true;
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
