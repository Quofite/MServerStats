package org.barbaris.gmstats.services.caching;

import org.barbaris.gmstats.services.Utils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Locale;

public class TimeStatsCaching extends Thread {
    private final Utils utils = new Utils();
    private final JdbcTemplate template;

    public TimeStatsCaching(JdbcTemplate template) {
        super();
        this.template = template;
    }

    @Override
    public void run() {
        super.run();

        String sql;
        long players;
        float count;
        List<String> times = utils.generateTimes();
        for (String time : times) {
            sql = String.format("SELECT SUM(players) FROM statistics WHERE to_char(time, 'HH24:MI')='%s';", time);
            players = (Long) template.queryForMap(sql).get("sum");

            sql = String.format("SELECT COUNT(*) FROM statistics WHERE to_char(time, 'HH24:MI')='%s';", time);
            count = (Long) template.queryForMap(sql).get("count");

            sql = String.format(Locale.US, "INSERT INTO timestatscache(time, average_online) VALUES ('%s', %f);", time, (players / count));
            template.execute(sql);
        }
    }
}
