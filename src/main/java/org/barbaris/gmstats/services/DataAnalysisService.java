package org.barbaris.gmstats.services;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.barbaris.gmstats.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataAnalysisService {

    private final Utils utils;
    private final CacheService cache;
    private final DBService dbService;
    @Autowired
    private JdbcTemplate template;

    @Autowired
    public DataAnalysisService(Utils utils, @Lazy CacheService cache, DBService dbService) {
        this.dbService = dbService;
        this.cache = cache;
        this.utils = utils;
    }

    /*
     * @return all server data related to average numbers computing
     * */
    public DataAnalysisModel averagePlayersPerDay(String serverId) {

        String sql = "SELECT online, time FROM statistics WHERE server_id=" + serverId + " ORDER BY id;";
        List<Map<String, Object>> rows = template.queryForList(sql);

        sql = "SELECT COUNT(*) FROM statistics WHERE server_id=" + serverId + ";";
        Long statsCount = (Long) template.queryForMap(sql).get("count");


        // ---------------- CALCULATING ALL AVERAGES -----------------------


        Timestamp currentDay = (Timestamp) rows.getFirst().get("time");
        int currentDayPlayers = 0;

        int sumPlayers = 0;

        float maxDailyAverage = 0f;
        Timestamp maxDailyAverageDay = currentDay;

        for (Map<String, Object> row : rows) {
            sumPlayers += (int) row.get("online");

            Timestamp recordTime = (Timestamp) row.get("time");
            if ((recordTime.toLocalDateTime().truncatedTo(ChronoUnit.DAYS).equals(currentDay.toLocalDateTime().truncatedTo(ChronoUnit.DAYS)))) {
                currentDayPlayers += (int) row.get("online");
            } else {
                float dailyAverage = currentDayPlayers / Values.RECORDS_A_DAY;

                if (dailyAverage > maxDailyAverage) {
                    maxDailyAverage = dailyAverage;
                    maxDailyAverageDay = Timestamp.valueOf(recordTime.toLocalDateTime().minusDays(1));
                }

                currentDayPlayers = (int) row.get("online");
                currentDay = recordTime;
            }
        }

        sql = String.format("SELECT MAX(online) FROM statistics WHERE server_id=%s AND time >= timestamp '%s' AND time < timestamp '%s';",
                serverId,
                maxDailyAverageDay,
                Timestamp.valueOf(maxDailyAverageDay.toLocalDateTime().plusDays(1)));

        Map<String, Object> callback = template.queryForMap(sql);
        int maxPlayersInMaxDailyAverage = callback.get("max") != null ? (int) callback.get("max") : 0;

        // -------------- ASSEMBLING DATA TRANSFER OBJECT ----------------
        DataAnalysisModel data = new DataAnalysisModel();

        data.setAveragePlayers((float) sumPlayers / statsCount);

        data.setMaxDailyAverage(maxDailyAverage);
        data.setMaxDailyAverageDay(maxDailyAverageDay.toString().substring(0, 10));

        data.setMaxPlayersInMaxDailyAverage(maxPlayersInMaxDailyAverage);
        //map when peak online in this day happened
        data.setMaxPlayersInMaxDailyAverageMaps(getMaps(serverId, maxPlayersInMaxDailyAverage, maxDailyAverageDay, Timestamp.valueOf(maxDailyAverageDay.toLocalDateTime().plusDays(1))));
        return data;
    }


    /*
     * @return list of servers sorted by average online
     *  */
    public ArrayList<ServerModel> popularServers(boolean reversed) {
        String sql = "SELECT server_id, hostname, average_online FROM cache;";
        ArrayList<ServerModel> servers = new ArrayList<>();
        List<Map<String, Object>> rows = template.queryForList(sql);

        for (Map<String, Object> row : rows) {
            ServerModel server = new ServerModel();
            server.setMid((Integer) row.get("server_id"));
            server.setHostname((String) row.get("hostname"));
            server.setPopularityRatio((float) row.get("average_online"));
            servers.add(server);
        }

        Comparator<ServerModel> compareByPopularityRatio = Comparator.comparing(ServerModel::getPopularityRatio).thenComparing(ServerModel::getHostname);

        if (!reversed) compareByPopularityRatio = compareByPopularityRatio.reversed();

        return servers.stream().sorted(compareByPopularityRatio).collect(Collectors.toCollection(ArrayList::new));
    }


    /*
     * @return admins favourite and players favourite maps stats
     *  */
    public DataAnalysisModel mapsStats(int serverId) {

        String sql = String.format("SELECT map FROM statistics WHERE server_id=%d;", serverId);
        List<Map<String, Object>> rows = template.queryForList(sql);
        List<String> mapNames = new ArrayList<>();
        List<Integer> mapPlays = new ArrayList<>();

        for (Map<String, Object> row : rows) {

            String mapName = (String) row.get("map");
            if (!mapNames.contains(mapName)) {
                mapNames.add(mapName);
                mapPlays.add(1);
            } else {
                mapPlays.set(mapNames.indexOf(mapName), mapPlays.get(mapNames.indexOf(mapName)) + 1);
            }
        }

        float maxPlayersMultiplier = 0f;
        String playersFavMap = null;

        for (String mapName : mapNames) {
            if (mapPlays.get(mapNames.indexOf(mapName)) > 20) {
                sql = String.format("SELECT online FROM statistics WHERE server_id=%d AND map='%s';", serverId, mapName);
                rows = template.queryForList(sql);

                float playersMultiplier = 0f;
                for (Map<String, Object> row : rows) {
                    playersMultiplier += (int) row.get("online");
                }

                playersMultiplier /= mapPlays.get(mapNames.indexOf(mapName));
                if (playersMultiplier > maxPlayersMultiplier) {
                    maxPlayersMultiplier = playersMultiplier;
                    playersFavMap = mapName;
                }
            }
        }

        // counting average players number in admins fav map
        sql = String.format("SELECT online FROM statistics WHERE server_id=%d AND map='%s';", serverId, mapNames.get(utils.maxNumberIndex(mapPlays)));
        rows = template.queryForList(sql);

        int playersSum = 0;
        for (Map<String, Object> row : rows) {
            playersSum += (int) row.get("online");
        }

        // setting up DTO
        DataAnalysisModel data = new DataAnalysisModel();
        data.setAdminFavouriteMap(mapNames.get(utils.maxNumberIndex(mapPlays)));
        data.setAdminsFavouriteMapStats(new MapsDataModel(((float) playersSum / mapPlays.get(utils.maxNumberIndex(mapPlays))), mapPlays.get(utils.maxNumberIndex(mapPlays))));
        data.setMostPopularMap(playersFavMap);

        if (!mapNames.contains(playersFavMap)) {
            data.setMostPopularMapStats(new MapsDataModel(maxPlayersMultiplier, mapPlays.getFirst()));
        } else {
            data.setMostPopularMapStats(new MapsDataModel(maxPlayersMultiplier, mapPlays.get(mapNames.indexOf(playersFavMap))));
        }

        return data;
    }


    /*
     * @return peak online data like max players online and timestamps w/ map
     *  */
    public DataAnalysisModel peakOnlineStats(int serverId) {
        String sql = String.format("SELECT MAX(online) FROM statistics WHERE server_id=%d;", serverId);
        int maxPlayers = (int) template.queryForMap(sql).get("max");

        sql = String.format("SELECT map, time FROM statistics WHERE server_id=%d AND online=%d;", serverId, maxPlayers);
        List<Map<String, Object>> rows = template.queryForList(sql);
        List<StatsModel> records = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            StatsModel record = new StatsModel();
            record.setTime((Timestamp) row.get("time"));
            record.setMap((String) row.get("map"));
            records.add(record);
        }

        DataAnalysisModel data = new DataAnalysisModel();
        data.setMaxPlayers(maxPlayers);
        data.setMaxPlayersRecords(records);
        return data;
    }


    /*
     * @return data for graph presentation
     * */
    public List<GraphDataModel> getGraphData(String serverId, float maxAverageOnline, Timestamp start, Timestamp stop) {
        String sql = String.format("SELECT time, online FROM statistics WHERE server_id=%s AND time >= timestamp '%s' AND time < timestamp '%s' ORDER BY id;",
                serverId, start, stop);
        ArrayList<GraphDataModel> data = new ArrayList<>();

        LocalDateTime serverStartLDT = dbService.serverEdgeTime(serverId, true).toLocalDateTime().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime recordsStartLDT = start.toLocalDateTime().truncatedTo(ChronoUnit.DAYS);

        if ((!serverStartLDT.equals(recordsStartLDT)) && maxAverageOnline == 0) {
            long gap = (Timestamp.valueOf(serverStartLDT).getTime() - Timestamp.valueOf(recordsStartLDT).getTime()) / Values.MILLISECONDS_A_DAY;
            for (int i = 0; i < gap; i++) {
                LocalDateTime day = recordsStartLDT.plusDays(i);

                GraphDataModel graphData = new GraphDataModel();
                graphData.setYear(String.valueOf(((day.getYear()))));
                graphData.setMonth(day.getMonthValue() < 10 ? "0" + day.getMonthValue() : String.valueOf(day.getMonthValue()));
                graphData.setDay(day.getDayOfMonth() < 10 ? "0" + day.getDayOfMonth() : String.valueOf(day.getDayOfMonth()));
                graphData.setOnline(0);

                data.add(graphData);
            }
        }

        List<Map<String, Object>> rows = template.queryForList(sql);

        int onlineSum = 0;
        LocalDateTime day = ((Timestamp) rows.getFirst().get("time")).toLocalDateTime().truncatedTo(ChronoUnit.DAYS);

        for (Map<String, Object> row : rows) {
            LocalDateTime time = ((Timestamp) row.get("time")).toLocalDateTime().truncatedTo(ChronoUnit.DAYS);

            if (day.equals(time)) {
                onlineSum += (int) row.get("online");
            } else {
                GraphDataModel graphData = new GraphDataModel();
                graphData.setYear(String.valueOf(((day.getYear()))));
                graphData.setMonth(day.getMonthValue() < 10 ? "0" + day.getMonthValue() : String.valueOf(day.getMonthValue()));
                graphData.setDay(day.getDayOfMonth() < 10 ? "0" + day.getDayOfMonth() : String.valueOf(day.getDayOfMonth()));
                graphData.setOnline(onlineSum / Values.RECORDS_A_DAY);
                graphData.setCompareOnline(maxAverageOnline);

                data.add(graphData);

                day = time;
                onlineSum = (int) row.get("online");
            }
        }

        return data;
    }


    /*
     * @return aggregated statistics about all the servers
     * */
    public AggregatedStatsModel aggregatedStatistics(boolean isTimeSorted) {
        AggregatedStatsModel stats = new AggregatedStatsModel();

        List<OnlinePerMap> onlinePerMaps = cache.readMapCache();
        onlinePerMaps = utils.sortOpm(onlinePerMaps);
        List<OnlinePerTime> onlinePerTimes = cache.readTimeCache();

        if (isTimeSorted) onlinePerTimes = utils.sortOpt(onlinePerTimes);

        stats.setOnlinesPerMaps(onlinePerMaps);
        stats.setOnlinesPerTimes(onlinePerTimes);

        return stats;
    }


    /*
     * @return json data of average onlines, max onlines and records amount about particular server or all servers
     * */
    public String getWeeklyData(int serverId) {
        String sql;
        if (serverId == -1) {
            sql = "SELECT time, players FROM statistics;";
        } else {
            sql = String.format("SELECT time, online FROM statistics WHERE server_id=%d;", serverId);
        }

        List<Map<String, Object>> rows = template.queryForList(sql);

        int[] maxes = {0, 0, 0, 0, 0, 0, 0};
        int[] onlines = {0, 0, 0, 0, 0, 0, 0};
        float[] records = {0f, 0f, 0f, 0f, 0f, 0f, 0f};
        for (Map<String, Object> row : rows) {
            Timestamp timestamp = (Timestamp) row.get("time");
            int players = (Integer) row.get("online");
            LocalDateTime time = timestamp.toLocalDateTime();

            switch (time.getDayOfWeek()) {
                case MONDAY:
                    if (players > maxes[0]) maxes[0] = players;
                    onlines[0] += players;
                    records[0]++;
                    break;
                case TUESDAY:
                    if (players > maxes[1]) maxes[1] = players;
                    onlines[1] += players;
                    records[1]++;
                    break;
                case WEDNESDAY:
                    if (players > maxes[2]) maxes[2] = players;
                    onlines[2] += players;
                    records[2]++;
                    break;
                case THURSDAY:
                    if (players > maxes[3]) maxes[3] = players;
                    onlines[3] += players;
                    records[3]++;
                    break;
                case FRIDAY:
                    if (players > maxes[4]) maxes[4] = players;
                    onlines[4] += players;
                    records[4]++;
                    break;
                case SATURDAY:
                    if (players > maxes[5]) maxes[5] = players;
                    onlines[5] += players;
                    records[5]++;
                    break;
                case SUNDAY:
                    if (players > maxes[6]) maxes[6] = players;
                    onlines[6] += players;
                    records[6]++;
                    break;
            }
        }

        List<WeeklyDataModel> data = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            WeeklyDataModel dayData = new WeeklyDataModel();

            try {
                dayData.setDay(DayOfWeek.of(i + 1).toString());

                if (Double.isNaN(onlines[i] / records[i])) {
                    dayData.setAverageOnline(0);
                } else {
                    dayData.setAverageOnline(onlines[i] / records[i]);
                }

                dayData.setRecords((int) records[i]);
                dayData.setMaxOnline(maxes[i]);
            } catch (Exception ex) {
                dayData.setDay(DayOfWeek.of(i + 1).toString());
                dayData.setAverageOnline(0);
                dayData.setRecords(0);
                dayData.setMaxOnline(0);
            }

            data.add(dayData);
        }

        return new Gson().toJson(data);
    }


    // ------------------ AUXILIARY METHODS --------------

    // @return list of non-duplicate maps by server_id, players recorded and time period if needed
    public List<String> getMaps(String serverId, int players, Timestamp start, Timestamp finish) {

        String sql;
        if (serverId == null) {
            sql = "SELECT map FROM statistics;";
        } else if ((start == null) || (finish == null)) {
            sql = String.format("SELECT map FROM statistics WHERE server_id=%s AND online=%d;",
                    serverId, players);
        } else {
            sql = String.format("SELECT map FROM statistics WHERE server_id=%s AND online=%d AND time >= timestamp '%s' AND time < timestamp '%s';",
                    serverId, players, start, finish);
        }

        List<Map<String, Object>> rows = template.queryForList(sql);
        List<String> maps = new ArrayList<>();  // list with non-duplicate maps

        for (Map<String, Object> row : rows) {
            String map = (String) row.get("map");

            if (!maps.contains(map)) maps.add(map);  // preventing duplicate records summoning
        }
        return maps;
    }
}





























