package com.klemstinegroup.bleutrade;


import com.klemstinegroup.bleutrade.json.Balance;
import com.klemstinegroup.bleutrade.json.Currency;
import com.klemstinegroup.bleutrade.HttpKeys;
import com.klemstinegroup.bleutrade.json.Market;
import com.klemstinegroup.bleutrade.json.Ticker;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Paul on 2/21/2016.
 */


public class Http {

    static String uri = "https://bleutrade.com/api/v2";

    public static double bitcoinPrice() throws Exception {
        String url="https://api.coinbase.com/v2/prices/spot?currency=USD";
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        String res = response.toString();
        JSONObject obj=new JSONObject(res);
        return obj.getJSONObject("data").getDouble("amount");

    }


    public static JSONObject open(String url, Map<String, String> params) throws IOException {
        url=uri+url;
        if (params == null) params = new HashMap<String, String>();

        if (params.size()>0) {

            url += "?";
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url += entry.getKey() + "=" + entry.getValue();
                url += "&";
            }
            url = url.substring(0, url.length() - 1);
        }
        System.out.println("opening url=" + url);
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        String res = response.toString();
        System.out.println("response="+res);
        return new JSONObject(res);
    }

    public static JSONObject openPrivate(String url, Map<String, String> params) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        url=uri+url;
        if (params == null) params = new HashMap<String, String>();
        params.put("apikey",HttpKeys.apikey);

        if (params.size()>0) {

            url += "?";
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url += entry.getKey() + "=" + entry.getValue();
                url += "&";
            }
            url = url.substring(0, url.length() - 1);
        }

        Mac sha_HMAC = Mac.getInstance("HmacSHA512");

        SecretKeySpec secret_key = new SecretKeySpec(HttpKeys.apisecret.getBytes(), "HmacSHA512");
        sha_HMAC.init(secret_key);

        String hash = toHex(sha_HMAC.doFinal(url.getBytes()));

        System.out.println("opening url=" + url);
        URL website = new URL(url);

        URLConnection connection = website.openConnection();
        connection.setRequestProperty("apisign",hash);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        String res = response.toString();
        return new JSONObject(res);
    }
    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "x", bi);
    }

    public static ArrayList<Currency> getCurrencies() throws Exception {
        JSONObject json=null;
        try {
            json=open("/public/getcurrencies",null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean success=json.getBoolean("success");
        String message=json.getString("message");
        if (!success)throw new Exception("error!");

        JSONArray array=json.getJSONArray("result");
        ArrayList<Currency> arr=new ArrayList<Currency>();
        for (int i=0;i<array.length();i++){
            arr.add(Currency.fromJson(array.getJSONObject(i)));
        }
        return arr;
    }

    public static ArrayList<Market> getMarkets() throws Exception {
        JSONObject json=null;
        try {
            json=open("/public/getmarkets",null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean success=json.getBoolean("success");
        String message=json.getString("message");
        if (!success)throw new Exception("error!");

        JSONArray array=json.getJSONArray("result");
        ArrayList<Market> arr=new ArrayList<Market>();
        for (int i=0;i<array.length();i++){
            arr.add(Market.fromJson(array.getJSONObject(i)));
        }
        return arr;
    }

    public static Ticker getTicker(String ticker) throws Exception {
        ArrayList<String> al=new ArrayList<String>();
        al.add(ticker);
        return getTickers(al).get(0);
    }

    public static ArrayList<Ticker> getTickers(List<String> tickers) throws Exception {
        JSONObject json=null;
        HashMap<String,String> params=new HashMap<String, String>();
        if (tickers!=null){
            String s="";
            for (String t:tickers)s+=t+",";
            s=s.substring(0,s.length()-1);
            params.put("market",s);
        }
        try {
            json=open("/public/getticker",params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean success=json.getBoolean("success");
        if (!success)throw new Exception("error!");

        JSONArray array=json.getJSONArray("result");
        ArrayList<Ticker> arr=new ArrayList<Ticker>();
        for (int i=0;i<array.length();i++){
            arr.add(Ticker.fromJson(array.getJSONObject(i)));
        }
        return arr;

    }

    public static ArrayList<Balance> getBalances() throws Exception {
        JSONObject json=null;
        try {
            json=openPrivate("/account/getbalances",null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean success=json.getBoolean("success");
        if (!success)throw new Exception("error!");
        JSONArray array=json.getJSONArray("result");
        ArrayList<Balance> arr=new ArrayList<Balance>();
        for (int i=0;i<array.length();i++){
            arr.add(Balance.fromJson(array.getJSONObject(i)));
        }
        return arr;
    }
}
