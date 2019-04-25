package me.melijn.dhs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.melijn.dhs.components.SwitchComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Database {

    private final HikariDataSource ds;
    private final Logger logger = LoggerFactory.getLogger(Database.class.getName());
    public Set<SwitchComponent> switchComponentSet = new HashSet<>();


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
        executeUpdate("CREATE TABLE IF NOT EXISTS switches(id int, on_code int, off_code int, last_state boolean, PRIMARY KEY (id));");
        executeUpdate("CREATE TABLE IF NOT EXISTS remote_btn_index(id varchar(64), codeSequence varchar(1024), PRIMARY KEY (id));");

        executeUpdate("CREATE TABLE IF NOT EXISTS button_presets(username varchar(32), name varchar(32), location varchar(64), code int);");
        executeUpdate("CREATE TABLE IF NOT EXISTS switch_presets(username varchar(32), name varchar(32), location varchar(64), id int);");
        executeUpdate("CREATE TABLE IF NOT EXISTS view_presets(username varchar(32), name varchar(32), location varchar(64), url varchar(128));");


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

    private void executeQuery(final String sql, final Consumer<ResultSet> consumer, final Object... objects) {
        try (final Connection connection = ds.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int current = 1;
            for (final Object object : objects) {
                preparedStatement.setObject(current++, object);
            }
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                consumer.accept(resultSet);
            }
        } catch (final SQLException e) {
            logger.error("Something went wrong while executing the query: " + sql);
            e.printStackTrace();
        }
    }

    public List<SwitchComponent> getSwitchPresets(String user) {
        List<SwitchComponent> switchComponents = new ArrayList<>();
        try (Connection con = ds.getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT * FROM switch_presets WHERE username = ?")) {
            statement.setString(1, user);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    //TODO use cache instead of query
                    //switchComponents.add(new SwitchComponent(rs.getString(""), rs.getInt(""), false, 0, 0));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return switchComponents;
    }
}
