package org.barbaris.gmstats.services.caching;

import org.barbaris.gmstats.models.Values;
import org.barbaris.gmstats.services.DataAnalysisService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Locale;

public class MapStatsCaching extends Thread {
    private final DataAnalysisService dataAnalysisService;
    private final JdbcTemplate template;

    public MapStatsCaching(DataAnalysisService dataAnalysisService, JdbcTemplate template) {
        super();
        this.dataAnalysisService = dataAnalysisService;
        this.template = template;
    }

    @Override
    public void run() {
        super.run();

        String sql;
        long players;
        float count;
        List<String> maps = dataAnalysisService.getMaps(null, 0, null, null);
        for (String map : maps) {
            sql = String.format("SELECT SUM(players) FROM statistics WHERE map='%s';", map);
            players = (Long) template.queryForMap(sql).get("sum");

            sql = String.format("SELECT COUNT(*) FROM statistics WHERE map='%s';", map);
            count = (Long) template.queryForMap(sql).get("count");

            if (count >= Values.RECORDS_A_DAY) {
                sql = String.format(Locale.US, "INSERT INTO mapstatscache(map, average_online, records) VALUES ('%s', %f, %d);", map, (players / count), Math.round(count));
                template.execute(sql);
            }
        }
    }
}
