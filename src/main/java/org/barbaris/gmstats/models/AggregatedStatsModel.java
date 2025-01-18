package org.barbaris.gmstats.models;

import java.util.ArrayList;
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

    public List<OnlinePerMap> getOnlinesPerMaps(int amount) {
        List<OnlinePerMap> maps = new ArrayList<>();

        for(int i = 0; i < amount; i++) {
            maps.add(onlinesPerMaps.get(i));
        }

        return maps;
    }

    public List<OnlinePerTime> getOnlinesPerTimes(int amount) {
        List<OnlinePerTime> times = new ArrayList<>();

        for(int i = 0; i < amount; i++) {
            times.add(onlinesPerTimes.get(i));
        }

        return times;
    }


}
