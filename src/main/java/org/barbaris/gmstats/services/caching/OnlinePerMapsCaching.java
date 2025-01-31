package org.barbaris.gmstats.services.caching;

import com.google.gson.Gson;
import org.barbaris.gmstats.models.OnlinePerMap;
import org.barbaris.gmstats.models.Values;
import org.barbaris.gmstats.services.DataAnalysisService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class OnlinePerMapsCaching extends Thread {
    private final int id;
    private final JdbcTemplate template;
    private final DataAnalysisService dataAnalysisService;

    Gson gson = new Gson();

    public OnlinePerMapsCaching(DataAnalysisService dataAnalysisService, JdbcTemplate template, int id) {
        super();
        this.id = id;
        this.template = template;
        this.dataAnalysisService = dataAnalysisService;
    }

    @Override
    public void run() {
        super.run();
        cache();
    }

    private synchronized void cache() {
        String sql;
        long players;
        float count;
        List<OnlinePerMap> onlinePerMaps = new ArrayList<>();
        List<String> maps = dataAnalysisService.getMaps(null, 0, null, null);
        for (String map : maps) {
            try {
                sql = String.format("SELECT SUM(players) FROM statistics WHERE map='%s' AND server_id=%d;", map, id);
                players = (Long) template.queryForMap(sql).get("sum");

                sql = String.format("SELECT COUNT(*) FROM statistics WHERE map='%s' AND server_id=%d;", map, id);
                count = (Long) template.queryForMap(sql).get("count");

                if (count >= Values.RECORDS_A_DAY) {
                    onlinePerMaps.add(new OnlinePerMap(map, (players / count), Math.round(count)));
                }
            } catch (Exception ignored) {

            }
        }

        sql = String.format("UPDATE cache SET average_onlines_per_times='%s' WHERE server_id=%d;", gson.toJson(onlinePerMaps), id);
        template.execute(sql);

        notify();
    }
}
