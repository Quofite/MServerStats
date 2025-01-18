package org.barbaris.gmstats.models;

public class WeeklyDataModel {
    private String day;
    private float averageOnline;
    private int records;
    private int maxOnline;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public float getAverageOnline() {
        return averageOnline;
    }

    public void setAverageOnline(float averageOnline) {
        this.averageOnline = averageOnline;
    }

    public int getRecords() {
        return records;
    }

    public void setRecords(int records) {
        this.records = records;
    }

    public int getMaxOnline() {
        return maxOnline;
    }

    public void setMaxOnline(int maxOnline) {
        this.maxOnline = maxOnline;
    }

    @Override
    public String toString() {
        return String.format("Средний онлайн: %f в %d записях с пиковым онлайном в %d человек(-а)", averageOnline, records, maxOnline);
    }
}
