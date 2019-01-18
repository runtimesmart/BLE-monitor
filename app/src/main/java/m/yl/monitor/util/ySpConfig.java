package m.yl.monitor.util;

import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;

import m.yl.monitor.Application;

/**
 * Created by Yl on 16/12/24.
 */

public class ySpConfig {
    public ySpConfig() {
    }

    public static void clear() {
        SharedPreferences sp = Application.getInstance().getSharedPreferences(Application.getInstance().mSpname, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        commit(editor);
    }

    public static void saveObjToSp(String key, Object obj) {
        saveStrToSp(key, (new Gson()).toJson(obj));
    }

    public static Object readObjFromSp(String key, Class<?> objClass) {
        String str = readStrFromSp(key);
        return TextUtils.isEmpty(str)?null:(new Gson()).fromJson(str, objClass);
    }

    public static void saveStrToSp(String key, String value) {
        if(!TextUtils.isEmpty(key)) {
            SharedPreferences sp = Application.getInstance().getSharedPreferences(Application.getInstance().mSpname, 0);
            SharedPreferences.Editor editor = sp.edit();
            if(TextUtils.isEmpty(value)) {
                editor.remove(key);
            } else {
                editor.putString(key, value);
            }

            commit(editor);
        }
    }

    public static String readStrFromSp(String key) {
        if(TextUtils.isEmpty(key)) {
            return "";
        } else {
            SharedPreferences sp = Application.getInstance().getSharedPreferences(Application.getInstance().mSpname, 0);
            return sp.getString(key, "");
        }
    }

    public static String readStrFromSp(String key, String defaultvalue) {
        if(TextUtils.isEmpty(key)) {
            return defaultvalue;
        } else {
            SharedPreferences sp = Application.getInstance().getSharedPreferences(Application.getInstance().mSpname, 0);
            return sp.getString(key, defaultvalue);
        }
    }

    public static void saveBooleanToSp(String key, boolean value) {
        if(!TextUtils.isEmpty(key)) {
            SharedPreferences sp = Application.getInstance().getSharedPreferences(Application.getInstance().mSpname, 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(key, value);
            commit(editor);
        }
    }

    public static boolean readBooleanFromSp(String key, boolean defaultValue) {
        if(TextUtils.isEmpty(key)) {
            return defaultValue;
        } else {
            SharedPreferences sp = Application.getInstance().getSharedPreferences(Application.getInstance().mSpname, 0);
            return sp.getBoolean(key, defaultValue);
        }
    }

    public static void commit(SharedPreferences.Editor editor) {
        if(Build.VERSION.SDK_INT < 9) {
            editor.commit();
        } else {
            editor.apply();
        }

    }
}
