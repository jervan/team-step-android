package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

/**
 * Created by Jeremiah on 12/6/16.
 */

public class BackgroundService  {

    private static final String TAG = "BackgroundService";
    private static final long POLL_INTERVAL = 1000 * 60 * 15;
    private static final int JOB_ID = 1;

    public static void setService(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, MyBackgroundService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPeriodic(POLL_INTERVAL)
                .setPersisted(true)
                .build();
        scheduler.schedule(jobInfo);
        Log.d(TAG, "Background service scheduled");
    }

    public static void cancelService(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);
        Log.d(TAG, "Background service cancelled");
    }

    public static boolean isServiceOn(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo: scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;
            }
        }
        return hasBeenScheduled;
    }
}
