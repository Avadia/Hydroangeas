package net.samagames.hydroangeas.server.client;

import com.google.gson.JsonElement;
import net.samagames.hydroangeas.common.protocol.intranet.AskForClientActionPacket;
import net.samagames.hydroangeas.common.protocol.intranet.MinecraftServerInfoPacket;
import net.samagames.hydroangeas.server.data.Status;
import net.samagames.hydroangeas.server.games.AbstractGameTemplate;

import java.util.UUID;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Geekpower14 on 25/06/2015.
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class MinecraftServerS
{

    private HydroClient client;
    private UUID uuid;
    private boolean coupaingServer;
    private String game;
    private String map;
    private int minSlot;
    private int maxSlot;
    private JsonElement options, startupOptions;

    private Integer hubID;

    private boolean started;

    private String templateID;

    private int weight;

    private int port;

    private Status status = Status.STARTING;
    private int actualSlots;


    public MinecraftServerS(HydroClient client, AbstractGameTemplate template)
    {
        this(client, UUID.randomUUID(), template.getGameName(), template.getMapName(), template.getMinSlot(), template.getMaxSlot(), template.getOptions(), template.getStartupOptions());
        this.coupaingServer = template.isCoupaing();
        this.templateID = template.getId();
    }

    public MinecraftServerS(HydroClient client, MinecraftServerInfoPacket packet)
    {
        this(client, UUID.randomUUID(), packet.getGame(), packet.getMap(), packet.getMinSlot(), packet.getMaxSlot(), packet.getOptions(), packet.getStartupOptions());
        this.port = packet.getPort();
    }

    public MinecraftServerS(HydroClient client, UUID uuid, String game, String map, int minSlot, int maxSlot, JsonElement options, JsonElement startupOptions)
    {
        this.client = client;
        this.uuid = uuid;
        this.game = game;
        this.map = map;
        this.minSlot = minSlot;
        this.maxSlot = maxSlot;
        this.options = options;
        this.startupOptions = startupOptions;
    }

    public void shutdown()
    {
        client.getInstance().getConnectionManager().sendPacket(client,
                new AskForClientActionPacket(client.getUUID(), AskForClientActionPacket.ActionCommand.SERVEREND, getServerName()));
    }

    public void onShutdown()
    {
        //If we need to save some data after shutdown
    }

    public void changeUUID()
    {
        this.uuid = UUID.randomUUID();
    }

    public UUID getUUID()
    {
        return this.uuid;
    }

    public String getGame()
    {
        return this.game;
    }

    public String getMap()
    {
        return this.map;
    }

    public String getServerName()
    {
        return this.game + "_" + ((hubID == null) ? this.uuid.toString().split("-")[0] : hubID);
    }

    public int getMinSlot()
    {
        return this.minSlot;
    }

    public int getMaxSlot()
    {
        return this.maxSlot;
    }

    public JsonElement getOptions()
    {
        return this.options;
    }

    public boolean isCoupaingServer()
    {
        return this.coupaingServer;
    }

    public void setCoupaingServer(boolean isCoupaing)
    {
        this.coupaingServer = isCoupaing;
    }

    public boolean isStarted()
    {
        return started;
    }

    public void setStarted(boolean started)
    {
        this.started = started;
    }

    public int getWeight()
    {
        return weight;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getTemplateID()
    {
        return templateID;
    }

    public void setTemplateID(String templateID)
    {
        this.templateID = templateID;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public int getActualSlots()
    {
        return actualSlots;
    }

    public void setActualSlots(int actualSlots)
    {
        this.actualSlots = actualSlots;
    }

    public boolean isHub()
    {
        return hubID != null;
    }

    public Integer getHubID()
    {
        return hubID;
    }

    public void setHubID(int hubID)
    {
        this.hubID = hubID;
    }

    public JsonElement getStartupOptions()
    {
        return startupOptions;
    }
}
