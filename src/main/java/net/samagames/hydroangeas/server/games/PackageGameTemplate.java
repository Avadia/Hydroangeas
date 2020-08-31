package net.samagames.hydroangeas.server.games;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.server.algo.TemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
public class PackageGameTemplate implements AbstractGameTemplate {
    private final String id;

    private final List<String> templates = new ArrayList<>();

    private SimpleGameTemplate currentTemplate;

    public PackageGameTemplate(String id, JsonElement data) {
        this.id = id;
        JsonObject object = data.getAsJsonObject();

        for (JsonElement element : object.getAsJsonArray("Templates")) {
            templates.add(element.getAsString());
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean selectTemplate(TemplateManager templateManager) {
        Random random = new Random();
        String selected = templates.get(random.nextInt(templates.size()));
        AbstractGameTemplate template = templateManager.getTemplateByID(selected);
        if (template == null || template instanceof PackageGameTemplate) {
            Hydroangeas.getLogger().severe("Package Template: " + id + " contains an invalid sub template");
            return false;
        } else {
            currentTemplate = (SimpleGameTemplate) template;
        }
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getGameName() {
        verifTemplateInit();
        return currentTemplate.getGameName();
    }

    @Override
    public String getMapName() {
        verifTemplateInit();
        return currentTemplate.getMapName();
    }

    @Override
    public int getMinSlot() {
        verifTemplateInit();
        return currentTemplate.getMinSlot();
    }

    @Override
    public int getMaxSlot() {
        verifTemplateInit();
        return currentTemplate.getMaxSlot();
    }

    @Override
    public JsonElement getOptions() {
        verifTemplateInit();
        return currentTemplate.getOptions();
    }

    @Override
    public int getWeight() {
        verifTemplateInit();
        return currentTemplate.getWeight();
    }

    @Override
    public boolean isCoupaing() {
        verifTemplateInit();
        return currentTemplate.isCoupaing();
    }

    @Override
    public String toString() {
        return "Template id: " + id + ((isCoupaing()) ? " Coupaing Server " : " ");
    }

    @Override
    public JsonElement getStartupOptions() {
        verifTemplateInit();
        return currentTemplate.getStartupOptions();
    }

    @Override
    public void addTimeToStart(long time) {
        verifTemplateInit();
        currentTemplate.addTimeToStart(time);
    }

    @Override
    public long getTimeToStart() {
        verifTemplateInit();
        return currentTemplate.getTimeToStart();
    }

    @Override
    public void resetStats() {
        for (String template : templates) {
            try {
                Hydroangeas.getInstance().getAsServer().getTemplateManager().getTemplateByID(template).resetStats();
            } catch (Exception ignored) {

            }
        }
    }

    public void verifTemplateInit() {
        if (currentTemplate == null)
            selectTemplate(Hydroangeas.getInstance().getAsServer().getTemplateManager());
    }
}
