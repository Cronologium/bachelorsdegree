package ro.ubbcluj.cs.locationprovider.database;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ro.ubbcluj.cs.locationprovider.domain.LocationObject;

/**
 * Created by tudor on 17.02.2018.
 */

public class FirebaseLocationProvider implements LocationListener {
    private final String TAG = "FirebaseProvider";
    private DatabaseReference firebaseDatabase;
    private FirebaseUser user;
    private Thread thread;
    private LocationManager locationManager;
    private Date startDate;
    private long duration;

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

    public void sendData(Thread thread, LocationManager locationManager, long duration) {
        this.thread = thread;
        this.locationManager = locationManager;
        this.startDate = new Date();
        this.duration = duration;
    }

    @Override
    public void onLocationChanged(Location location) {
        long currentElapsedTime = TimeUnit.MINUTES.convert((new Date()).getTime() - this.startDate.getTime(),TimeUnit.MILLISECONDS);
        if (location != null) {
            //Log.d(TAG, "Current location is " + location.getLongitude() + " -- " + location.getLatitude());
            LocationObject locationObject = new LocationObject(location.getLatitude(), location.getLongitude(), (new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss")).format(new Date()), Build.VERSION.SDK_INT);
            Log.d(TAG, "Elapsed time since service started: " + currentElapsedTime + " time left:" + (duration - currentElapsedTime));
            Log.d(TAG, locationObject.toMap().toString());
            //firebaseDatabase.child(user.getUid() + locationObject.getDateTime()).setValue(locationObject);
        }
        if (currentElapsedTime >= duration) {
            this.locationManager.removeUpdates(this);
            Log.d(TAG, "Attempting to close thread");
            this.thread.interrupt();
        }
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
