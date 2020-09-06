package net.samagames.hydroangeas.utils;

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
public class ModMessage {
    public static void sendMessage(InstanceType type, String message) {
        new JsonModMessage("Hydroangeas " + type, ModChannel.INFORMATION, ChatColor.GREEN, message).send();
    }

    public static void sendError(InstanceType type, String message) {
        new JsonModMessage("Hydroangeas " + type, ModChannel.INFORMATION, ChatColor.GREEN, ChatColor.RED + "✖" + ChatColor.RESET + " " + message).send();
    }

    public static void sendDebug(String message) {
        new JsonModMessage("Hydroangeas DEBUG", ModChannel.INFORMATION, ChatColor.DARK_PURPLE, message).send();
    }
}
