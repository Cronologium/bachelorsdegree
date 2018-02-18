package ro.ubbcluj.cs.locationprovider.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import ro.ubbcluj.cs.locationprovider.database.FirebaseLocationProvider;

/**
 * Created by tudor on 17.02.2018.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class OreoLocationProvider extends JobService {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Thread serviceThread;
    private final String TAG = "OLocProvider";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.w(TAG, "Preparing to fetch location!");
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new FirebaseLocationProvider();
        if (ActivityCompat.checkSelfPermission(OreoLocationProvider.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(OreoLocationProvider.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing permissions");
            return false;
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
        serviceThread = new Thread(runnable);
        ((FirebaseLocationProvider) locationListener).sendData(serviceThread, locationManager, this, jobParameters);
        serviceThread.start();
        Log.w(TAG, "Location manager is done!");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.w(TAG,"Job must be stopped!");
        try {
            serviceThread.join(0);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }
}
