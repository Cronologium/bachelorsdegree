package ro.ubbcluj.cs.locationprovider.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.sql.Timestamp;

import ro.ubbcluj.cs.locationprovider.R;
import ro.ubbcluj.cs.locationprovider.database.FirebaseLocationProvider;

import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;

/**
 * Created by tudor on 15.02.2018.
 */

public class LocationProvider extends IntentService {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final String TAG = "LocationProviderService";

    public LocationProvider() {
        super("location-provider");
    }

    public LocationProvider(String name) {
        super(name);
    }

    public void stop() {
        locationManager.removeUpdates(locationListener);
        Looper looper = Looper.myLooper();
        if (looper != null) {
            looper.quit();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(this, DEFAULT_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Using location service.")
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(1, notification);
        } else {
            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Using location service.")
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(1, notification);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(TAG, "Preparing to fetch location!");
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new FirebaseLocationProvider();
        boolean gps_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {

        }
        if (!gps_enabled) {
            Log.d(TAG, "Gps status: " + gps_enabled);
            return;
        }
        Runnable runnable = new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                Looper.prepare();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
                Looper.loop();
            }
        };
        Thread thread = new Thread(runnable);
        ((FirebaseLocationProvider) locationListener).sendData(thread, locationManager);
        thread.start();
        Log.d(TAG, "Location manager is done!");
    }
}
