package org.barbaris.gmstats.models;

public class ServerModel {
    private int id;
    private int mid;
    private String hostname;
    private String map;
    private String ip;
    private int port;
    private String owner;
    private float popularityRatio;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public float getPopularityRatio() {
        return popularityRatio;
    }

    public void setPopularityRatio(float popularityRatio) {
        this.popularityRatio = popularityRatio;
    }
}
