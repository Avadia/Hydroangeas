package net.samagames.hydroangeas.client.panel;

import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.entities.PteroAPI;
import net.samagames.hydroangeas.Hydroangeas;

import java.util.logging.Level;

public class PanelController {
    private PteroAPI adminPanel;
    private PteroAPI userPanel;

    public PanelController(String panelUrl, String adminToken, String userToken) {
        Hydroangeas.getLogger().log(Level.INFO, "Connecting to panel...");
        try {
            this.adminPanel = new PteroBuilder().setApplicationUrl(panelUrl).setToken(adminToken).build();
            this.userPanel = new PteroBuilder().setApplicationUrl(panelUrl).setToken(userToken).build();
            Hydroangeas.getLogger().log(Level.INFO, "Connected to panel.");
        } catch (Exception e) {
            Hydroangeas.getLogger().log(Level.SEVERE, "Can't connect to panel!");
            e.printStackTrace();
        }
    }

    public PteroAPI getAdminPanel() {
        return adminPanel;
    }

    public PteroAPI getUserPanel() {
        return userPanel;
    }
}
