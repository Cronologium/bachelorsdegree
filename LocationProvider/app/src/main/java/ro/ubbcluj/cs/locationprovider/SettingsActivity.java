package ro.ubbcluj.cs.locationprovider;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import ro.ubbcluj.cs.locationprovider.receivers.AlarmReceiver;
import ro.ubbcluj.cs.locationprovider.service.LocationProvider;

public class SettingsActivity extends AppCompatActivity {
    private final String PREFERENCES_FILE = "LocationProvider";
    private boolean toggled;
    private final String TAG = "SettingsActivity";
    private final int PERMISSION_LOCATION_REQUEST = 3124;
    private final int ONE_SECOND = 1000;
    private final int ONE_MINUTE = 60 * ONE_SECOND;
    private int minutes;
    private SharedPreferences locationProviderSettings;
    private LocationManager locationManagerTester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        locationProviderSettings = getSharedPreferences(PREFERENCES_FILE, 0);
        toggled = locationProviderSettings.getBoolean("location", false);
        minutes = locationProviderSettings.getInt("interval", 1);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            finish();
        Log.d(TAG, "Starting the application with the provider on " + toggled);
        Log.d(TAG, "Alarm status: " + isAlarmActive());
        CheckBox check = findViewById(R.id.checkBox);
        check.setChecked(toggled);
        EditText editText = findViewById(R.id.editText);
        editText.setText(String.valueOf(minutes));
        EditText editText3 = findViewById(R.id.editText3);
        editText3.setText(String.valueOf(20));
        if (toggled) {
            scheduleAlarm();
        } else {
            cancelAlarm();
        }
    }

    private boolean isAlarmActive() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        return (null != PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE));
    }

    private void deactivateAndShowError(String message) {
        CheckBox check = findViewById(R.id.checkBox);
        check.setChecked(false);
        toggled = false;
        SharedPreferences.Editor editor = locationProviderSettings.edit();
        editor.putBoolean("location", toggled);
        editor.apply();
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void scheduleAlarm() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_LOCATION_REQUEST);
                    deactivateAndShowError(null);
                } else {
                    deactivateAndShowError("Go to Settings->App->LocationProvider->Permissions and select location to true");
                }
                return;
            }
        }
        locationManagerTester = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        try {
            gpsEnabled = locationManagerTester.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ignored) {

        }
        if (!gpsEnabled) {
            deactivateAndShowError("Please activate the gps service first!");
            return;
        }
        if (isAlarmActive()) {
            Log.d(TAG, "Alarm is already active!");
        }
        final PendingIntent pIntent = getAlarmIntent(-1);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, (long) ONE_MINUTE * minutes, pIntent);
        Log.d(TAG, "Alarm has been scheduled!");
    }

    public void cancelAlarm() {
        if (!isAlarmActive()) {
            Log.d(TAG, "Alarm is already inactive");
        }
        final PendingIntent pIntent = getAlarmIntent(-1);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        Log.d(TAG, "Alarm has been canceled");
    }

    private PendingIntent getAlarmIntent(long duration) {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        if (duration != -1)
            intent.putExtra("duration", duration);
        return PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void startBlitzkriegMode(View view) {
        Context context = getApplicationContext();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_LOCATION_REQUEST);
                } else {
                    Toast.makeText(context, "Go to Settings->App->LocationProvider->Permissions and select location to true", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        locationManagerTester = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        try {
            gpsEnabled = locationManagerTester.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ignored) {

        }
        if (!gpsEnabled) {
            Toast.makeText(context, "Please activate the gps service first!", Toast.LENGTH_SHORT).show();
            return;
        }
        long duration;
        EditText editText = findViewById(R.id.editText3);
        String minutesString = editText.getText().toString();
        duration = Integer.parseInt(minutesString);
        duration = Math.max(0, duration);
        final PendingIntent pIntent = getAlarmIntent(duration);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 15 * 1000, pIntent);
        Toast.makeText(this, "Please DO NOT KILL THE APP in the next " + duration + " minutes", Toast.LENGTH_LONG).show();
        finish();
    }

    public void switchToggle(View view) {
        toggled = !toggled;

        EditText editText = findViewById(R.id.editText);
        String minutesString = editText.getText().toString();
        if (minutesString.length() > 1) {
            minutesString = "10";
        } else if (minutesString.length() == 0) {
            minutesString = "1";
        }
        minutes = Integer.parseInt(minutesString);
        minutes = Math.max(1, Math.min(minutes, 10));
        editText.setText(String.valueOf(minutes));
        SharedPreferences.Editor editor = locationProviderSettings.edit();
        editor.putBoolean("location", toggled);
        editor.putInt("interval", minutes);
        editor.apply();
        if (toggled) {
            scheduleAlarm();
            if (toggled) {
                if (minutes > 1) {
                    Toast.makeText(getApplicationContext(), "Location will be fetched every " + minutes + " minutes!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Location will be fetched every " + minutes + " minute!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            cancelAlarm();
            Toast.makeText(getApplicationContext(), "Service is now off.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST: {
                if (grantResults.length > 1
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), "All permissions granted, please try again!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please give at least one permission!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
