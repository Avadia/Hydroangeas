package net.samagames.hydroangeas.server.algo;

import net.samagames.hydroangeas.server.HydroangeasServer;
import net.samagames.hydroangeas.server.client.MinecraftServerS;
import net.samagames.hydroangeas.server.games.SimpleGameTemplate;
import net.samagames.hydroangeas.server.waitingqueue.Queue;

import java.util.UUID;

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
public class HostGameManager {
    private final HydroangeasServer instance;

    // private HashMap<UUID, MinecraftServerS> servers = new HashMap<UUID, MinecraftServerS>();

    public HostGameManager(HydroangeasServer instance) {
        this.instance = instance;
    }

    public MinecraftServerS orderServer(UUID asker, SimpleGameTemplate template) {
        if (instance.getQueueManager().getQueueByTemplate(template.getId()) != null)
            return null;

        Queue queue = instance.getQueueManager().addQueue(template, instance.getQueueManager(), instance.getTemplateManager());
        queue.getWatchQueue().setAutoOrder(false);

        MinecraftServerS minecraftServerS = instance.getAlgorithmicMachine().orderTemplate(template, getClass().getSimpleName());
        //servers.put(minecraftServerS.getUUID(), minecraftServerS);
        minecraftServerS.setOwner(asker);
        minecraftServerS.setCoupaingServer(true);

        return minecraftServerS;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeServer(String name) {
        MinecraftServerS s = instance.getClientManager().getServerByName(name);

        if (s != null) {
            Queue queue = instance.getQueueManager().getQueueByTemplate(s.getTemplateID());
            if (queue != null) {
                instance.getQueueManager().removeQueue(queue);
                return true;
            }
        }
        return false;
    }
}
