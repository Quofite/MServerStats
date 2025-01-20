package org.barbaris.gmstats.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.barbaris.gmstats.models.GraphDataModel;
import org.barbaris.gmstats.models.InstantDataModel;
import org.barbaris.gmstats.models.ServerModel;
import org.barbaris.gmstats.models.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DBService {

    private static final Logger log = LogManager.getLogger(DBService.class);
    @Autowired
    private JdbcTemplate template;
    @Autowired
    private Utils utils;

    public String getHostnameByServerId(int serverId) {
        String sql = "SELECT hostname FROM goodservers WHERE mid=" + serverId + ";";
        try {
            Map<String, Object> hostname = template.queryForMap(sql);
            return (String) hostname.get("hostname");
        } catch (Exception ex) {
            return Values.BAD_ID;
        }
    }

    public String getAddressById(int serverId) {
        String sql = "SELECT (ip, port) FROM goodservers WHERE mid=" + serverId + ";";

        try {
            Map<String, Object> address = template.queryForMap(sql);
            return address.get("row").toString().replace("(", "").replace(")", "").replace(",", ":");
        } catch (Exception ex) {
            return Values.DEFAULT_ADDRESS;
        }
    }

    // returns GraphDataModel objects list filled with record time, online players amount and current map at given day
    public List<GraphDataModel> getDailyData(String serverId, String dateString) {

        // this two lines creates data extraction time bounds - from `date` to `nextDate`
        Timestamp date = utils.stringToTimestamp(dateString, 0);
        Timestamp nextDate = utils.stringToTimestamp(dateString, 1);

        String sql = String.format("SELECT * FROM statistics WHERE server_id=%s AND time >= timestamp '%s' AND time < timestamp '%s' ORDER BY id;",
                serverId, date, nextDate);
        List<Map<String, Object>> rows = template.queryForList(sql);

        List<GraphDataModel> stats = new ArrayList<>();
        for(Map<String, Object> row : rows) {
            GraphDataModel stat = new GraphDataModel();
            stat.setOnline(Float.parseFloat(String.valueOf(row.get("players"))));
            stat.setMap((String) row.get("map"));

            LocalDateTime timeLDT = ( (Timestamp) row.get("time") ).toLocalDateTime();
            stat.setHour(timeLDT.getHour() < 10 ?  "0" + timeLDT.getHour() : String.valueOf(timeLDT.getHour()));
            stat.setMinute(timeLDT.getMinute() < 10 ?  "0" + timeLDT.getMinute() : String.valueOf(timeLDT.getMinute()));
            stats.add(stat);
        }

        return stats;
    }

    public int getMaxDailyOnline(String serverId, String dateString) {
        // same as in getDailyData(..)
        Timestamp date = utils.stringToTimestamp(dateString, 0);
        Timestamp nextDay = utils.stringToTimestamp(dateString, 1);

        String sql = String.format("SELECT MAX(players) FROM statistics WHERE server_id=%s AND time >= timestamp '%s' AND time < timestamp '%s';",
                serverId, date, nextDay);

        return (int) template.queryForMap(sql).get("max");
    }

    public long getServerRecordsAmount(int serverId) {
        String sql = String.format("SELECT COUNT(*) FROM statistics WHERE server_id=%d;", serverId);
        Map<String, Object> row = template.queryForMap(sql);
        return (long) row.get("count");
    }

    // first=true -> first record timestamp
    // first=false -> last record timestamp
    public Timestamp serverEdgeTime(String serverId, boolean first) {
        String sql = String.format("SELECT time FROM statistics WHERE server_id=%s ORDER BY id", serverId);

        if(first) {
            sql += " LIMIT 1;";
        } else {
            sql += " DESC LIMIT 1";
        }

        Map<String, Object> row = template.queryForMap(sql);
        return (Timestamp) row.get("time");
    }

    public ArrayList<ServerModel> getGoodServers() {
        ArrayList<ServerModel> servers = new ArrayList<>();
        String sql = "SELECT * FROM goodservers;";
        List<Map<String, Object>> rows = template.queryForList(sql);

        for(Map<String, Object> row : rows) {
            ServerModel server = new ServerModel();
            server.setHostname((String) row.get("hostname"));
            server.setMid((Integer) row.get("mid"));
            servers.add(server);
        }

        return servers;
    }

    public InstantDataModel getInstantData(String serverId, String timestamp) {
        InstantDataModel data = new InstantDataModel();

        String sql = String.format("SELECT hostname, players, map FROM statistics WHERE server_id=%s AND to_char(time, 'DD.MM.YYYY HH24:MI')='%s';", serverId, timestamp);
        try {
            Map<String, Object> row = template.queryForMap(sql);

            data.setOnline((Integer) row.get("players"));
            data.setMap((String) row.get("map"));
            data.setTimestamp(timestamp);
            data.setHostname((String) row.get("hostname"));
        } catch (Exception e) {
            data.setHostname(Values.BAD_ID);
        }

        return data;
    }

    // --------------------- DATABASE MANIPULATING SCRIPTS


    public void initializeDatabase() {

        try(FileReader fr = new FileReader("/home/gleb/Coding/gm/createGoodServersTable.sql")) {
            StringBuilder sql = new StringBuilder();

            int c;
            while ((c = fr.read()) != -1) {
                sql.append((char) c);
            }

            template.execute(sql.toString());
        } catch (Exception ex) {
            log.error(String.valueOf(ex.getCause()));
        }

    }

    // This method fills new database table with non-empty and non-test servers
    public void regroupServers() {

        String sql = "SELECT * FROM servers;";
        List<Map<String, Object>> dbdata = template.queryForList(sql);
        List<ServerModel> servers = new ArrayList<>();
        List<ServerModel> nonEmptyServers = new ArrayList<>();


        for(Map<String, Object> data : dbdata) {

            ServerModel server = new ServerModel();
            server.setHostname((String) data.get("hostname"));
            server.setMid((Integer) data.get("mid"));

            if(server.getHostname().contains(" TEST ")) continue;
            if(utils.isBadId(server.getMid())) continue;

            server.setId((Integer) data.get("id"));
            server.setMap((String) data.get("map"));
            server.setIp((String) data.get("ip"));
            server.setPort((Integer) data.get("port"));
            server.setOwner((String) data.get("owner"));
            servers.add(server);
        }

        for (ServerModel server : servers) {
            sql = String.format("SELECT * FROM statistics WHERE server_id=%d;", server.getMid());
            List<Map<String, Object>> records = template.queryForList(sql);

            if(!records.isEmpty()) {
                for(Map<String, Object> record : records) {
                    if(record.get("players_json") != null) {
                        nonEmptyServers.add(server);
                        break;
                    }
                }
            }
        }

        for(ServerModel s : nonEmptyServers) {
            sql = String.format("INSERT INTO goodservers (id, mid, hostname, map, ip, port, owner) VALUES (%d, %d, '%s', '%s', '%s', %d, '%s');",
                    s.getId(), s.getMid(), s.getHostname(), s.getMap(), s.getIp(), s.getPort(), s.getOwner());
            template.execute(sql);
        }
    }

    // this method deletes unnecessary stats from table (empty/dev servers stats)
    public void deleteUnnecessaryStatistics() {
        ArrayList<Integer> ids = getGoodIds();

        String sql = "select distinct on(server_id) server_id from statistics;";
        List<Map<String, Object>> rows = template.queryForList(sql);
        for(Map<String, Object> row : rows) {
            int serverId = (Integer) row.get("server_id");

            if(!ids.contains(serverId)) {
                sql = String.format("DELETE FROM statistics WHERE server_id=%d;", serverId);
                template.execute(sql);
            }
        }

        sql = "SELECT COUNT(*) FROM statistics;";
        System.out.println(template.queryForMap(sql).get("count"));
    }

    // --------------------- HELPING METHODS
    // this method returns good servers ids list
    public ArrayList<Integer> getGoodIds() {
        ArrayList<Integer> goodIDs = new ArrayList<>();
        String sql = "SELECT mid FROM goodservers;";
        List<Map<String, Object>> goodServersIDs = template.queryForList(sql);

        for(Map<String, Object> serverID : goodServersIDs) {
            goodIDs.add((Integer) serverID.get("mid"));
        }

        return goodIDs;
    }

    public Timestamp recordingsEdgeTime(boolean first) {
        String sql;

        if(first) {
            sql = "SELECT time FROM statistics ORDER BY ID LIMIT 1;";
        } else {
            sql = "SELECT time FROM statistics ORDER BY ID DESC LIMIT 1;";
        }

        return (Timestamp) template.queryForMap(sql).get("time");
    }
}

























