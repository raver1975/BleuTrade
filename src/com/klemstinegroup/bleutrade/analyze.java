package com.klemstinegroup.bleutrade;


import com.klemstinegroup.bleutrade.json.Balance;
import com.klemstinegroup.bleutrade.json.Currency;
import com.klemstinegroup.bleutrade.json.Market;
import com.klemstinegroup.bleutrade.json.Ticker;
import edu.princeton.cs.algs4.BellmanFordSP;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
//import edu.princeton.cs.algs4.StdOut;

import javax.swing.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

class Analyze {
//    private TradeFrame mainFrame;
//
//    private Server hsqlServer;
//    private  Connection conn;


    ArrayList<TickerData> saved = new ArrayList<TickerData>();
    ArrayList<Currency> currencies = new ArrayList<Currency>();
    HashMap<String, Double> currencyCost = new HashMap<String, Double>();
    ArrayList<Market> markets = new ArrayList<Market>();
    private ArrayList<Ticker> tickers = new ArrayList<Ticker>();
    private HashMap<String, Ticker> tickerHM = new HashMap<String, Ticker>();
    private ArrayList<String> negativeCycles;

    HashMap<String, TickerData> nowhm = new HashMap<String, TickerData>();
    HashMap<String, TickerData> maxhm = new HashMap<String, TickerData>();
    HashMap<String, TickerData> minhm = new HashMap<String, TickerData>();

    DecimalFormat df = new DecimalFormat("000.00");
    DecimalFormat df1 = new DecimalFormat("000.000000000");

    //CREATE TABLE TICKER(TIME BIGINT,COIN VARCHAR(10),BASE VARCHAR(10),BID DOUBLE,ASK DOUBLE,LAST DOUBLE)

