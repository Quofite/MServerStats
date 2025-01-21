package org.barbaris.gmstats.models;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
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

    public void setTime(Timestamp time) {
        this.time = time;
    }

}
