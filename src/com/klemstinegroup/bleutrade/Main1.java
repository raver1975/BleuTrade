package com.klemstinegroup.bleutrade;


import com.klemstinegroup.bleutrade.json.Currency;
import com.klemstinegroup.bleutrade.json.Market;
import com.klemstinegroup.bleutrade.json.Ticker;
import edu.princeton.cs.algs4.BellmanFordSP;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.StdOut;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.util.*;

public class Main1 {

    private final TradeFrame mainFrame;

    // private final Server hsqlServer;
    ArrayList<Currency> currencies = new ArrayList<Currency>();
    HashMap<String, Double> currencyCost = new HashMap<String, Double>();
    ArrayList<Market> markets = new ArrayList<Market>();
    private ArrayList<Ticker> tickers = new ArrayList<Ticker>();
    private HashMap<String, Ticker> tickerHM = new HashMap<String, Ticker>();
    private ArrayList<String> negativeCycles;


    public Main1() {
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


        mainFrame = new TradeFrame();
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            SoundUtils.tone(1000,100);
            SoundUtils.tone(2000,100);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }


        //set up coins
        ArrayList<Currency> temp1 = null;
        try {
            temp1 = Http.getCurrencies();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                            tickerHM.put(markets.get(i).getMarketCurrency(), tickers.get(i));
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

                    negativeCycles = new ArrayList<String>();
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
        new Main1();

    }

    public void negativeCycle(HashMap<String, Double> hm) {
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
            double stake = 1000.0;
            boolean first = false;
            String remove1 = null;
            String remove2 = null;

            String last = null;
            String list = "";

            for (DirectedEdge e : spt.negativeCycle()) {
                if (!first) {
                    first = true;
                    remove1 = name.get(e.from()) + "_" + name.get(e.to());
                    remove2 = name.get(e.to()) + "_" + name.get(e.from());
                }

                StdOut.printf("%10.5f %s ", stake, name.get(e.from()));
                stake-=.0025*stake;
                stake *= Math.exp(-e.weight());


                StdOut.printf("= %10.5f %s\n", stake, name.get(e.to()));

                list += name.get(e.from()) + "-";
                last = name.get(e.to());

            }
            list += last;
            String g=String.format("%05.2f",(stake-1000d)/10d);
            list=g+" "+list;
            if (stake>1000)negativeCycles.add(list);
            hm.remove(remove1);
            hm.remove(remove2);
            negativeCycle(hm);
        }
    }
}