    public Analyze() {
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
//        }
//
//
//        try {
//            conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/iva", "SA", "");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        // ---------------------------------------DATABASE------------------------------------------

//        mainFrame = new TradeFrame();
//        mainFrame.setVisible(true);
//        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
                            tickerHM.put(markets.get(i).getMarketName(), tickers.get(i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    long time = System.currentTimeMillis();
                    for (String s : tickerHM.keySet()) {
                        maxhm.clear();
                        minhm.clear();
                        nowhm.clear();
                        ArrayList<TickerData> remove = new ArrayList<TickerData>();

                        for (TickerData td : remove) saved.remove(td);
                        String g1 = s.substring(0, s.indexOf('_'));
                        String g2 = s.substring(s.indexOf('_') + 1);
                        Ticker t = tickerHM.get(s);
                        double bid = t.getBid();
                        double ask = t.getAsk();
                        double last = t.getLast();
//                        String bidS = df.format(new BigDecimal(t.getBid()));
//                        String askS = df.format(new BigDecimal(t.getAsk()));
//                        String lastS = df.format(new BigDecimal(t.getLast()));
//                       String insert="INSERT INTO ticker(time,coin,base,bid,ask,last) VALUES ("+time+",'"+g1+"','"+g2+"',"+bid+","+ask+","+last+")";
                        saved.add(new TickerData(g1, g2, bid, ask, last, time));

                        for (TickerData td : saved) {
                            if (td.time < System.currentTimeMillis() - 86400000) {
                                remove.add(td);
                            } else {
                                String bb = td.coin + "-" + td.base;
                                if (!maxhm.containsKey(bb) || td.ask > maxhm.get(bb).ask) {
                                    maxhm.put(bb, td);
                                }
                                if (!minhm.containsKey(bb) || td.ask < minhm.get(bb).ask) {
                                    minhm.put(bb, td);
                                }
                                if (!nowhm.containsKey(bb) || td.time > nowhm.get(bb).time) {
                                    nowhm.put(bb, td);
                                }
                            }
                        }

                        // System.out.println(g+"\t\t"+(now/range));


                    }
                    negativeCycles = new ArrayList<String>();

                    for (String g : maxhm.keySet()) {
//                            System.out.println(g+"\t"+df.format(minhm.get(g))+"\t"+df.format(maxhm.get(g)));
                        double range = maxhm.get(g).ask - minhm.get(g).ask;
                        double now = (nowhm.get(g).ask - minhm.get(g).ask);
                        //if (range!=0d)System.out.println("range="+g+"\t"+range);
                        if (Math.abs(range) < .000000001d) {
                            continue;
                        }

                        double perc = now / range;
                       // System.out.println("perc="+g+"\t"+perc);
                        if (perc>0d&&perc<1d&&(perc<.1d||perc>.9d)){
                            String s=df.format((perc) * 100d) + "\t" + g+"\t"+minhm.get(g).ask+"\t"+nowhm.get(g).ask+"\t"+maxhm.get(g).ask+"\t"+new Date(minhm.get(g).time)+"\t"+new Date(maxhm.get(g).time);
                            negativeCycles.add(s);
                        }
                    }
//                    try {
//                        query("select * from ticker");
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }

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


                    //negativeCycle(hm);
                    Collections.sort(negativeCycles);
                    //Collections.reverse(negativeCycles);
                    for (String s:negativeCycles){
                        System.out.println(s);

                    }

                    double bitcoinprice=0;
                    try {
                        bitcoinprice=Http.bitcoinPrice();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        ArrayList<Balance> bal=Http.getBalances();
                        System.out.println("Balances:");
                        for (Balance b:bal){
                            if (b.getBalance()>0d) {
                                System.out.print("  "+b.getCurrency() + "\t" + df1.format(b.getBalance()));
                                if (b.getCurrency().equals("BTC"))System.out.print("\t"+df1.format(b.getBalance())+"\t$"+df.format(b.getBalance()*bitcoinprice));
                                for (int i=saved.size()-1;i>-1;i--) {
                                    TickerData td=saved.get(i);
                                    if (td.coin.equals(b.getCurrency())&&td.base.equals("BTC")){
                                        System.out.print("\t"+df1.format(td.last*b.getBalance())+"\t$"+df.format(td.last*b.getBalance()*bitcoinprice));
                                        break;
                                    }

                                }
                                System.out.println();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    mainFrame.change(negativeCycles);

                    System.out.println("-------------------------------------------------------------------------------------------------");
                    System.out.println("time=" + new Date());
                    try {
                        Thread.sleep(60000 * 10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        ).

                start();

    }

    public static void main(String[] args) throws Exception {
        new Analyze();

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

                //StdOut.printf("%10.5f %s ", stake, name.get(e.from()));
                stake -= .0025 * stake;
                stake *= Math.exp(-e.weight());


                //StdOut.printf("= %10.5f %s\n", stake, name.get(e.to()));

                list += name.get(e.from()) + "-";
                last = name.get(e.to());

            }
            list += last;
            String g = String.format("%06.2f", (stake - 1000d) / 10d);
            list = g + " " + list;
            if (stake > 1000) {
                negativeCycles.add(list);
                //System.out.println("cycle: " + list);
            }
            hm.remove(remove1);
            hm.remove(remove2);
            negativeCycle(hm);
        }
    }


//    //use for SQL command SELECT
//    public synchronized void query(String expression) throws SQLException {
//
//        Statement st = null;
//        ResultSet rs = null;
//        st = conn.createStatement();         // statement objects can be reused with
//        rs = st.executeQuery(expression);    // run the query
//        dump(rs);
//        st.close();    // NOTE!! if you close a statement the associated ResultSet is
//    }
//
//    //use for SQL commands CREATE, DROP, INSERT and UPDATE
//    public synchronized void update(String expression) throws SQLException {
//        Statement st = null;
//        st = conn.createStatement();    // statements
//        int i = st.executeUpdate(expression);    // run the query
//        if (i == -1) {
//            System.out.println("db error : " + expression);
//        }
//        st.close();
//    }
//
//
//    public static void dump(ResultSet rs) throws SQLException {
//        ResultSetMetaData meta   = rs.getMetaData();
//        int               colmax = meta.getColumnCount();
//        int               i;
//        Object            o = null;
//        for (; rs.next(); ) {
//            for (i = 0; i < colmax; ++i) {
//                o = rs.getObject(i + 1);    // Is SQL the first column is indexed
//                System.out.print(o.toString() + " ");
//            }
//            System.out.println(" ");
//        }
//    }
}
