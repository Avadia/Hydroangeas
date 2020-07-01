package net.samagames.hydroangeas.client.panel;

import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.entities.PteroAPI;

public class PanelController {
    private final PteroAPI adminPanel;
    private final PteroAPI userPanel;

    public PanelController(String adminToken, String userToken) {
        this.adminPanel = new PteroBuilder().setApplicationUrl("https://panel.avadia.fr/").setToken(adminToken).build();
        this.userPanel = new PteroBuilder().setApplicationUrl("https://panel.avadia.fr/").setToken(userToken).build();
    }

    public PteroAPI getAdminPanel() {
        return adminPanel;
    }

    public PteroAPI getUserPanel() {
        return userPanel;
    }
}
