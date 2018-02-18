package ro.ubbcluj.cs.locationprovider.database;

import android.app.job.JobParameters;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ro.ubbcluj.cs.locationprovider.domain.LocationObject;
import ro.ubbcluj.cs.locationprovider.service.OreoLocationProvider;

/**
 * Created by tudor on 17.02.2018.
 */

public class FirebaseLocationProvider implements LocationListener {
    private final String TAG = "FirebaseProvider";
    private DatabaseReference firebaseDatabase;
    private FirebaseUser user;
    private Thread thread;
    private LocationManager locationManager;
    private JobParameters params;
    private OreoLocationProvider oreoLocationProvider;

    public FirebaseLocationProvider() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "user: " + user.getEmail());
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("locations");
        Log.d(TAG, "Got location database");
        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.w(TAG, "Location was saved!");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "An error occured: " + databaseError.getMessage());
            }
        });
    }

    public void sendData(Thread thread, LocationManager locationManager) {
        this.thread = thread;
        this.locationManager = locationManager;
    }
    public void sendData(Thread thread, LocationManager locationManager, OreoLocationProvider oreoLocationProvider, JobParameters params) {
        this.thread = thread;
        this.locationManager = locationManager;
        this.oreoLocationProvider = oreoLocationProvider;
        this.params = params;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d(TAG, "Current location is " + location.getLongitude() + " -- " + location.getLatitude());
            LocationObject locationObject = new LocationObject(user.getEmail(), "" + location.getLatitude(), "" + location.getLongitude(), (new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss")).format(new Date()), Build.VERSION.SDK_INT);
            firebaseDatabase.child(user.getUid() + locationObject.getDateTime()).setValue(locationObject);
        }
        this.locationManager.removeUpdates(this);
        Log.d(TAG, "Attempting to close thread");
        if (oreoLocationProvider != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                oreoLocationProvider.jobFinished(params, false);
            }
        }
        this.thread.interrupt();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
