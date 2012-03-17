package uk.co.ks07.uhome.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.ks07.uhome.HomeConfig;

public class ConnectionManager {

    private static Connection conn;

    public static Connection initialize(Logger log) {
        try {
            if (HomeConfig.usemySQL) {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(HomeConfig.mySQLconn, HomeConfig.mySQLuname, HomeConfig.mySQLpass);
                conn.setAutoCommit(false);
                return conn;
            } else {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:" + HomeConfig.dataDir.getAbsolutePath() + "/uhomes.db");
                conn.setAutoCommit(false);
                return conn;
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "SQL exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            log.log(Level.SEVERE, "You need the SQLite/MySQL library.", ex);
        }
        return conn;
    }

    public static Connection getConnection(Logger log) {
        if (conn == null) {
            conn = initialize(log);
        }

        if (HomeConfig.usemySQL) {
            // We probably dont need to do this for SQLite.
            try {
                if (!conn.isValid(10)) {
                    conn = initialize(log);
                }
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "Failed to check SQL status", ex);
            }
        }
        return conn;
    }

    public static void closeConnection(Logger log) {
        if (conn != null) {
            try {
                if (HomeConfig.usemySQL) {
                    if (conn.isValid(10)) {
                        conn.close();
                    }
                    conn = null;
                } else {
                    conn.close();
                    conn = null;
                }
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "Error on Connection close", ex);
            }
        }
    }
}
