package net.samagames.hydroangeas;

import net.samagames.hydroangeas.client.HydroangeasClient;
import net.samagames.hydroangeas.server.HydroangeasServer;

import java.io.IOException;

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
public class Main {
    public static void main(String[] args) {
        try {
            Hydroangeas hydroangeas;

            if (args[0].equalsIgnoreCase("server"))
                hydroangeas = new HydroangeasServer();
            else if (args[0].equalsIgnoreCase("client"))
                hydroangeas = new HydroangeasClient();
            else {
                System.exit(0);
                return;
            }

            while (hydroangeas.isRunning) {
                String line = null;
                try {
                    line = hydroangeas.getConsoleReader().readLine(">");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (line != null) {
                    hydroangeas.getCommandManager().inputCommand(line);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getLocalizedMessage());
            System.exit(42);
        }
    }
}
