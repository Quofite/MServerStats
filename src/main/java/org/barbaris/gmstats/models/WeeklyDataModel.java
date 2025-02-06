package org.barbaris.gmstats.models;

public class WeeklyDataModel {
    private String day;
    private float averageOnline;
    private int records;
    private int maxOnline;

    public void setDay(String day) {
        this.day = day;
    }

    public void setAverageOnline(float averageOnline) {
        this.averageOnline = averageOnline;
    }

    public void setRecords(int records) {
        this.records = records;
    }

    public void setMaxOnline(int maxOnline) {
        this.maxOnline = maxOnline;
    }

    public String getDay() {
        return day;
    }

    public float getAverageOnline() {
        return averageOnline;
    }

    public int getRecords() {
        return records;
    }

    public int getMaxOnline() {
        return maxOnline;
    }

    @Override
    public String toString() {
        return String.format("Средний онлайн: %f в %d записях с пиковым онлайном в %d человек(-а)", averageOnline, records, maxOnline);
    }
}
