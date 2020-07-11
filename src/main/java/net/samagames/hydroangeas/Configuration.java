package net.samagames.hydroangeas;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
public class Configuration {
    private final Hydroangeas instance;
    public String redisIp;
    public String redisPassword;
    public int redisPort;
    public String sqlIp;
    public String sqlPort;
    public String sqlName;
    public String sqlUser;
    public String sqlPassword;
    private JsonObject jsonConfiguration;

    public Configuration(Hydroangeas instance) {
        this.instance = instance;
        try {
            this.loadConfiguration("config.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void loadConfiguration(String path) throws IOException {
        this.instance.log(Level.INFO, "Configuration file is: " + path);
        File configurationFile = new File(path);

        if (!configurationFile.exists()) {
            this.instance.log(Level.SEVERE, "Configuration file don't exist!");
            System.exit(4);
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configurationFile), StandardCharsets.UTF_8)) {
            this.jsonConfiguration = new JsonParser().parse(reader).getAsJsonObject();
        }

        if (!validateJson(jsonConfiguration)) {
            this.instance.log(Level.SEVERE, "Configuration file isn't valid! Please just modify the default configuration file!");
            System.exit(5);
        }

        this.redisIp = jsonConfiguration.get("redis-ip").getAsString();
        this.redisPort = jsonConfiguration.get("redis-port").getAsInt();
        this.redisPassword = jsonConfiguration.get("redis-password").getAsString();
        this.sqlIp = jsonConfiguration.get("sql-ip").getAsString();
        this.sqlPort = jsonConfiguration.get("sql-port").getAsString();
        this.sqlName = jsonConfiguration.get("sql-name").getAsString();
        this.sqlUser = jsonConfiguration.get("sql-user").getAsString();
        this.sqlPassword = jsonConfiguration.get("sql-password").getAsString();
    }

    public JsonObject getJsonConfiguration() {
        return this.jsonConfiguration;
    }

    public boolean validateJson(JsonObject object) {
        boolean flag = true;

        if (!object.has("redis-ip")) flag = false;
        if (!object.has("redis-port")) flag = false;
        if (!object.has("redis-password")) flag = false;
        if (!object.has("sql-url")) flag = false;
        if (!object.has("sql-user")) flag = false;
        if (!object.has("sql-password")) flag = false;

        return flag;
    }
}
