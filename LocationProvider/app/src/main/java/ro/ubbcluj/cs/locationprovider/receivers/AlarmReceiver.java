package ro.ubbcluj.cs.locationprovider.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import ro.ubbcluj.cs.locationprovider.service.LocationProvider;

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
