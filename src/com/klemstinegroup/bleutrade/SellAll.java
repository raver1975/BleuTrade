package com.klemstinegroup.bleutrade;

import com.klemstinegroup.bleutrade.json.Balance;
import com.klemstinegroup.bleutrade.json.Market;
import com.klemstinegroup.bleutrade.json.Ticker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paul on 4/13/2016.
 */
public class SellAll {

    static DecimalFormat dfcoins = new DecimalFormat("+0000.00000000;-0000.00000000");


    public static void main(String[] args) {
        ArrayList<Market> markets = new ArrayList<Market>();
        ArrayList<Market> temp2 = new ArrayList<Market>();
        HashMap<String, Ticker> tickerHM = new HashMap<String, Ticker>();
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
        ArrayList<Ticker> tickers = Http.getTickers(al);
        for (int i = 0; i < tickers.size(); i++) {
            tickerHM.put(markets.get(i).getMarketName(), tickers.get(i));
        }

        ArrayList<Balance> balance = Http.getBalances();
        for (Balance b : balance) {
            if (b.getCurrency().equals("BLEU")||b.getCurrency().equals("BTC"))continue;
            String sellmarket=b.getCurrency()+"_BTC";
            double sellrate=tickerHM.get(sellmarket).getBid();
            System.out.println(dfcoins.format(b.getAvailable())+"\t"+dfcoins.format(sellrate)+"\t"+b.getCurrency());
            long id=Http.buyselllimit(sellmarket, sellrate, b.getAvailable(), false);
            System.out.println("order number=" + id);
        }
    }
}
