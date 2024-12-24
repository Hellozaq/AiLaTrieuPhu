package org.example.conf;

import org.example.model.DatabaseProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Connection con;

    public Database() {}

    public static Connection getConnection() {
        return con;
    }

    public static void connect(DatabaseProperties prop) throws SQLException {
        new com.mysql.cj.jdbc.Driver();
        String url="jdbc:mysql://" + prop.getHostname() + ":" + prop.getPort() + "/" +
                prop.getDatabaseName() + "?createDatabaseIfNotExist=true";
        Database.con = DriverManager.getConnection(url, prop.getUsername(), prop.getPassword());
        if(Database.con != null) {
            System.out.println("Connected to database");
        }

    }

    public static void close() throws SQLException {
        if(Database.con != null) {
            Database.con.close();
            System.out.println("Closed database");
        }
    }
}
