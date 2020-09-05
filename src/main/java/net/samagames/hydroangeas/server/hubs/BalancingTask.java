package net.samagames.hydroangeas.server.hubs;

import net.samagames.hydroangeas.server.client.MinecraftServerS;

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
public class BalancingTask extends Thread {
    public static final double HUB_SAFETY_MARGIN = 1;
    public static final int HUB_CONSIDERED_AS_EMPTY = 5; //Number minimum of player on a HUB we can stop
    private final HubBalancer hubBalancer;
    private int coolDown; //*1000ms (1s)

    public BalancingTask(HubBalancer hubBalancer) {
        this.hubBalancer = hubBalancer;
        coolDown = 10; //Wait 10s to load balance hub
    }

    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        while (true) {
            try {
                //Wait desired time in case of server start
                cooldown(this.coolDown);

                //Calculate the needed lobby
                int requestNumber = (int) Math.ceil(needNumberOfHub());

                //Need we some lobby ?
                if (hubBalancer.getNumberServer() < requestNumber) {
                    //Start them !
                    boolean doCoolDown = false;
                    for (int i = requestNumber - hubBalancer.getNumberServer(); i > 0; i--) {
                        if (hubBalancer.startNewHub()) {
                            doCoolDown = true;
                        }
                    }
                    //Wait until started
                    if (doCoolDown)
                        coolDown += 60;

                    //Are they too much lobby ?
                } else if (hubBalancer.getNumberServer() > requestNumber) {
                    //Stop them !
                    List<MinecraftServerS> balancedHubList = new ArrayList<>(hubBalancer.getBalancedHubList());
                    for (MinecraftServerS serverS : balancedHubList) {
                        if (hubBalancer.getNumberServer() == requestNumber)
                            break;

                        if (serverS.getActualSlots() < HUB_CONSIDERED_AS_EMPTY) {
                            //We are good so we let to players the time to leave the lobby
                            MinecraftServerS hubTo = null;
                            for (MinecraftServerS minecraftServerS : hubBalancer.getBalancedHubList()) {
                                if (!minecraftServerS.getHubID().equals(serverS.getHubID())) {
                                    hubTo = minecraftServerS;
                                    break;
                                }
                            }
                            if (hubTo == null) {
                                serverS.dispatchCommand("evacuate auth");
                            } else {
                                serverS.dispatchCommand("evacuate " + hubTo.getServerName());
                            }
                            //Security force shutdown
                            hubBalancer.getInstance().getScheduler().schedule(serverS::shutdown, 65, TimeUnit.SECONDS);
                        }
                    }
                }
                TimeUnit.SECONDS.sleep(1);//Need to be very reactive
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public double needNumberOfHub() {
        double v = (((double) hubBalancer.getUsedSlots()) * 1.1) / (double) hubBalancer.getHubTemplate().getMaxSlot();
        if (v <= 0.5)
            return 1;

        return v + HUB_SAFETY_MARGIN;
    }

    public void increaseCooldown(int sec) {
        coolDown += sec;
    }

    public void cooldown(final int coolDown) throws InterruptedException {
        TimeUnit.SECONDS.sleep(coolDown);
        if (this.coolDown == coolDown) {
            this.coolDown = 0;
        } else {
            this.coolDown -= coolDown;
            cooldown(this.coolDown);
        }
    }
}
