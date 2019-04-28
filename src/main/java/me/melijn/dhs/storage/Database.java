package me.melijn.dhs.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.melijn.dhs.components.Location;
import me.melijn.dhs.components.SwitchComponent;
import org.jooby.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private final HikariDataSource ds;
    private final Logger logger = LoggerFactory.getLogger(Database.class.getName());


    public Database(String host, String port, String user, String password, String db) {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(20);
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("useLegacyDatetimeCode", "false");
        config.addDataSourceProperty("serverTimezone", "UTC");
        //https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        config.addDataSourceProperty("allowMultiQueries", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "350");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true");

        this.ds = new HikariDataSource(config);
        init();
    }

    private void init() {
        logger.info("[MySQL] has connected & Loading init..");

        //Tables
        executeUpdate("CREATE TABLE IF NOT EXISTS users(username varchar(32), token varchar(64), PRIMARY KEY (username));");
        executeUpdate("CREATE TABLE IF NOT EXISTS log(username varchar(32), action varchar(512), moment bigint);");
        executeUpdate("CREATE TABLE IF NOT EXISTS remote_buttons(id varchar(64), codeSequence varchar(1024), PRIMARY KEY (id));");

        executeUpdate("CREATE TABLE IF NOT EXISTS views(id int, name varchar(32), url varchar(128), location varchar(64), PRIMARY KEY (id));");
        executeUpdate("CREATE TABLE IF NOT EXISTS buttons(id int, name varchar(32), url varchar(128), location varchar(64), PRIMARY KEY (id));");
        executeUpdate("CREATE TABLE IF NOT EXISTS switches(id int, name varchar(32), on_code int, off_code int, location varchar(64), last_state boolean, PRIMARY KEY (id));");

        executeUpdate("CREATE TABLE IF NOT EXISTS button_presets(username varchar(32), name varchar(32), id int);");
        executeUpdate("CREATE TABLE IF NOT EXISTS switch_presets(username varchar(32), id int, name varchar(32), on_code int, off_code int, location varchar(64), last_state boolean);");
        executeUpdate("CREATE TABLE IF NOT EXISTS view_presets(username varchar(32), name varchar(32), id int);");


        logger.info("[MySQL] init loaded");
    }

    public int executeUpdate(final String query, final Object... objects) {
        try (final Connection connection = ds.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int current = 1;
            for (final Object object : objects) {
                preparedStatement.setObject(current++, object);
            }
            return preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            logger.error("Something went wrong while executing the query: " + query);
            e.printStackTrace();
        }
        return 0;
    }

    public List<SwitchComponent> getSwitchPresets(String username) {
        List<SwitchComponent> switchComponents = new ArrayList<>();
        try (Connection con = ds.getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT * FROM switch_presets WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    switchComponents.add(new SwitchComponent(
                            rs.getString("name"),
                            Location.valueOf(rs.getString("location")),
                            rs.getInt("id"),
                            rs.getBoolean("last_state"),
                            rs.getInt("on_code"),
                            rs.getInt("off_code")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return switchComponents;
    }

    public List<SwitchComponent> getSwitchList() {
        List<SwitchComponent> switchComponents = new ArrayList<>();
        try (Connection con = ds.getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT * FROM switches")) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    if (rs.getString("location").isEmpty()) continue;
                    switchComponents.add(new SwitchComponent(
                            rs.getString("name"),
                            Location.valueOf(rs.getString("location")),
                            rs.getInt("id"),
                            rs.getBoolean("last_state"),
                            rs.getInt("on_code"),
                            rs.getInt("off_code")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return switchComponents;
    }


    public Map<String, String> getUserTokens() {
        Map<String, String> userTokens = new HashMap<>();
        try (Connection con = ds.getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT * FROM users")) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                userTokens.put(rs.getString("name"), rs.getString("token"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userTokens;
    }

    public void log(String user, Request req) {
        executeUpdate("INSERT INTO log (username, action, moment) VALUES (?, ?, ?)", user, req.ip() + "/" + req.method() + " - " + req.path(), System.currentTimeMillis());
    }

    public void updateSwitchState(int id, boolean state) {
        executeUpdate("UPDATE switches SET last_state= ? WHERE id= ?", state, id);
    }
}
