package xyz.funtimes909.serverseekerv2.util;

import org.apache.commons.dbcp2.BasicDataSource;
import xyz.funtimes909.serverseekerv2.Main;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
    private static final BasicDataSource dataSource = new BasicDataSource();

    static {
        dataSource.setUrl("jdbc:postgresql://" + Main.postgresUrl + "postgres");
        dataSource.setPassword(Main.postgresPassword);
        dataSource.setUsername(Main.postgresUser);
    }

    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database!", e);
        }
    }
}