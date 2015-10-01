package net.samagames.hydroangeas.client.tasks;

import net.samagames.hydroangeas.Hydroangeas;
import net.samagames.hydroangeas.client.servers.MinecraftServerC;
import net.samagames.restfull.LogLevel;
import net.samagames.restfull.RestAPI;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Geekpower14 on 05/07/2015.
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class ServerThread extends Thread
{

    public boolean isServerProcessAlive;
    public Process server;
    public File directory;
    private long lastHeartbeat = System.currentTimeMillis();
    private ExecutorService executor;
    private MinecraftServerC instance;

    public ServerThread(MinecraftServerC instance, String[] command, String[] env, File directory)
    {
        this.instance = instance;
        this.executor = Executors.newFixedThreadPool(5);
        try
        {
            this.directory = directory;

            Thread.sleep(10);

            server = Runtime.getRuntime().exec(command, env, directory);
            isServerProcessAlive = true;

            executor.execute(() -> {
                try
                {
                    String line = null;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(server.getErrorStream())))
                    {
                        while (isServerProcessAlive && (line = reader.readLine()) != null)
                        {
                            RestAPI.getInstance().log(LogLevel.ERROR, instance.getServerName(), line);
                            System.err.println(instance.getServerName() + " > " + line);
                            //TODO handle errors
                        }
                    }
                } catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            });

            executor.execute(() -> {
                try
                {
                    String line = null;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream())))
                    {
                        while (isServerProcessAlive && (line = reader.readLine()) != null)
                        {
                            lastHeartbeat = System.currentTimeMillis();
                            if (line.contains("WARN]"))
                            {
                                RestAPI.getInstance().log(line.contains(" at ") || line.contains("exception") ? LogLevel.ERROR : LogLevel.WARINING, instance.getServerName(), line);
                            }
                            else if (line.contains("SEVERE]"))
                            {
                                RestAPI.getInstance().log(LogLevel.ERROR, instance.getServerName(), line);
                            }
                            //TODO: best crash detection
                        }
                    }
                } catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            });

            executor.execute(() -> {
                while (true)
                {
                    if (System.currentTimeMillis() - lastHeartbeat > 120000)
                    {
                        instance.stopServer();
                    }
                    try
                    {
                        Thread.sleep(15 * 1000);
                    } catch (InterruptedException e)
                    {
                        break;
                    }
                }
            });
        } catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try
        {
            server.waitFor();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        normalStop();
        Hydroangeas.getInstance().getAsClient().getServerManager().onServerStop(instance);
        server.destroy();
    }

    public void normalStop()
    {
        isServerProcessAlive = false;
        instance.getInstance().getScheduler().execute(() -> {
            try
            {
                FileUtils.deleteDirectory(directory);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });
        executor.shutdownNow();
    }

    public void forceStop()
    {
        isServerProcessAlive = false;
        normalStop();
        server.destroy();
        executor.shutdownNow();
    }
}
