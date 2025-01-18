package org.barbaris.gmstats.models;

public class GraphDataModel {

    private String day;
    private String month;
    private String year;
    private String hour;
    private String minute;
    private String map;
    private float online;
    private float compareOnline;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public float getOnline() {
        return online;
    }

    public void setOnline(float online) {
        this.online = online;
    }

    public float getCompareOnline() {
        return compareOnline;
    }

    public void setCompareOnline(float compareOnline) {
        this.compareOnline = compareOnline;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }
}
