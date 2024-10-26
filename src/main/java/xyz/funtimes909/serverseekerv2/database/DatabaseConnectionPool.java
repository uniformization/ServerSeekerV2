package xyz.funtimes909.serverseekerv2.database;

import org.apache.commons.dbcp2.BasicDataSource;
import xyz.funtimes909.serverseekerv2.builders.Config;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionPool {
    private static final BasicDataSource dataSource = new BasicDataSource();

    public static void initPool(Config config) {
        dataSource.setUrl("jdbc:postgresql://" + config.getPostgresUrl());
        dataSource.setUsername(config.getPostgresUser());
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println("Failed to connect to database!");
            e.printStackTrace();
        }
        return null;
    }
}
