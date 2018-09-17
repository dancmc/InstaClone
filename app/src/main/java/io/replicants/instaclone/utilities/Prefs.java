package io.replicants.instaclone.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

    public static String JWT = "jwt";
    public static String FEED_SORT = "feed_sort";
    public static int LOCATION_REQUEST_CODE =2318389;


    private static Prefs instance;
    private SharedPreferences sharedPref;

    private Prefs(Context context){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context){
        instance = new Prefs(context);
    }

    public static Prefs getInstance(){
       return instance;
    }

    public void writeString(String key, String value){
        sharedPref.edit().putString(key, value).apply();
    }

    public void writeInt(String key, int value){
        sharedPref.edit().putInt(key, value).apply();
    }

    public void writeFloat(String key, float value){
        sharedPref.edit().putFloat(key, value).apply();
    }

    public void writeBoolean(String key, boolean value){
        sharedPref.edit().putBoolean(key, value).apply();
    }

    public String readString(String key, String def){
        return sharedPref.getString(key, def);
    }

    public int readInt(String key, int def){
        return sharedPref.getInt(key, def);
    }

    public boolean readBoolean(String key, boolean def){
        return sharedPref.getBoolean(key, def);
    }

    public float readFloat(String key, float def){
        return sharedPref.getFloat(key, def);
    }
}
