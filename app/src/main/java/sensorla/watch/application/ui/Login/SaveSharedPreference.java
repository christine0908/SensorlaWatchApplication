package sensorla.watch.application.ui.Login;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.Gson;
import java.util.regex.Pattern;
import sensorla.watch.application.ui.Login.Models.UserInfo;
import static sensorla.watch.application.ui.Login.PreferencesUtility.LOGGED_IN_PREF;
import static sensorla.watch.application.ui.Login.PreferencesUtility.LOGGED_IN_USER;

public class SaveSharedPreference {

    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setLoggedIn(Context context, boolean loggedIn) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(LOGGED_IN_PREF, loggedIn);
        editor.apply();
    }

    public static boolean getLoggedStatus(Context context) {
        return getPreferences(context).getBoolean(LOGGED_IN_PREF, false);
    }

    public static void saveUserInfo(Context context, UserInfo userInfo) {
        String userInfoString = new Gson().toJson(userInfo);
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(LOGGED_IN_USER, userInfoString);
        editor.apply();
    }

    public static UserInfo getUserInfo(Context context) {
        String userInfoString = getPreferences(context).getString(LOGGED_IN_USER, null);
        UserInfo userInfo = new Gson()
                .fromJson(userInfoString, UserInfo.class);

        if (userInfo != null)
            return userInfo;
        else
            return null;
    }

    public static UserInfo loggedInUserInfo(String responseBody) {
        String[] userString = responseBody.split(Pattern.quote("*"));

        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(userString[1]);
        userInfo.setUserId(userString[2]);
        userInfo.setDeviceId(userString[3]);

        return userInfo;
    }

    public static UserInfo connectedUserInfo(String responseBody) {
        String[] userString = responseBody.split(Pattern.quote("*"));

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userString[0]);
        userInfo.setUserName(userString[1]);

        return userInfo;
    }

    static final String ENV = "ENV";
    public static void setEnvironment(Context context, String env) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(ENV, env);
        editor.apply();
    }
    public static String getEnvironment(Context context) {
        return getPreferences(context).getString("ENV", "");
    }

    // Getter and Setter for device id
    static final String DEVICE_ID = "DeviceId";
    public static void setDeviceId(Context context, String deviceId) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(DEVICE_ID, deviceId);
        editor.apply();
    }

    public static String getDeviceID(Context context) {
        return getPreferences(context).getString(DEVICE_ID, " ");
    }


    static final int user_id = 000000;
    public static void setUser_id(Context context,int user_ID){
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt("userID",user_ID);
        editor.apply();
    }
    public  static int getUser_id(Context context){
        return getPreferences(context).getInt("userID",000000);
    }
}
