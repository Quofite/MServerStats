package org.barbaris.gmstats.models;

import java.util.List;

public class AggregatedStatsModel {
    private List<OnlinePerMap> onlinesPerMaps;
    private List<OnlinePerTime> onlinesPerTimes;

    public List<OnlinePerMap> getOnlinesPerMaps() {
        return onlinesPerMaps;
    }

    public void setOnlinesPerMaps(List<OnlinePerMap> onlinesPerMaps) {
        this.onlinesPerMaps = onlinesPerMaps;
    }

    public List<OnlinePerTime> getOnlinesPerTimes() {
        return onlinesPerTimes;
    }

    public void setOnlinesPerTimes(List<OnlinePerTime> onlinesPerTimes) {
        this.onlinesPerTimes = onlinesPerTimes;
    }

}
