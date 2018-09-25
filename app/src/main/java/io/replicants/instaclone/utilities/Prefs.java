package io.replicants.instaclone.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

    public static String JWT = "jwt";
    public static String USERNAME = "username";
    public static String DISPLAY_NAME = "display_name";
    public static String USER_ID = "user_id";
    public static String FEED_SORT = "feed_sort";
    public static String LOCATION_DENIED_FOREVER = "location_denied_forever";
    public static String CAMERA_DENIED_FOREVER = "camera_denied_forever";
    public static String EXTERNAL_STORAGE_DENIED_FOREVER = "external_storage_denied_forever";
    public static String CAMERA_FLASH_STATUS = "camera_flash_status";
    public static String CAMERA_SIDE = "camera_side";
    public static int LOCATION_REQUEST_CODE =757;
    public static int CAMERA_REQUEST_CODE =123;
    public static int EXTERNAL_STORAGE_CODE =372;

    public static String CAMERA_BACK_WIDTH = "camera_back_width";
    public static String CAMERA_BACK_HEIGHT = "camera_back_height";
    public static String CAMERA_FRONT_WIDTH = "camera_front_width";
    public static String CAMERA_FRONT_HEIGHT = "camera_front_height";

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
