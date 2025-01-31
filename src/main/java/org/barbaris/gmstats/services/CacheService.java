package org.barbaris.gmstats.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.barbaris.gmstats.models.*;
import org.barbaris.gmstats.services.caching.MapStatsCaching;
import org.barbaris.gmstats.services.caching.OnlinePerMapsCaching;
import org.barbaris.gmstats.services.caching.OnlinePerTimesCaching;
import org.barbaris.gmstats.services.caching.TimeStatsCaching;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * <p>This service handles everything related to writing or reading cache.</p>
 * <p>Cache reading method is separated into several methods so program do not have to read everything
 * just to get one particular data piece.</p>
 * <p>Probably later this class will be re-written in Go language in case it would increase performance.</p>
 * */
@Service
public class CacheService {

    private final DBService dbService;
    private final DataAnalysisService dataAnalysisService;
    @Autowired
    private JdbcTemplate template;

    @Autowired
    public CacheService(DBService dbService, DataAnalysisService dataAnalysisService) {
        this.dbService = dbService;
        this.dataAnalysisService = dataAnalysisService;
    }

    /*
     * <p>Deletes all data from <i>cache</i>, <i>mapstatscache</i> and <i>timestatscache</i>.</p>
     * <p>Later there will be ability to delete data only from desired tables.</p>
     * */
    public void clearCache() {
        String sql = "DELETE FROM cache";
        template.execute(sql);
        sql = "DELETE FROM mapstatscache";
        template.execute(sql);
        sql = "DELETE FROM timestatscache";
        template.execute(sql);
    }

    /*
     * <p>Collects all needed data to write it into cache tables.</p>
     * <p>Later there will be ability to write data only in desired tables.</p>
     * */
    public void writeCache() {
        String sql;
        clearCache();
        // calling thread-separated caching methods at the beginning
        new MapStatsCaching(dataAnalysisService, template).start();
        new TimeStatsCaching(template).start();

        // servers stats cache
        for (int serverId : dbService.getGoodIds()) {
            String hostname = dbService.getHostnameByServerId(serverId);

            DataAnalysisModel peakNumbers = dataAnalysisService.peakOnlineStats(serverId);
            int maxPlayers = peakNumbers.getMaxPlayers();

            DataAnalysisModel averageNumbers = dataAnalysisService.averagePlayersPerDay(Integer.toString(serverId));
            float averageOnline = averageNumbers.getAveragePlayers();
            float maxDailyAverage = averageNumbers.getMaxDailyAverage();
            String maxDailyAverageDay = averageNumbers.getMaxDailyAverageDay();
            int maxPlayersInMaxDailyAverageDay = averageNumbers.getMaxPlayersInMaxDailyAverage();
            String maxPlayersInMaxDailyAverageDayMap = Arrays.toString(averageNumbers.getMaxPlayersInMaxDailyAverageMaps().toArray());

            DataAnalysisModel mapsNumbers = dataAnalysisService.mapsStats(serverId);
            String adminsFavouriteMap = mapsNumbers.getAdminFavouriteMap();
            int adminsFavMapRecords = mapsNumbers.getAdminsFavouriteMapStats().getRecords();
            float adminsFavMapAvgPlayers = mapsNumbers.getAdminsFavouriteMapStats().getAveragePlayers();
            String playersFavouriteMap = mapsNumbers.getMostPopularMap();
            int playersFavMapRecords = mapsNumbers.getMostPopularMapStats().getRecords();
            float playersFavMapAvgPlayers = mapsNumbers.getMostPopularMapStats().getAveragePlayers();

            sql = String.format(Locale.US, "INSERT INTO cache(server_id, hostname, peak_online, average_online, max_daily_average, max_daily_average_day, po_in_mda, po_in_mda_map," +
                            " admin_fav_map, admin_fav_map_records, admin_fav_map_online, players_fav_map, players_fav_map_records, players_fav_map_online, weekly_data)" +
                            " VALUES (%d,'%s',%d,%f,%f,'%s',%d,'%s','%s',%d,%f,'%s',%d,%f,'%s');",
                    serverId, hostname, maxPlayers, averageOnline, maxDailyAverage, maxDailyAverageDay, maxPlayersInMaxDailyAverageDay, maxPlayersInMaxDailyAverageDayMap, adminsFavouriteMap,
                    adminsFavMapRecords, adminsFavMapAvgPlayers, playersFavouriteMap, playersFavMapRecords, playersFavMapAvgPlayers, dataAnalysisService.getWeeklyData(serverId));

            template.execute(sql);
        }

        // and that ones are called at the bottom because they append to the rows created earlier
        new OnlinePerTimesCaching(template, dbService).start(); // while this one should probably go to top because it runs very slow
        new OnlinePerMapsCaching(dataAnalysisService, dbService, template).start();
    }

