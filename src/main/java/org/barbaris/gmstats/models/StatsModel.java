package org.barbaris.gmstats.models;

import java.sql.Timestamp;

public class StatsModel {
    private int id;
    private int server_id;
    private int req_id;
    private Timestamp time;
    private int players;
    private String map;
    private String hostname;
    private String ip;
    private int port;
    private String players_json;

    private String mapLink;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServer_id() {
        return server_id;
    }

    public void setServer_id(int server_id) {
        this.server_id = server_id;
    }

    public String getPlayers_json() {
        return players_json;
    }

    public void setPlayers_json(String players_json) {
        this.players_json = players_json;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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
        this.mapLink = Values.STEAM_WORKSHOP_LINK + map;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getReq_id() {
        return req_id;
    }

    public void setReq_id(int req_id) {
        this.req_id = req_id;
    }

    public String getMapLink() {
        return mapLink;
    }

    public void setMapLink(String mapLink) {
        this.mapLink = mapLink;
    }
}
