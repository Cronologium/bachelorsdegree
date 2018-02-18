package ro.ubbcluj.cs.locationprovider.receivers;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import ro.ubbcluj.cs.locationprovider.service.LocationProvider;
import ro.ubbcluj.cs.locationprovider.service.OreoLocationProvider;

/**
 * Created by tudor on 15.02.2018.
 */

public class AlarmReceiver extends BroadcastReceiver{
    public static final int REQUEST_CODE = 12345;
    private final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, LocationProvider.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }
}
