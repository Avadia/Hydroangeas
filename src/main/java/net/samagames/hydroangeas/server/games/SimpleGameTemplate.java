package net.samagames.hydroangeas.server.games;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

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
public class SimpleGameTemplate implements AbstractGameTemplate {
    private static final JsonObject DEFAULT_OPTIONS;
    private static final JsonObject DEFAULT_STARTUP_OPTIONS;

    static {
        DEFAULT_OPTIONS = new JsonObject();
        DEFAULT_STARTUP_OPTIONS = new JsonObject();
        DEFAULT_STARTUP_OPTIONS.addProperty("RAM", 1024L);
        DEFAULT_STARTUP_OPTIONS.addProperty("swap", 1024L);
        DEFAULT_STARTUP_OPTIONS.addProperty("disk", 500L);
        DEFAULT_STARTUP_OPTIONS.addProperty("plugins", "net.samagames:samagamescore;fr.farmvivi:api-spigot;ViaVersion;ViaBackwards;ViaRewind");
        DEFAULT_STARTUP_OPTIONS.addProperty("configs", "default");
    }

    private final String id;

    private String gameName;
    private String mapName;
    private int minSlot;
    private int maxSlot;
    private JsonElement options;
    private JsonObject startupOptions;
    private boolean isCoupaing;

    private int weight;

    private ArrayBlockingQueue<Long> stats;

    public SimpleGameTemplate(String id, JsonElement data) {
        stats = new ArrayBlockingQueue<>(5, true);

        JsonObject formated = data.getAsJsonObject();
        this.id = id;
        this.gameName = Objects.requireNonNull(multiple(formated, "game-name", "gameName")).getAsString();
        this.mapName = Objects.requireNonNull(multiple(formated, "map-name", "mapName")).getAsString();
        this.minSlot = Objects.requireNonNull(multiple(formated, "min-slots", "minSlot")).getAsInt();
        this.maxSlot = Objects.requireNonNull(multiple(formated, "max-slots", "maxSlot")).getAsInt();
        this.options = formated.get("options");
        this.startupOptions = new JsonObject();
        JsonElement startupElement = formated.get("startupOptions");
        if (startupElement != null) {
            for (Map.Entry<String, JsonElement> entry : startupElement.getAsJsonObject().entrySet()) {
                startupOptions.addProperty(entry.getKey(), entry.getValue().getAsString());
            }
        }
        for (Map.Entry<String, JsonElement> entry : DEFAULT_STARTUP_OPTIONS.entrySet()) {
            if (!startupOptions.has(entry.getKey())) {
                startupOptions.addProperty(entry.getKey(), entry.getValue().getAsString());
            }
        }
        this.isCoupaing = formated.get("isCoupaing").getAsBoolean();

        //Temproray until autmatic compute
        this.weight = 150;
        if (formated.has("weight"))
            this.weight = formated.get("weight").getAsInt();
    }

    public SimpleGameTemplate(String id, String gameName, String mapName, int minSlot, int maxSlot, int weight, JsonElement options) {
        this(id, gameName, mapName, minSlot, maxSlot, options, weight, false);
    }

    public SimpleGameTemplate(String id, String gameName, String mapName, int minSlot, int maxSlot, JsonElement options, int weight, boolean isCoupaing) {
        this.id = id;
        this.gameName = gameName;
        this.mapName = mapName;
        this.minSlot = minSlot;
        this.maxSlot = maxSlot;
        this.options = options;
        this.isCoupaing = isCoupaing;
    }

    private int computeWeight() {
        return 0;
    }

    private JsonElement multiple(JsonObject object, String... multiple) {
        for (String id :
                multiple) {
            if (object.has(id))
                return object.get(id);
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public int getMinSlot() {
        return minSlot;
    }

    public void setMinSlot(int minSlot) {
        this.minSlot = minSlot;
    }

    public int getMaxSlot() {
        return maxSlot;
    }

    public void setMaxSlot(int maxSlot) {
        this.maxSlot = maxSlot;
    }

    public JsonElement getOptions() {
        return options;
    }

    public void setOptions(JsonElement options) {
        this.options = options;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isCoupaing() {
        return isCoupaing;
    }

    public void setIsCoupaing(boolean isCoupaing) {
        this.isCoupaing = isCoupaing;
    }

    public String toString() {
        return "Template id: " + id + ((isCoupaing) ? " Coupaing Server " : " ");
    }

    @Override
    public JsonObject getStartupOptions() {
        return startupOptions;
    }

    @Override
    public void addTimeToStart(long time) {
        if (stats.remainingCapacity() <= 0) {
            stats.poll();
        }
        stats.offer(time);
    }

    @Override
    public long getTimeToStart() {
        long startTime = 0;
        int nb = 0;
        for (Long time : stats) {
            startTime += time;
            nb++;
        }
        return (nb > 0) ? startTime / nb : -1;
    }

    @Override
    public void resetStats() {
        stats.clear();
    }
}
