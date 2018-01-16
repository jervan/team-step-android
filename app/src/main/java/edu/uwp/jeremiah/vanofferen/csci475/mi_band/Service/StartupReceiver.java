package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Jeremiah on 12/6/16.
 */

public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG ="StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        if (QueryPreferences.getStoredGroup(context) != null) {
            BackgroundService.setService(context);
        }
    }
}
