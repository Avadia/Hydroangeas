package net.samagames.hydroangeas.client;

import com.google.gson.JsonElement;
import joptsimple.OptionSet;
import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.client.commands.ClientCommandManager;
import net.samagames.hydroangeas.client.panel.PanelController;
import net.samagames.hydroangeas.client.servers.ServerManager;
import net.samagames.hydroangeas.client.tasks.LifeThread;
import net.samagames.hydroangeas.client.tasks.ServerAliveWatchDog;
import net.samagames.hydroangeas.common.protocol.intranet.ByeFromClientPacket;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
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
public class HydroangeasClient extends Hydroangeas {
    private int maxWeight;
    private RestrictionMode restrictionMode;
    private List<String> whitelist;
    private List<String> blacklist;
    private ClientConnectionManager connectionManager;
    private LifeThread lifeThread;
    private ServerManager serverManager;
    private ServerAliveWatchDog serverAliveWatchDog;
    private PanelController panelController;
    private List<String> ports;

    public HydroangeasClient(OptionSet options) throws IOException {
        super(options);
    }

    @Override
    public void enable() {
        this.log(Level.INFO, "Starting Hydroangeas client...");

        this.loadConfig();

        ports = new ArrayList<>();
        for (int i = 15600; i <= 15699; i++)
            ports.add(i + "");

        panelController = new PanelController(this.configuration.getJsonConfiguration().get("panel-admin-token").getAsString(), this.configuration.getJsonConfiguration().get("panel-user-token").getAsString());

        connectionManager = new ClientConnectionManager(this);

        commandManager = new ClientCommandManager(this);

        this.redisSubscriber.registerReceiver("global@" + getUUID() + "@hydroangeas-client", connectionManager::getPacket);
        this.redisSubscriber.registerReceiver("globalSecurity@hydroangeas-client", connectionManager::getPacket);

        this.serverManager = new ServerManager(this);

        this.lifeThread = new LifeThread(this);
        this.lifeThread.start();

        this.serverAliveWatchDog = new ServerAliveWatchDog(this);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();

        blacklist = new ArrayList<>();
        whitelist = new ArrayList<>();

        this.maxWeight = this.configuration.getJsonConfiguration().get("max-weight").getAsInt();

        try {
            this.restrictionMode = RestrictionMode.valueFrom(configuration.getJsonConfiguration().get("RestrictionMode").getAsString());
            getLogger().info("Server restriction is set to: " + restrictionMode.getMode());
        } catch (Exception e) {
            this.restrictionMode = RestrictionMode.NONE;
            getLogger().warning("Restriction mode not set ! Default: none");
        }

        try {
            for (JsonElement data : configuration.getJsonConfiguration().get("Whitelist").getAsJsonArray()) {
                String templateID = data.getAsString();
                if (templateID != null) {
                    whitelist.add(templateID);
                    getLogger().info("Adding to whitelist: " + templateID);
                }
            }
        } catch (Exception e) {
            getLogger().info("No whitelist load !");
        }

        try {
            for (JsonElement data : configuration.getJsonConfiguration().get("Blacklist").getAsJsonArray()) {
                String templateID = data.getAsString();
                if (templateID != null) {
                    blacklist.add(templateID);
                    getLogger().info("Adding to blacklist: " + templateID);
                }
            }
        } catch (Exception e) {
            getLogger().info("No blacklist load !");
        }

        try {
            if (lifeThread != null) {
                lifeThread.sendData(true);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void disable() {
        connectionManager.sendPacket(new ByeFromClientPacket(getUUID()));
        this.serverManager.stopAll();
        this.serverAliveWatchDog.disable();
    }

    public UUID getClientUUID() {
        return getUUID();
    }

    public int getMaxWeight() {
        return this.maxWeight;
    }

    public int getActualWeight() {
        return serverManager.getWeightOfAllServers();
    }

    public LifeThread getLifeThread() {
        return this.lifeThread;
    }

    public ServerManager getServerManager() {
        return this.serverManager;
    }

    public String getIP() {
        try {
            return getInternalIpv4();
        } catch (IOException e) {
            return "0.0.0.0";
        }
    }

    @SuppressWarnings("rawtypes")
    private String getInternalIpv4() throws IOException {
        NetworkInterface i = NetworkInterface.getByName("eth0");
        for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements(); ) {
            InetAddress addr = (InetAddress) en2.nextElement();
            if (!addr.isLoopbackAddress()) {
                if (addr instanceof Inet4Address) {
                    return addr.getHostAddress();
                }
            }
        }
        InetAddress inet = Inet4Address.getLocalHost();
        return inet == null ? "0.0.0.0" : inet.getHostAddress();
    }

    public ClientConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public RestrictionMode getRestrictionMode() {
        return restrictionMode;
    }

    public void setRestrictionMode(RestrictionMode restrictionMode) {
        this.restrictionMode = restrictionMode;
    }

    public ServerAliveWatchDog getServerAliveWatchDog() {
        return serverAliveWatchDog;
    }

    public PanelController getPanelController() {
        return panelController;
    }

    public List<String> getPorts() {
        return ports;
    }
}
