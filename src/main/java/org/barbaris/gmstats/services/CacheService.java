package org.barbaris.gmstats.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.barbaris.gmstats.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CacheService {

    @Autowired
    private JdbcTemplate template;
    @Autowired
    private Utils utils;

    private final DBService dbService;
    private final DataAnalysisService dataAnalysisService;

    @Autowired
    public CacheService(DBService dbService, DataAnalysisService dataAnalysisService) {
        this.dbService = dbService;
        this.dataAnalysisService = dataAnalysisService;
    }

    public void clearCache() {
        String sql = "DELETE FROM cache";
        template.execute(sql);
        sql = "DELETE FROM mapstatscache";
        template.execute(sql);
        sql = "DELETE FROM timestatscache";
        template.execute(sql);
    }

    public void writeCache() {
        String sql;
        Gson gson = new Gson();
        clearCache();

        // servers stats cache
        for(int serverId : dbService.getGoodIds()) {
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

            List<OnlinePerTime> onlinePerTimes = new ArrayList<>();
            long players;
            float count;
            List<String> times = utils.generateTimes();
            for (String time : times) {
                try {
                    sql = String.format("SELECT SUM(players) FROM statistics WHERE to_char(time, 'HH24:MI')='%s' AND server_id=%d;", time, serverId);
                    players = (Long) template.queryForMap(sql).get("sum");

                    sql = String.format("SELECT COUNT(*) FROM statistics WHERE to_char(time, 'HH24:MI')='%s' AND server_id=%d;", time, serverId);
                    count = (Long) template.queryForMap(sql).get("count");

                    onlinePerTimes.add(new OnlinePerTime(time, (players / count)));
                } catch (NullPointerException ex) {
                    onlinePerTimes.add(new OnlinePerTime(time, 0));
                }
            }

            List<OnlinePerMap> onlinePerMaps = new ArrayList<>();
            List<String> maps = dataAnalysisService.getMaps(null, 0, null, null);
            for(String map : maps) {
                try {
                    sql = String.format("SELECT SUM(players) FROM statistics WHERE map='%s' AND server_id=%d;", map, serverId);
                    players = (Long) template.queryForMap(sql).get("sum");

                    sql = String.format("SELECT COUNT(*) FROM statistics WHERE map='%s' AND server_id=%d;", map, serverId);
                    count = (Long) template.queryForMap(sql).get("count");

                    if(count >= Values.RECORDS_A_DAY) {
                        onlinePerMaps.add(new OnlinePerMap(map, (players / count), Math.round(count)));
                    }
                } catch (Exception ignored) {

                }
            }

            sql = String.format(Locale.US,"INSERT INTO cache(server_id, hostname, peak_online, average_online, max_daily_average, max_daily_average_day, po_in_mda, po_in_mda_map," +
                            " admin_fav_map, admin_fav_map_records, admin_fav_map_online, players_fav_map, players_fav_map_records, players_fav_map_online, average_onlines_per_times, average_onlines_per_maps, weekly_data)" +
                            " VALUES (%d,'%s',%d,%f,%f,'%s',%d,'%s','%s',%d,%f,'%s',%d,%f,'%s','%s', '%s');",
                    serverId, hostname, maxPlayers, averageOnline, maxDailyAverage, maxDailyAverageDay, maxPlayersInMaxDailyAverageDay, maxPlayersInMaxDailyAverageDayMap, adminsFavouriteMap,
                    adminsFavMapRecords, adminsFavMapAvgPlayers, playersFavouriteMap, playersFavMapRecords, playersFavMapAvgPlayers, gson.toJson(onlinePerTimes), gson.toJson(onlinePerMaps), dataAnalysisService.getWeeklyData(serverId));

            template.execute(sql);
        }

        // --- maps stats cache

        List<String> maps = dataAnalysisService.getMaps(null, 0, null, null);
        long players;
        float count;
        for(String map : maps) {
            sql = String.format("SELECT SUM(players) FROM statistics WHERE map='%s';", map);
            players = (Long) template.queryForMap(sql).get("sum");

            sql = String.format("SELECT COUNT(*) FROM statistics WHERE map='%s';", map);
            count = (Long) template.queryForMap(sql).get("count");

            if(count >= Values.RECORDS_A_DAY) {
                sql = String.format(Locale.US, "INSERT INTO mapstatscache(map, average_online, records) VALUES ('%s', %f, %d);", map, (players / count), Math.round(count));
                template.execute(sql);
            }
        }

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

    public List<OnlinePerMap> readMapCache() {
        List<OnlinePerMap> cachedData = new ArrayList<>();
        String sql = "SELECT * FROM mapstatscache;";
        List<Map<String, Object>> rows = template.queryForList(sql);

        for(Map<String, Object> row : rows) {
            cachedData.add(new OnlinePerMap((String) row.get("map"), (Float) row.get("average_online"), (Integer) row.get("records")));
        }

        return cachedData;
    }

    public List<OnlinePerTime> readTimeCache() {
        List<OnlinePerTime> cachedData = new ArrayList<>();
        String sql = "SELECT * FROM timestatscache;";
        List<Map<String, Object>> rows = template.queryForList(sql);

        for(Map<String, Object> row : rows) {
            cachedData.add(new OnlinePerTime((String) row.get("time"), (Float) row.get("average_online")));
        }

        return cachedData;
    }

    public List<OnlinePerTime> readAverageOnlinesPerTimesCache(String serverId) {
        String sql = "SELECT average_onlines_per_times FROM cache WHERE server_id=" + serverId + ";";
        String json = (template.queryForMap(sql).get("average_onlines_per_times")).toString();
        return new Gson().fromJson(json, new TypeToken<List<OnlinePerTime>>(){}.getType());
    }

    public List<OnlinePerMap> readAverageOnlinesPerMapsCache(String serverId) {
        String sql = "SELECT average_onlines_per_maps FROM cache WHERE server_id=" + serverId + ";";
        String json = (template.queryForMap(sql).get("average_onlines_per_maps")).toString();
        return new Gson().fromJson(json, new TypeToken<List<OnlinePerMap>>(){}.getType());
    }

    public List<WeeklyDataModel> readWeeklyDataCache(int serverId) {
        String sql = "SELECT weekly_data FROM cache WHERE server_id=" + serverId + ";";
        String json = (template.queryForMap(sql).get("weekly_data")).toString();
        return new Gson().fromJson(json, new TypeToken<List<WeeklyDataModel>>(){}.getType());
    }
}












