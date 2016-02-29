package com.klemstinegroup.bleutrade.json;
import org.hsqldb.Server;
import org.hsqldb.util.*;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by Paul on 2/24/2016.
 */
public class DatabaseManagerStart {

   static public void main(String[] args) {
        // ---------------------------------------DATABASE------------------------------------------
       Server hsqlServer = new Server();
        hsqlServer.setLogWriter(null);
        hsqlServer.setSilent(true);
        hsqlServer.setDatabaseName(0, "iva");
        hsqlServer.setDatabasePath(0, "file:ivadb");
        hsqlServer.start();

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (Exception e) {
            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
            return;
        }


//        try {
//            Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/iva", "SA", "");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        // ---------------------------------------DATABASE------------------------------------------
        DatabaseManagerSwing.main(new String[]{});
}
}
