package com.klemstinegroup.bleutrade;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Paul on 3/26/2016.
 */
public class Serializer {

    static String savedFile="saved.ser";

    public static Object load(String file) throws Exception {
        FileInputStream fin = new FileInputStream(file);
        ObjectInputStream oos = new ObjectInputStream(fin);
        Object o=oos.readObject();
        fin.close();
        return o;

    }

    public static void save(Object o,String file) throws Exception {
        FileOutputStream fout = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(o);
        fout.close();
    }

    public static ArrayList<TickerData> loadSaved() throws Exception {
           return (ArrayList<TickerData>)   load(savedFile);
    }
    public static void saveSaved(ArrayList<TickerData> saved) throws Exception{
        save(saved,savedFile);
    }
}
