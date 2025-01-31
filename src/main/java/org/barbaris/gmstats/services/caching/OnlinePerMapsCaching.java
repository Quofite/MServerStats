package org.barbaris.gmstats.services.caching;

import com.google.gson.Gson;
import org.barbaris.gmstats.models.OnlinePerMap;
import org.barbaris.gmstats.models.Values;
import org.barbaris.gmstats.services.DBService;
import org.barbaris.gmstats.services.DataAnalysisService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class OnlinePerMapsCaching extends Thread {
    private final JdbcTemplate template;
    private final DBService dbService;
    private final DataAnalysisService dataAnalysisService;

    Gson gson = new Gson();

    public OnlinePerMapsCaching(DataAnalysisService dataAnalysisService, DBService dbService, JdbcTemplate template) {
        super();
        this.template = template;
        this.dbService = dbService;
        this.dataAnalysisService = dataAnalysisService;
    }

    @Override
    public void run() {
        super.run();
        cache();
    }

    private void cache() {
        String sql;
        long players;
        float count;

        for (int id : dbService.getGoodIds()) {
            List<OnlinePerMap> onlinePerMaps = new ArrayList<>();
            List<String> maps = dataAnalysisService.getMaps(String.valueOf(id), 0, null, null);
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

            sql = String.format("UPDATE cache SET average_onlines_per_maps='%s' WHERE server_id=%d;", gson.toJson(onlinePerMaps), id);
            template.execute(sql);
        }

        System.out.println("Maps caching done!");
    }
}
