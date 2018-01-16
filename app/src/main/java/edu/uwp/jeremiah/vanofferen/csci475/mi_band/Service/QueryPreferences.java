package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Service;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Group;

/**
 * Created by Jeremiah on 12/6/16.
 */


public class QueryPreferences {
    private static final String TAG = "QueryPreferences";
    private static final String PREF_GROUP = "group";
    private static final String PREF_FIRST_RUN ="firstRun";
    private static Gson gson = new GsonBuilder().create();

    public static Group getStoredGroup(Context context) {
        Log.d(TAG, "Group Retrieved");
        String JSONGroup = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_GROUP, null);
        if (JSONGroup == null) {
            return null;
        }
        return gson.fromJson(JSONGroup, new TypeToken<Group>() {}.getType());
    }

    public static void setStoredGroup(Context context,@Nullable Group group) {
        Log.d(TAG, "Group Saved");
        String JSONGroup;
        if (group == null) {
            JSONGroup = null;
        } else {
            JSONGroup = gson.toJson(group, new TypeToken<Group>() {}.getType());
        }
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_GROUP, JSONGroup)
                .apply();
    }

    public static boolean getFirstRun(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_FIRST_RUN, true);
    }

    public static void setFirstRun(Context context, boolean firstRun) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_FIRST_RUN, firstRun)
                .apply();
    }
}
