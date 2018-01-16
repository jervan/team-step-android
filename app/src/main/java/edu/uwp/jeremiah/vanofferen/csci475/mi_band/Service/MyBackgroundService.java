package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.MiBandManager;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Group;

/**
 * Created by Jeremiah on 12/6/16.
 */

public class MyBackgroundService extends JobService {
    private static final String TAG ="MyBackGroundService";
    private BackgroundTask mCurrentTask;
    private Looper mLooper;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mCurrentTask = new BackgroundTask();
        mCurrentTask.execute(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        if (mLooper != null) {
            mLooper.quit();
        }
        return true;
    }

    private class BackgroundTask extends AsyncTask<JobParameters, Void, Void> {

        @Override
        protected Void doInBackground(JobParameters... params) {
            Log.i(TAG, "processing background task");
            if (QueryPreferences.getFirstRun(MyBackgroundService.this)) {
                QueryPreferences.setFirstRun(MyBackgroundService.this, false);
            } else {
                backgroundService();
            }
            return null;
        }
    }

    private void backgroundService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Group group = QueryPreferences.getStoredGroup(MyBackgroundService.this);
                MiBandManager miBandManager = MiBandManager.getInstance(MyBackgroundService.this);
                if(group == null) {
                    // user logged out no group to scan
                    Log.d(TAG, "No Group to scan");
                } else if (miBandManager.getGroup() == null) {
                    Log.d(TAG, "Restarting Scan for saved group");
                    miBandManager.setGroup(group);
                } else {
                    miBandManager.stopScanning();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Log.e(TAG,"Sleep Interrupted", e);
                    }
                    miBandManager.startScanning();
                }
                mLooper = Looper.myLooper();
                Looper.loop();
            }
        }).start();
    }
}
