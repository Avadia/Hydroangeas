package net.samagames.hydroangeas.client.tasks;

import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.client.HydroangeasClient;
import net.samagames.hydroangeas.client.servers.MinecraftServerC;
import net.samagames.hydroangeas.utils.ping.MinecraftPing;
import net.samagames.hydroangeas.utils.ping.MinecraftPingOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class ServerAliveWatchDog {
    public ServerAliveWatchDog(HydroangeasClient instance) {
        //Minecraft vanilla ping
        instance.getScheduler().scheduleAtFixedRate(() -> {
            try {
                List<MinecraftServerC> servers = new ArrayList<>(instance.getServerManager().getServers());
                for (MinecraftServerC server : servers) {
                    if (System.currentTimeMillis() - server.getStartedTime() < 60000L)
                        continue;
                    try {
                        //TODO Verif timeout time default : 100
                        new MinecraftPing().getPing(new MinecraftPingOptions().setHostname(server.getAllocation().getIP()).setPort(Integer.parseInt(server.getAllocation().getPort())).setTimeout(2000));
                    } catch (Exception e) {
                        Hydroangeas.getLogger().info("Can't ping server: " + server.getServerName() + " shutting down");
                        e.printStackTrace();
                        server.stopServer();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 15, 15, TimeUnit.SECONDS);
    }
}
