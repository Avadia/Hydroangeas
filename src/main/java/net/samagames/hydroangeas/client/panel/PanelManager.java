package net.samagames.hydroangeas.client.panel;

import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.application.entities.Allocation;
import com.mattmalec.pterodactyl4j.application.entities.Node;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.client.HydroangeasClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class PanelManager {
    private final List<Allocation> proxyAllocations = new ArrayList<>();
    private final List<Allocation> serverAllocations = new ArrayList<>();
    public HydroangeasClient instance;
    private PteroApplication adminPanel;
    private PteroClient userPanel;

    public PanelManager(Hydroangeas hydroangeas) {
        this.instance = hydroangeas.getAsClient();

        Hydroangeas.getLogger().log(Level.INFO, "Connecting to panel...");
        try {
            String panelUrl = instance.getConfiguration().getJsonConfiguration().get("panel-url").getAsString();
            this.adminPanel = new PteroBuilder().setApplicationUrl(panelUrl).setToken(instance.getConfiguration().getJsonConfiguration().get("panel-admin-token").getAsString()).buildApplication();
            this.userPanel = new PteroBuilder().setApplicationUrl(panelUrl).setToken(instance.getConfiguration().getJsonConfiguration().get("panel-user-token").getAsString()).buildClient();
            Hydroangeas.getLogger().log(Level.INFO, "Connected to panel.");
        } catch (Exception e) {
            Hydroangeas.getLogger().log(Level.SEVERE, "Can't connect to panel!");
            e.printStackTrace();
        }
        Set<String> registeredIps = instance.getDatabaseConnector().getJedisPool().getResource().smembers("proxys");
        Node node = adminPanel.retrieveNodeById(instance.getConfiguration().getJsonConfiguration().get("panel-node-id").getAsLong()).execute();
        List<Allocation> allocations = adminPanel.retrieveAllocationsByNode(node).execute();
        for (Allocation allocation : allocations) {
            if (!allocation.isAssigned()) {
                if (allocation.getPortLong() > Hydroangeas.getInstance().getConfiguration().getJsonConfiguration().get("proxy-min-port").getAsLong()
                        && allocation.getPortLong() < Hydroangeas.getInstance().getConfiguration().getJsonConfiguration().get("proxy-max-port").getAsLong()) {
                    this.proxyAllocations.add(allocation);
                } else if (allocation.getPortLong() > Hydroangeas.getInstance().getConfiguration().getJsonConfiguration().get("server-min-port").getAsLong()
                        && allocation.getPortLong() < Hydroangeas.getInstance().getConfiguration().getJsonConfiguration().get("server-max-port").getAsLong()) {
                    if (!registeredIps.contains(allocation.getIP()))
                        instance.getDatabaseConnector().getJedisPool().getResource().sadd("proxys", allocation.getIP());
                    this.serverAllocations.add(allocation);
                }
            }
        }
    }

    public PteroApplication getAdminPanel() {
        return adminPanel;
    }

    public PteroClient getUserPanel() {
        return userPanel;
    }

    public List<Allocation> getProxyAllocations() {
        return proxyAllocations;
    }

    public List<Allocation> getServerAllocations() {
        return serverAllocations;
    }
}
