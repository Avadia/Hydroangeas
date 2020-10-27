package com.mattmalec.pterodactyl4j;

import com.mattmalec.pterodactyl4j.entities.PteroAPI;
import com.mattmalec.pterodactyl4j.entities.impl.PteroAPIImpl;

public class PteroBuilder {

    private String token;
    private String applicationUrl;

    public PteroBuilder(String applicationUrl, String token) {
        this.token = token;
        this.applicationUrl = applicationUrl;
    }

    public PteroBuilder() {
    }

    public String getToken() {
        return this.token;
    }

    public PteroBuilder setToken(String token) {
        this.token = token;
        return this;
    }

    public String getApplicationUrl() {
        return this.applicationUrl;
    }

    public PteroBuilder setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
        return this;
    }

    public PteroAPI build() {
        return new PteroAPIImpl(this.applicationUrl, this.token);
    }
}
