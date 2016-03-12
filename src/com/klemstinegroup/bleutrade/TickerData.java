package com.klemstinegroup.bleutrade;

import com.klemstinegroup.bleutrade.json.Ticker;

import java.math.BigDecimal;

/**
 * Created by Paul on 3/12/2016.
 */
public class TickerData {
    //String g1 = s.substring(0, s.indexOf('_'));
    //String g2 = s.substring(s.indexOf('_') + 1);
    //Ticker t=tickerHM.get(s);
    //String bid=df.format(new BigDecimal(t.getBid()));
    //String ask=df.format(new BigDecimal(t.getAsk()));
    //String last=df.format(new BigDecimal(t.getLast()));
    //  String insert="INSERT INTO ticker(time,coin,base,bid,ask,last) VALUES ("+time+",'"+g1+"','"+g2+"',"+bid+","+ask+","+last+")";
    String coin;
    String base;
    double bid;
    double ask;
    double last;
    long time;

    public TickerData(String coin, String base, double bid, double ask, double last,long time) {
        this.coin = coin;
        this.base = base;
        this.bid = bid;
        this.ask = ask;
        this.last = last;
        this.time=time;
    }



}
