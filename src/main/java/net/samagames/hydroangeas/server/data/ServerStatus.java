package net.samagames.hydroangeas.server.data;

public class ServerStatus
{
    private String bungeeName;
    private String game;
    private String map;
    private Status status;
    private int players;
    private int maxPlayers;

    public ServerStatus(String bungeeName, String game, String map, Status status, int players, int maxPlayers)
    {
        this.bungeeName = bungeeName;
        this.game = game;
        this.map = map;
        this.status = status;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    public String getBungeeName()
    {
        return this.bungeeName;
    }

    public void setBungeeName(String bungeeName)
    {
        this.bungeeName = bungeeName;
    }

    public String getGame()
    {
        return this.game;
    }

    public void setGame(String game)
    {
        this.game = game;
    }

    public String getMap()
    {
        return this.map;
    }

    public void setMap(String map)
    {
        this.map = map;
    }

    public Status getStatus()
    {
        return this.status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public int getPlayers()
    {
        return this.players;
    }

    public void setPlayers(int players)
    {
        this.players = players;
    }

    public int getMaxPlayers()
    {
        return this.maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers)
    {
        this.maxPlayers = maxPlayers;
    }
}
