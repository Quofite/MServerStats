package org.barbaris.gmstats.models;

public class MapsDataModel {

    private float averagePlayers;
    private int records;

    public MapsDataModel(float averagePlayers, int records) {
        setAveragePlayers(averagePlayers);
        setRecords(records);
    }

    public float getAveragePlayers() {
        return averagePlayers;
    }

    public void setAveragePlayers(float averagePlayers) {
        this.averagePlayers = averagePlayers;
    }

    public int getRecords() {
        return records;
    }

    public void setRecords(int records) {
        this.records = records;
    }
}
