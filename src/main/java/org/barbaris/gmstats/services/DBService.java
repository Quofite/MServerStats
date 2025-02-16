package org.barbaris.gmstats.services;

import java.io.FileReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.barbaris.gmstats.models.GraphDataModel;
import org.barbaris.gmstats.models.InstantDataModel;
import org.barbaris.gmstats.models.ServerModel;
import org.barbaris.gmstats.models.Values;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DBService {
    private final Utils utils;
    @Autowired
    private JdbcTemplate template;

    @Autowired
    public DBService(Utils utils) {
        this.utils = utils;
    }

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

    // @return GraphDataModel objects list filled with record time, online players amount and current map at given day
    public List<GraphDataModel> getDailyData(String serverId, String dateString) {

        // this two lines creates data extraction time bounds - from `date` to `nextDate`
        Timestamp date = utils.stringToTimestamp(dateString, 0);
        Timestamp nextDate = utils.stringToTimestamp(dateString, 1);

        String sql = String.format("SELECT online, map, time FROM statistics WHERE server_id=%s AND time >= timestamp '%s' AND time < timestamp '%s' ORDER BY id;",
                serverId, date, nextDate);
        List<Map<String, Object>> rows = template.queryForList(sql);

        List<GraphDataModel> stats = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            GraphDataModel stat = new GraphDataModel();
            stat.setOnline(Float.parseFloat(String.valueOf(row.get("online"))));
            stat.setMap((String) row.get("map"));

            LocalDateTime timeLDT = ((Timestamp) row.get("time")).toLocalDateTime();
            stat.setHour(timeLDT.getHour() < 10 ? "0" + timeLDT.getHour() : String.valueOf(timeLDT.getHour()));
            stat.setMinute(timeLDT.getMinute() < 10 ? "0" + timeLDT.getMinute() : String.valueOf(timeLDT.getMinute()));
            stats.add(stat);
        }

        return stats;
    }

    public int getMaxDailyOnline(String serverId, String dateString) {
        // same as in getDailyData(..)
        Timestamp date = utils.stringToTimestamp(dateString, 0);
        Timestamp nextDay = utils.stringToTimestamp(dateString, 1);

        String sql = String.format("SELECT MAX(online) FROM statistics WHERE server_id=%s AND time >= timestamp '%s' AND time < timestamp '%s';",
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
        String sql = String.format("SELECT time FROM statistics WHERE server_id=%s ORDER BY time", serverId);

        if (first) {
            sql += " LIMIT 1;";
        } else {
            sql += " DESC LIMIT 1";
        }

        Map<String, Object> row = template.queryForMap(sql);
        return (Timestamp) row.get("time");
    }

    public ArrayList<ServerModel> getGoodServers() {
        ArrayList<ServerModel> servers = new ArrayList<>();
        String sql = "SELECT hostname, mid FROM goodservers;";
        List<Map<String, Object>> rows = template.queryForList(sql);

        for (Map<String, Object> row : rows) {
            ServerModel server = new ServerModel();
            server.setHostname((String) row.get("hostname"));
            server.setMid((Integer) row.get("mid"));
            servers.add(server);
        }

        return servers;
    }

    // @return server data such as online and map in particular timestamp
    public InstantDataModel getInstantData(String serverId, String timestamp) {
        InstantDataModel data = new InstantDataModel();

        String sql = String.format("SELECT hostname, online, map FROM statistics WHERE server_id=%s AND to_char(time, 'DD.MM.YYYY HH24:MI')='%s';", serverId, timestamp);
        try {
            Map<String, Object> row = template.queryForMap(sql);

            data.setOnline((Integer) row.get("online"));
            data.setMap((String) row.get("map"));
            data.setTimestamp(timestamp);
            data.setHostname((String) row.get("hostname"));
        } catch (Exception e) {
            data.setHostname(Values.BAD_ID);
        }

        return data;
    }

    // --------------------- DATABASE MANIPULATING SCRIPTS (these scripts are to be re-written in Go and moved to small rarely-used microservice)

    public void initializeDatabase() {
        template.execute("DROP TABLE goodservers;");

        try (FileReader fr = new FileReader("/home/gleb/Coding/gm/createGoodServersTable.sql")) {
            StringBuilder sql = new StringBuilder();

            int c;
            while ((c = fr.read()) != -1) {
                sql.append((char) c);
            }

            template.execute(sql.toString());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    // This method fills new database table with non-empty and non-test servers
    public void regroupServers() {
        String sql = "SELECT * FROM servers;";
        List<Map<String, Object>> dbdata = template.queryForList(sql);
        List<ServerModel> servers = new ArrayList<>();
        List<ServerModel> nonEmptyServers = new ArrayList<>();

        for (Map<String, Object> data : dbdata) {
            ServerModel server = new ServerModel();
            server.setHostname((String) data.get("hostname"));
            server.setMid((Integer) data.get("mid"));

            if (server.getHostname().contains(" TEST ")) continue;
            //if (utils.isBadId(server.getMid())) continue;

            server.setMap((String) data.get("map"));
            server.setIp((String) data.get("ip"));
            server.setPort((Integer) data.get("port"));

            if(data.get("owner") != null) {
                server.setOwner(((PGobject) data.get("owner")).getValue());
            } else {
                server.setOwner(null);
            }

            servers.add(server);
        }

        for (ServerModel server : servers) {
            sql = String.format("SELECT COUNT(*) FROM statistics WHERE server_id=%d;", server.getMid());
            Map<String, Object> records = template.queryForMap(sql);

            if((Integer) records.get("count") > 2000) {
                nonEmptyServers.add(server);
            }
        }

        for (ServerModel s : nonEmptyServers) {
            System.out.println(s.getHostname());
            sql = String.format("INSERT INTO goodservers (mid, hostname, map, ip, port, owner) VALUES (%d, '%s', '%s', '%s', %d, '%s');",
                    s.getMid(), s.getHostname(), s.getMap(), s.getIp(), s.getPort(), s.getOwner());
            template.execute(sql);
        }
    }

    // this method deletes unnecessary stats from table (empty/dev servers stats)
    public void deleteUnnecessaryStatistics() {
        ArrayList<Integer> ids = getGoodIds();

        String sql = "select distinct on(server_id) server_id from statistics;";
        List<Map<String, Object>> rows = template.queryForList(sql);
        for (Map<String, Object> row : rows) {
            int serverId = (Integer) row.get("server_id");

            if (!ids.contains(serverId)) {
                sql = String.format("DELETE FROM statistics WHERE server_id=%d;", serverId);
                template.execute(sql);
            }
        }

        sql = "SELECT COUNT(*) FROM statistics;";
        System.out.println(template.queryForMap(sql).get("count"));
    }

    // --------------------- HELPING METHODS

    // @return good servers ids list
    public ArrayList<Integer> getGoodIds() {
        ArrayList<Integer> goodIDs = new ArrayList<>();
        String sql = "SELECT mid FROM goodservers;";
        List<Map<String, Object>> goodServersIDs = template.queryForList(sql);

        for (Map<String, Object> serverID : goodServersIDs) {
            goodIDs.add((Integer) serverID.get("mid"));
        }

        return goodIDs;
    }

    public Timestamp recordingsEdgeTime(boolean first) {
        String sql;

        if (first) {
            sql = "SELECT time FROM statistics ORDER BY ID LIMIT 1;";
        } else {
            sql = "SELECT time FROM statistics ORDER BY ID DESC LIMIT 1;";
        }

        return (Timestamp) template.queryForMap(sql).get("time");
    }
}

























