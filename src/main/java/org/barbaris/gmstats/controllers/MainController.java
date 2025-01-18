package org.barbaris.gmstats.controllers;

import org.barbaris.gmstats.models.*;
import org.barbaris.gmstats.services.CacheService;
import org.barbaris.gmstats.services.DBService;
import org.barbaris.gmstats.services.DataAnalysisService;
import org.barbaris.gmstats.services.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {

    private final DBService dbService;
    private final DataAnalysisService dataAnalysisService;
    private final CacheService cache;
    private final Utils utils;

    @Autowired
    public MainController(DBService service, DataAnalysisService dataAnalysisService, CacheService cache, Utils utils) {
        this.dbService = service;
        this.dataAnalysisService = dataAnalysisService;
        this.cache = cache;
        this.utils = utils;
    }

    @GetMapping("/")
    public String index(Model model) {
        ArrayList<ServerModel> servers = dataAnalysisService.popularServers(false);
        model.addAttribute("servers", servers);
        return "index";
    }

    // -----

    @GetMapping("/data")
    public String serverData(@RequestParam("id") int serverId, Model model) {
        String hostname = dbService.getHostnameByServerId(serverId);
        String address = dbService.getAddressById(serverId);

        if(hostname.equals(Values.BAD_ID)) return "bad_id";
        model.addAttribute("hostname", hostname);
        model.addAttribute("address", address);

        DataAnalysisModel data = cache.readCommonDataCache(serverId);

        // peak numbers stats
        model.addAttribute("maxPlayers", data.getMaxPlayers());

        // avg numbers stats
        model.addAttribute("averagePlayers", data.getAveragePlayers());
        model.addAttribute("maxDailyAverage", data.getMaxDailyAverage());
        model.addAttribute("maxDailyAverageDay", data.getMaxDailyAverageDay());
        model.addAttribute("maxPlayersInMaxDailyAverage", data.getMaxPlayersInMaxDailyAverage());
        model.addAttribute("maxPlayersInMaxDailyAverageMaps", data.getMaxPlayersInMaxDailyAverageMaps());

        // map stats
        model.addAttribute("adminsFavouriteMap", data.getAdminFavouriteMap());
        model.addAttribute("adminsFavouriteMapLink", Values.STEAM_WORKSHOP_LINK + data.getAdminFavouriteMap());
        model.addAttribute("adminsFavMapRecords", data.getAdminsFavouriteMapStats().getRecords());
        model.addAttribute("adminsFavMapAvgOnline", data.getAdminsFavouriteMapStats().getAveragePlayers());
        model.addAttribute("adminsFavMapPercentage", data.getAdminsFavouriteMapStats().getRecords() / (float) dbService.getServerRecordsAmount(serverId) * 100);
        model.addAttribute("adminsFavouriteMapTime", utils.recordsToTime(data.getAdminsFavouriteMapStats().getRecords()));

        model.addAttribute("playersFavMap", data.getMostPopularMap());
        model.addAttribute("playersFavMapLink", Values.STEAM_WORKSHOP_LINK + data.getMostPopularMap());
        model.addAttribute("playersFavMapRecords", data.getMostPopularMapStats().getRecords());
        model.addAttribute("playersFavMapAvgOnline", data.getMostPopularMapStats().getAveragePlayers());
        model.addAttribute("playersFavMapPercentage", data.getMostPopularMapStats().getRecords() / (float) dbService.getServerRecordsAmount(serverId) * 100);
        model.addAttribute("playersFavMapTime", utils.recordsToTime(data.getMostPopularMapStats().getRecords()));

        // weekly data
        List<WeeklyDataModel> weeklyData = cache.readWeeklyDataCache(serverId);
        for(int i = 0; i < 7; i++) {
            model.addAttribute(Values.WEEKLY_DATA_MODEL_NAMES[i], weeklyData.get(i));
        }

        return "server_info";
    }

    @GetMapping(value = "/graph", produces = "application/json")
    public ResponseEntity<?> graphData(@RequestParam("id") String serverId) {
        Timestamp start = dbService.serverEdgeTime(serverId, true);
        Timestamp stop = dbService.serverEdgeTime(serverId, false);
        List<GraphDataModel> data = dataAnalysisService.getGraphData(serverId, dataAnalysisService.averagePlayersPerDay(serverId).getMaxDailyAverage(), start, stop);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    // -----

    @GetMapping("details")
    public String detailedData(@RequestParam("id") int serverId, @RequestParam("date") String date, Model model) {
        model.addAttribute("hostname", dbService.getHostnameByServerId(serverId));
        model.addAttribute("date", date);
        return "details";
    }

    @GetMapping(value = "/graphdaily", produces = "application/json")
    public ResponseEntity<?> graphDailyData(@RequestParam("id") int serverId, @RequestParam("date") String date) {
        List<GraphDataModel> stats = dbService.getDailyData(serverId, date);
        int maxOnline = dbService.getMaxDailyOnline(serverId, date);
        Object[] dto = new Object[] { maxOnline, stats };
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    // -----

    @GetMapping("/update")
    public String writeCache() {
        cache.writeCache();
        return "updated";
    }

    // -----

    @GetMapping("/compare")
    public String compare(Model model) {
        List<ServerModel> servers = dbService.getGoodServers();
        model.addAttribute("servers", servers);

        return "compare";
    }

    @GetMapping("/comparedata")
    public ResponseEntity<?> graphCompareData(@RequestParam("servers") String servers) {
        String[] serversArray = servers.split(";");

        List<Timestamp> startTimes = new ArrayList<>();
        List<Timestamp> stopTimes = new ArrayList<>();

        for(String server : serversArray) {
            startTimes.add(dbService.serverEdgeTime(server, true));
            stopTimes.add(dbService.serverEdgeTime(server, false));
        }

        Timestamp start = utils.findLatest(startTimes);
        Timestamp stop = utils.findEarliest(stopTimes);

        List<List<GraphDataModel>> data = new ArrayList<>();
        for(String server : serversArray) {
            data.add(dataAnalysisService.getGraphData(server, dataAnalysisService.averagePlayersPerDay(server).getMaxDailyAverage(), start, stop));
        }

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    // -------

    @GetMapping("/statistics")
    public String statistics(Model model) {
        AggregatedStatsModel stats = dataAnalysisService.aggregatedStatistics(false);
        List<OnlinePerMap> mapsStats = stats.getOnlinesPerMaps();
        model.addAttribute("maps", mapsStats);
        return "statistics";
    }

    @GetMapping("/timestatsdata")
    public ResponseEntity<?> timesStatisticsData(@RequestParam(value = "id", required = false) String serverId) {
        List<OnlinePerTime> timesStats;

        if(serverId != null) {
            timesStats = cache.readAverageOnlinesPerTimesCache(serverId);
        } else {
            AggregatedStatsModel stats = dataAnalysisService.aggregatedStatistics(false);
            timesStats = stats.getOnlinesPerTimes();
        }

        return new ResponseEntity<>(timesStats, HttpStatus.OK);
    }

    @GetMapping("/mapstatsdata")
    public ResponseEntity<?> mapsStatisticsData(@RequestParam(value = "id", required = false) String serverId) {
        List<OnlinePerMap> mapsStats;

        if(serverId != null) {
            mapsStats = cache.readAverageOnlinesPerMapsCache(serverId);
            mapsStats = utils.sortOpm(mapsStats);
        } else {
            AggregatedStatsModel stats = dataAnalysisService.aggregatedStatistics(false);
            mapsStats = stats.getOnlinesPerMaps();
        }

        return new ResponseEntity<>(mapsStats, HttpStatus.OK);
    }
}






























