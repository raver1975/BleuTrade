package com.klemstinegroup.bleutrade;


import com.klemstinegroup.bleutrade.json.Currency;
import com.klemstinegroup.bleutrade.json.Market;
import com.klemstinegroup.bleutrade.json.Ticker;
import edu.princeton.cs.algs4.*;
import org.hsqldb.Server;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class Main {

    private final TradeFrame mainFrame;
    HashMap<String, Double> coins = new HashMap<String, Double>();

    // private final Server hsqlServer;
    ArrayList<Currency> currencies = new ArrayList<Currency>();
    HashMap<String, Double> currencyCost = new HashMap<String,Double>();
    ArrayList<Market> markets = new ArrayList<Market>();
    private ArrayList<Ticker> tickers=new ArrayList<Ticker>();
    private HashMap<String,Ticker> tickerHM=new HashMap<String, Ticker>();
    private ArrayList<String> negativeCycles;


    public Main() {
        // ---------------------------------------DATABASE------------------------------------------
//        hsqlServer = new Server();
//        hsqlServer.setLogWriter(null);
//        hsqlServer.setSilent(true);
//        hsqlServer.setDatabaseName(0, "iva");
//        hsqlServer.setDatabasePath(0, "file:ivadb");
//        hsqlServer.start();
//
//        try {
//            Class.forName("org.hsqldb.jdbc.JDBCDriver");
//        } catch (Exception e) {
//            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
//            e.printStackTrace();
//            return;
//        }
//
//
//        try {
//            Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/iva", "SA", "");

//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        // ---------------------------------------DATABASE------------------------------------------


        mainFrame	= new TradeFrame();
        mainFrame.setVisible( true );
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //set up coins
        ArrayList<Currency> temp1 = null;
        try {
            temp1 = Http.getCurrencies();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Currency c : temp1) {
            coins.put(c.getCurrency(), 0d);
            System.out.println(c.getCurrency());
        }
        coins.put("BTC", 1d);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ArrayList<Currency> temp1 = null;
                    try {
                        temp1 = Http.getCurrencies();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ArrayList<Currency> temp = new ArrayList<Currency>();
                    for (Currency c : temp1) {
                        if (c.getIsActive() && !c.getMaintenanceMode()) temp.add(c);
                    }
                    currencies.clear();
                    currencies.addAll(temp);
                    for (Currency c : currencies) {
                        currencyCost.put(c.getCurrency(), c.getTxFee());
                    }

                    ArrayList<Market> temp2 = new ArrayList<Market>();
                    try {
                        temp2 = Http.getMarkets();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ArrayList<Market> temp3 = new ArrayList<Market>();
                    for (Market m : temp2) {
                        if (m.getIsActive()) temp3.add(m);
                    }
                    markets.clear();
                    markets.addAll(temp3);

                    ArrayList<String> al = new ArrayList<String>();
                    for (Market m : markets) {
                        al.add(m.getMarketName());
                    }
                    try {
                        tickers = Http.getTickers(al);
                        for (int i = 0; i < tickers.size(); i++) {
                            tickerHM.put(markets.get(i).getMarketCurrency(),tickers.get(i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //arbitrage
                    HashMap<String, Double> hm = new HashMap<String, Double>();

                    for (int i = 0; i < markets.size(); i++) {
                        Market m = markets.get(i);
                        String g = m.getMarketName();
                        hm.put(g, tickers.get(i).getBid());
                        String g1 = g.substring(0, g.indexOf('_'));
                        String g2 = g.substring(g.indexOf('_') + 1);
                        hm.put(g2 + "_" + g1, 1d / tickers.get(i).getAsk());
                    }

                    negativeCycles=new ArrayList<String>();
                    negativeCycle(hm);
                    mainFrame.change(negativeCycles);


                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        new Main();

    }

    public void negativeCycle(HashMap<String, Double> hm) {
        System.out.println("---------------------------------------------------------");
        HashSet<String> hs = new HashSet();
        for (String g : hm.keySet()) {

            String g1 = g.substring(0, g.indexOf('_'));
            String g2 = g.substring(g.indexOf('_') + 1);
            hs.add(g1);
            hs.add(g2);
        }
        int V = hs.size();
        ArrayList<String> name = new ArrayList<String>();
        Iterator<String> iter = hs.iterator();
        while (iter.hasNext()) {
            String s = iter.next();
            name.add(s);
        }

        EdgeWeightedDigraph G = new EdgeWeightedDigraph(V);

        for (Market m : markets) {
            String g = m.getMarketName();
            String g1 = g.substring(0, g.indexOf('_'));
            String g2 = g.substring(g.indexOf('_') + 1);
            if (hm.get(g1 + "_" + g2) != null) {
                DirectedEdge e1 = new DirectedEdge(name.indexOf(g1), name.indexOf(g2), -Math.log(hm.get(g1 + "_" + g2)));
                DirectedEdge e2 = new DirectedEdge(name.indexOf(g2), name.indexOf(g1), -Math.log(hm.get(g2 + "_" + g1)));
                G.addEdge(e1);
                G.addEdge(e2);
            }
        }

        BellmanFordSP spt = new BellmanFordSP(G, 0);
        if (spt.hasNegativeCycle()) {
            //double stake = 1000.0;
            boolean first = false;
            String remove1 = null;
            String remove2 = null;

            String last=null;
            String list="";

            for (DirectedEdge e : spt.negativeCycle()) {
                boolean stop = false;
                if (!first) {
                    first = true;
                    remove1 = name.get(e.from()) + "_" + name.get(e.to());
                    remove2 = name.get(e.to()) + "_" + name.get(e.from());
                }
                if (coins.get(name.get(e.from())) == null) coins.put(name.get(e.from()), 0d);
                double in = coins.get(name.get(e.from()));
                double stake1 = in / 2d;
                double fee = stake1 * 0.0025d;
                if (in <= fee) stop = true;
                if (!stop) coins.put(name.get(e.from()), in - (stake1+fee));

                StdOut.printf("%10.5f %s ", stake1, name.get(e.from()));
                double stake2 = stake1 * Math.exp(-e.weight());

                StdOut.printf("= %10.5f %s FEE %10.5f\n", stake2, name.get(e.to()), fee);

                if (!stop) coins.put(name.get(e.to()), coins.get(name.get(e.to())) + stake2);
                list+=name.get(e.from())+"-";
                last=name.get(e.to());

            }
            list+=last;
            negativeCycles.add(list);
            hm.remove(remove1);
            hm.remove(remove2);
            negativeCycle(hm);
        } else {
            double tot=0;
            for (String c : coins.keySet()) {
                if (coins.get(c) != null && coins.get(c) > 0) {
                    System.out.println(c + "\t" + coins.get(c));
                    if (!c.equals("BTC")){
                        tot+=tickerHM.get(c).getAsk();
                    }
                    else tot+=coins.get(c);
                }
            }
            System.out.println("TOT\t"+tot);
        }
    }
}
