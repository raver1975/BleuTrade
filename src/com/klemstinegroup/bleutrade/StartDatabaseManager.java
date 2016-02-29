package com.klemstinegroup.bleutrade;

import org.hsqldb.DatabaseManager;
import org.hsqldb.Server;
import org.hsqldb.util.DatabaseManagerSwing;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Paul on 2/29/2016.
 */
public class StartDatabaseManager {
    public static void main(String[] args) {
        new StartDatabaseManager();
    }
    public StartDatabaseManager(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                // ---------------------------------------DATABASE------------------------------------------
                Server hsqlServer = new Server();
                hsqlServer.setLogWriter(null);
                hsqlServer.setSilent(true);
                hsqlServer.setDatabaseName(0, "iva");
                hsqlServer.setDatabasePath(0, "file:ivadb");
                hsqlServer.start();
                // ---------------------------------------DATABASE------------------------------------------

//                DatabaseManagerSwing dbms = new DatabaseManagerSwing();
//                dbms.main();
                DatabaseManagerSwing.main(new String[]{});

            }
        }).start();
    }
}
