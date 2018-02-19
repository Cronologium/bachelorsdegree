package ro.ubbcluj.cs.locationprovider;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import ro.ubbcluj.cs.locationprovider.receivers.AlarmReceiver;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class SettingsActivity extends AppCompatActivity {
    private final String PREFERENCES_FILE = "LocationProvider";
    private boolean toggled;
    private final String TAG = "SettingsActivity";
    private final int PERMISSION_LOCATION_REQUEST = 3124;
    private final int jobId = 1;
    private final int ONE_SECOND = 1000;
    private final int ONE_MINUTE = 60 * ONE_SECOND;
    private int minutes;
    private SharedPreferences locationProviderSettings;
    private JobScheduler jobScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        locationProviderSettings = getSharedPreferences(PREFERENCES_FILE, 0);
        toggled = locationProviderSettings.getBoolean("location", false);
        minutes = locationProviderSettings.getInt("interval", 1);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Starting the application with the provider on " + toggled);
        Log.d(TAG, "Alarm status: " + isAlarmActive());
        CheckBox check = findViewById(R.id.checkBox);
        check.setChecked(toggled);
        EditText editText = findViewById(R.id.editText);
        editText.setText(String.valueOf(minutes));
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

    public void scheduleAlarm() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_LOCATION_REQUEST);
                } else {
                    Toast.makeText(this, "Go to Settings->App->LocationProvider->Permissions and select location to true", Toast.LENGTH_LONG).show();
                }
                CheckBox check = findViewById(R.id.checkBox);
                check.setChecked(false);
                toggled = false;
                SharedPreferences.Editor editor = locationProviderSettings.edit();
                editor.putBoolean("location", toggled);
                editor.apply();
                return;
            }
        }
        if (isAlarmActive()) {
            Log.d(TAG, "Alarm is already active!");
        }
        final PendingIntent pIntent = getAlarmIntent();
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, (long) ONE_MINUTE * minutes, pIntent);
        Log.d(TAG, "Alarm has been scheduled!");
    }

    public void cancelAlarm() {
        if (!isAlarmActive()) {
            Log.d(TAG, "Alarm is already inactive");
        }
        final PendingIntent pIntent = getAlarmIntent();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        Log.d(TAG, "Alarm has been canceled");
    }

    private PendingIntent getAlarmIntent() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        return PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