    /*
     * <p>Reads all data from <i>cache</i> table - all server common data.</p>
     *
     * @param serverId Server's ID
     * @return Data model class with peak online, average online, daily max average online, this daily max average
     *  online day, peak online in this day, map during this peak online record, most common map in server,
     * most common map in server average online, map with the biggest average online and its average online data.
     * */
    public DataAnalysisModel readCommonDataCache(int serverId) {
        DataAnalysisModel data = new DataAnalysisModel();

        String sql = String.format("SELECT * FROM cache WHERE server_id=%d;", serverId);
        Map<String, Object> row = template.queryForMap(sql);

        data.setMaxPlayers((int) row.get("peak_online"));
        data.setAveragePlayers((float) row.get("average_online"));
        data.setMaxDailyAverage((float) row.get("max_daily_average"));
        data.setMaxDailyAverageDay((String) row.get("max_daily_average_day"));
        data.setMaxPlayersInMaxDailyAverage((int) row.get("po_in_mda"));
        data.setMaxPlayersInMaxDailyAverageMaps(Arrays.stream(((String) row.get("po_in_mda_map")).replace("[", "").replace("]", "").split(", ")).toList());
        data.setAdminFavouriteMap((String) row.get("admin_fav_map"));
        data.setAdminsFavouriteMapStats(new MapsDataModel((float) row.get("admin_fav_map_online"), (int) row.get("admin_fav_map_records")));
        data.setMostPopularMap((String) row.get("players_fav_map"));
        data.setMostPopularMapStats(new MapsDataModel((float) row.get("players_fav_map_online"), (int) row.get("players_fav_map_records")));

        return data;
    }

    /*
     * <p>Reads data from <i>mapstatscache</i> table.</p>
     * @return List<OnlinePerMap> cachedData - records list, where every record contains name, average online and
     * records amount for every map ever recorded in database.
     * */
    public List<OnlinePerMap> readMapCache() {
        List<OnlinePerMap> cachedData = new ArrayList<>();
        String sql = "SELECT * FROM mapstatscache;";
        List<Map<String, Object>> rows = template.queryForList(sql);

        for (Map<String, Object> row : rows) {
            cachedData.add(new OnlinePerMap((String) row.get("map"), (Float) row.get("average_online"), (Integer) row.get("records")));
        }

        return cachedData;
    }

    /*
     * <p>Reads data from <i>timestatscache</i> table.</p>
     * @return List<OnlinePerTime> - records list, where every record contains name and average online
     * for every timestamp gapped by five minutes (00:00, 00:05, 00:10 etc.).
     * */
    public List<OnlinePerTime> readTimeCache() {
        List<OnlinePerTime> cachedData = new ArrayList<>();
        String sql = "SELECT * FROM timestatscache;";
        List<Map<String, Object>> rows = template.queryForList(sql);

        for (Map<String, Object> row : rows) {
            cachedData.add(new OnlinePerTime((String) row.get("time"), (Float) row.get("average_online")));
        }

        return cachedData;
    }

    /*
     * <p>Reads average online per times data from <i>cache</i> table.</p>
     * @param serverId Server's ID
     * @return List<OnlinePerTime> - records list, where every record contains name and average online
     * for every timestamp gapped by five minutes (00:00, 00:05, 00:10 etc.) on particular server.
     * */
    public List<OnlinePerTime> readAverageOnlinesPerTimesCache(String serverId) {
        String sql = "SELECT average_onlines_per_times FROM cache WHERE server_id=" + serverId + ";";
        String json = (template.queryForMap(sql).get("average_onlines_per_times")).toString();
        return new Gson().fromJson(json, new TypeToken<List<OnlinePerTime>>() {
        }.getType());
    }

    /*
     * <p>Reads average online per maps data from <i>cache</i> table.</p>
     * @param serverId Server's ID
     * @return List<OnlinePerMap> - records list, where every record contains name, average online and
     * records amount for every map ever recorded in database on particular server.
     * */
    public List<OnlinePerMap> readAverageOnlinesPerMapsCache(String serverId) {
        String sql = "SELECT average_onlines_per_maps FROM cache WHERE server_id=" + serverId + ";";
        String json = (template.queryForMap(sql).get("average_onlines_per_maps")).toString();
        return new Gson().fromJson(json, new TypeToken<List<OnlinePerMap>>() {
        }.getType());
    }

    /*
     * <p>Reads weekly data from <i>cache</i> table.</p>
     * @param serverId Server's ID
     * @return List<WeeklyDataModel> - data model list with average online, records amount and peak online
     * data from every day in the week for a particular server.
     * */
    public List<WeeklyDataModel> readWeeklyDataCache(int serverId) {
        String sql = "SELECT weekly_data FROM cache WHERE server_id=" + serverId + ";";
        String json = (template.queryForMap(sql).get("weekly_data")).toString();
        return new Gson().fromJson(json, new TypeToken<List<WeeklyDataModel>>() {
        }.getType());
    }
}












