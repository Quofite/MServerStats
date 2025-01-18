package org.barbaris.gmstats.models;

public class PlayerModel {
    private String nickname;
    private String steamid;
    private int wrench_id;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSteamid() {
        return steamid;
    }

    public void setSteamid(String steamid) {
        this.steamid = steamid;
    }

    public int getWrench_id() {
        return wrench_id;
    }

    public void setWrench_id(int wrench_id) {
        this.wrench_id = wrench_id;
    }
}
