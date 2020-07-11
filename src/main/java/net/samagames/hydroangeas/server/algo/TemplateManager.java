package net.samagames.hydroangeas.server.algo;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.common.Messages;
import net.samagames.hydroangeas.server.HydroangeasServer;
import net.samagames.hydroangeas.server.games.AbstractGameTemplate;
import net.samagames.hydroangeas.server.games.PackageGameTemplate;
import net.samagames.hydroangeas.server.games.SimpleGameTemplate;
import net.samagames.hydroangeas.server.waitingqueue.Queue;
import net.samagames.hydroangeas.utils.MiscUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
public class TemplateManager {
    private final HydroangeasServer instance;
    private List<AbstractGameTemplate> templates;

    public TemplateManager(HydroangeasServer instance) {

        this.instance = instance;

        templates = loadTemplates();

        loadQueues();
    }

    public void loadQueues() {
        Hydroangeas.getLogger().info("Ajout des queues pour chaque Template:");

        for (AbstractGameTemplate template : templates) {
            Queue queue = instance.getQueueManager().getQueueByName(template.getId());
            if (queue == null) {
                queue = instance.getQueueManager().addQueue(template);
                if (template.getGameName().toLowerCase().equalsIgnoreCase(Messages.HUB.getMessage()))
                    queue.getWatchQueue().setAutoOrder(false);
            } else {
                queue.reload(template);
            }

            Hydroangeas.getLogger().info("ID: " + template.getId() + " Jeu: " + template.getGameName() + " Map: " + template.getMapName());
        }
    }

    @SuppressWarnings("deprecation")
    public List<AbstractGameTemplate> loadTemplates() {
        List<AbstractGameTemplate> result = new ArrayList<>();
        File directory = new File(MiscUtils.getApplicationDirectory(), "templates");
        try {
            File[] files = directory.listFiles();
            if (files == null) // Internal IO Exception
                throw new IOException("Internal IO Exception during listing of templates directory!");
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try {
                        JsonElement data = new JsonParser().parse(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                        if (data == null)
                            throw new JsonParseException("JSON object return null");
                        if (data.getAsJsonObject().getAsJsonPrimitive("Type") != null && data.getAsJsonObject().getAsJsonPrimitive("Type").getAsString().equals("Package")) {
                            result.add(new PackageGameTemplate(file.getName().split("\\.")[0], data));
                        } else {
                            result.add(new SimpleGameTemplate(file.getName().split("\\.")[0], data));
                        }
                    } catch (JsonParseException e) {
                        Hydroangeas.getLogger().severe("Invalid template " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void reload() {
        templates.clear();
        templates = loadTemplates();
        loadQueues();
    }

    public AbstractGameTemplate getTemplateByID(String id) {
        for (AbstractGameTemplate template : templates) {
            if (template.getId().equalsIgnoreCase(id)) {
                return template;
            }
        }
        return null;
    }

    public AbstractGameTemplate getTemplateByGameAndMap(String game, String map) {
        for (AbstractGameTemplate template : templates) {
            if (template.getGameName().equalsIgnoreCase(game) && template.getMapName().equalsIgnoreCase(map)) {
                return template;
            }
        }
        return null;
    }

    public List<AbstractGameTemplate> getTemplatesByGame(String game) {
        return templates.stream().filter(template -> template.getGameName().equalsIgnoreCase(game)).collect(Collectors.toList());
    }

    public List<AbstractGameTemplate> getTemplates() {
        return templates;
    }

    public List<String> getListTemplate() {
        List<String> tmp = new ArrayList<>();
        templates.forEach((template -> tmp.add(template.getId())));
        return tmp;
    }
}
