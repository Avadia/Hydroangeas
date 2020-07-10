package net.samagames.hydroangeas.client;

import com.google.gson.JsonElement;
import joptsimple.OptionSet;
import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.client.commands.ClientCommandManager;
import net.samagames.hydroangeas.client.panel.PanelManager;
import net.samagames.hydroangeas.client.servers.ServerManager;
import net.samagames.hydroangeas.client.tasks.LifeThread;
import net.samagames.hydroangeas.common.protocol.intranet.ByeFromClientPacket;

import java.io.IOException;
import java.util.ArrayList;
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
    private PanelManager panelManager;

    public HydroangeasClient(OptionSet options) throws IOException {
        super(options);
    }

    @Override
    public void enable() {
        this.log(Level.INFO, "Starting Hydroangeas client...");

        this.loadConfig();

        panelManager = new PanelManager(this);

        connectionManager = new ClientConnectionManager(this);

        commandManager = new ClientCommandManager(this);

        this.redisSubscriber.registerReceiver("global@" + getUUID() + "@hydroangeas-client", connectionManager::getPacket);
        this.redisSubscriber.registerReceiver("globalSecurity@hydroangeas-client", connectionManager::getPacket);

        this.serverManager = new ServerManager(this);

        this.lifeThread = new LifeThread(this);
        this.lifeThread.start();
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
        this.serverManager.stopAll();
        connectionManager.sendPacket(new ByeFromClientPacket(getUUID()));
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

    public PanelManager getPanelManager() {
        return panelManager;
    }
}
