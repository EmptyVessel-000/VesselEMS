package vesselems.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import vesselems.model.Datasource;

@Component
public class DSManager {

    private final Map<Long, DataSource> cache = new ConcurrentHashMap<>();

    public DataSource get(Datasource ds) {
        return cache.computeIfAbsent(ds.getId(), id -> create(ds));
    }

    public void evict(Long id) {
        DataSource old = cache.remove(id);
        if (old instanceof HikariDataSource hds) {
            hds.close();
        }
    }

    public boolean test(Datasource ds) {
        try (Connection conn = DriverManager.getConnection(buildJdbcUrl(ds), ds.getUsername(), ds.getPassword())) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    private DataSource create(Datasource ds) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(buildJdbcUrl(ds));
        config.setUsername(ds.getUsername());
        config.setPassword(ds.getPassword());
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(5000);
        return new HikariDataSource(config);
    }

    private String buildJdbcUrl(Datasource ds) {
        String dbType = ds.getDbType() != null ? ds.getDbType().toLowerCase() : "mysql";
        String host = ds.getHost() != null ? ds.getHost() : "localhost";
        int port = ds.getPort() != null ? ds.getPort() : defaultPort(dbType);
        String dbName = ds.getDatabaseName() != null ? ds.getDatabaseName() : "";
        String driver = driverClass(dbType);
        // Set driver class for test connection
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ignored) {
        }
        return String.format("jdbc:%s://%s:%d/%s", dbType, host, port, dbName);
    }

    private String driverClass(String dbType) {
        return switch (dbType) {
            case "postgresql" -> "org.postgresql.Driver";
            case "oracle" -> "oracle.jdbc.OracleDriver";
            case "sqlserver" -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "mariadb" -> "org.mariadb.jdbc.Driver";
            default -> "com.mysql.cj.jdbc.Driver";
        };
    }

    private int defaultPort(String dbType) {
        return switch (dbType) {
            case "postgresql" -> 5432;
            case "oracle" -> 1521;
            case "sqlserver" -> 1433;
            case "mariadb" -> 3306;
            default -> 3306;
        };
    }
}