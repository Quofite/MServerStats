package org.barbaris.gmstats.models;

import java.util.List;

public class DataAnalysisModel {
    private float averagePlayers;
    private int maxPlayers;
    private List<StatsModel> maxPlayersRecords;

    private float maxDailyAverage;
    private String maxDailyAverageDay;

    private int maxPlayersInMaxDailyAverage;
    private List<String> maxPlayersInMaxDailyAverageMaps;

    private String mostPopularMap;
    private MapsDataModel mostPopularMapStats;
    private String adminFavouriteMap;
    private MapsDataModel adminsFavouriteMapStats;

    // ----------------- GETTERS AND SETTERS ------------------

    public float getAveragePlayers() {
        return averagePlayers;
    }

    public void setAveragePlayers(float averagePlayers) {
        this.averagePlayers = averagePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public float getMaxDailyAverage() {
        return maxDailyAverage;
    }

    public void setMaxDailyAverage(float maxDailyAverage) {
        this.maxDailyAverage = maxDailyAverage;
    }

    public String getMaxDailyAverageDay() {
        return maxDailyAverageDay;
    }

    public void setMaxDailyAverageDay(String maxDailyAverageDay) {
        this.maxDailyAverageDay = maxDailyAverageDay;
    }

    public int getMaxPlayersInMaxDailyAverage() {
        return maxPlayersInMaxDailyAverage;
    }

    public void setMaxPlayersInMaxDailyAverage(int maxPlayersInMaxDailyAverage) {
        this.maxPlayersInMaxDailyAverage = maxPlayersInMaxDailyAverage;
    }

    public List<String> getMaxPlayersInMaxDailyAverageMaps() {
        return maxPlayersInMaxDailyAverageMaps;
    }

    public void setMaxPlayersInMaxDailyAverageMaps(List<String> maxPlayersInMaxDailyAverageMaps) {
        this.maxPlayersInMaxDailyAverageMaps = maxPlayersInMaxDailyAverageMaps;
    }

    public String getMostPopularMap() {
        return mostPopularMap;
    }

    public void setMostPopularMap(String mostPopularMap) {
        this.mostPopularMap = mostPopularMap;
    }

    public String getAdminFavouriteMap() {
        return adminFavouriteMap;
    }

    public void setAdminFavouriteMap(String adminFavouriteMap) {
        this.adminFavouriteMap = adminFavouriteMap;
    }

    public List<StatsModel> getMaxPlayersRecords() {
        return maxPlayersRecords;
    }

    public void setMaxPlayersRecords(List<StatsModel> maxPlayersRecords) {
        this.maxPlayersRecords = maxPlayersRecords;
    }

    public MapsDataModel getMostPopularMapStats() {
        return mostPopularMapStats;
    }

    public void setMostPopularMapStats(MapsDataModel mostPopularMapStats) {
        this.mostPopularMapStats = mostPopularMapStats;
    }

    public MapsDataModel getAdminsFavouriteMapStats() {
        return adminsFavouriteMapStats;
    }

    public void setAdminsFavouriteMapStats(MapsDataModel adminsFavouriteMapStats) {
        this.adminsFavouriteMapStats = adminsFavouriteMapStats;
    }
}
