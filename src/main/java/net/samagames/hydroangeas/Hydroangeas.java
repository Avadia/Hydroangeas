package net.samagames.hydroangeas;

import jline.console.ConsoleReader;
import net.samagames.hydroangeas.client.HydroangeasClient;
import net.samagames.hydroangeas.common.commands.CommandManager;
import net.samagames.hydroangeas.common.database.DatabaseConnector;
import net.samagames.hydroangeas.common.database.RedisSubscriber;
import net.samagames.hydroangeas.common.log.HydroLogger;
import net.samagames.hydroangeas.server.HydroangeasServer;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public abstract class Hydroangeas {
    public static final String version = "1.0.0.0";
    public static final boolean production = false;
    protected static Logger logger;
    private static Hydroangeas instance;
    protected final ScheduledExecutorService scheduler;
    protected final ConsoleReader consoleReader;
    public boolean isRunning;
    protected UUID uuid;
    protected Configuration configuration;
    protected DatabaseConnector databaseConnector;
    protected RedisSubscriber redisSubscriber;
    protected CommandManager commandManager;

    public Hydroangeas() throws IOException {
        instance = this;
        uuid = UUID.randomUUID();

        AnsiConsole.systemInstall();
        consoleReader = new ConsoleReader();
        consoleReader.setExpandEvents(false);

        logger = new HydroLogger(this);

        logger.info("Hydroangeas (V" + version + ") (Prod: " + production + ")");
        logger.info("----------------------------------------");

        this.scheduler = Executors.newScheduledThreadPool(16);
        loadConfig();
        this.databaseConnector = new DatabaseConnector(this);

        this.redisSubscriber = new RedisSubscriber(this);
        this.enable();
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            this.log(Level.INFO, "Shutdown asked!");
            this.shutdown();
            this.log(Level.INFO, "Bye!");
        }));

        isRunning = true;
        logger.info("Hydroangeas enabled!");
    }

    public static Hydroangeas getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return logger;
    }

    public abstract void enable();

    public abstract void disable();

    public void loadConfig() {
        this.configuration = new Configuration(this);
    }

    public void shutdown() {
        isRunning = false;

        disable();
        scheduler.shutdown();

        this.redisSubscriber.disable();

        databaseConnector.disconnect();
    }

    public void log(Level level, String message) {
        logger.log(level, message);
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    public RedisSubscriber getRedisSubscriber() {
        return this.redisSubscriber;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public HydroangeasClient getAsClient() {
        if (this instanceof HydroangeasClient)
            return (HydroangeasClient) this;
        else
            return null;
    }

    public HydroangeasServer getAsServer() {
        if (this instanceof HydroangeasServer)
            return (HydroangeasServer) this;
        else
            return null;
    }

    public ConsoleReader getConsoleReader() {
        return consoleReader;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public UUID getUUID() {
        return uuid;
    }

    public enum RestrictionMode {
        NONE("none"),
        WHITELIST("whitelist"),
        BLACKLIST("blacklist");

        private final String mode;

        RestrictionMode(String mode) {

            this.mode = mode;
        }

        static public RestrictionMode valueFrom(String mode) {
            for (RestrictionMode data : RestrictionMode.values()) {
                if (data.getMode().equalsIgnoreCase(mode))
                    return data;
            }

            return RestrictionMode.NONE;
        }

        public String getMode() {
            return mode;
        }
    }
}
