package org.barbaris.gmstats.services.caching;

import com.google.gson.Gson;
import org.barbaris.gmstats.models.OnlinePerTime;
import org.barbaris.gmstats.services.DBService;
import org.barbaris.gmstats.services.Utils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class OnlinePerTimesCaching extends Thread {

    private final DBService dbService;
    private final JdbcTemplate template;

    Utils utils = new Utils();
    Gson gson = new Gson();

    public OnlinePerTimesCaching(JdbcTemplate template, DBService dbService) {
        super();
        this.dbService = dbService;
        this.template = template;
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
        List<String> times = utils.generateTimes();

        for (int id : dbService.getGoodIds()) {
            List<OnlinePerTime> onlinePerTimes = new ArrayList<>();
            for (String time : times) {
                try {
                    sql = String.format("SELECT SUM(players) FROM statistics WHERE to_char(time, 'HH24:MI')='%s' AND server_id=%d;", time, id);
                    players = (Long) template.queryForMap(sql).get("sum");

                    sql = String.format("SELECT COUNT(*) FROM statistics WHERE to_char(time, 'HH24:MI')='%s' AND server_id=%d;", time, id);
                    count = (Long) template.queryForMap(sql).get("count");

                    onlinePerTimes.add(new OnlinePerTime(time, (players / count)));
                } catch (NullPointerException ex) {
                    onlinePerTimes.add(new OnlinePerTime(time, 0));
                }
            }

            sql = String.format("UPDATE cache SET average_onlines_per_times='%s' WHERE server_id=%d;", gson.toJson(onlinePerTimes), id);
            template.execute(sql);
        }

        System.out.println("Times caching done!");
    }
}
