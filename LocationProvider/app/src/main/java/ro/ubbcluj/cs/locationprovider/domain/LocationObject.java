package ro.ubbcluj.cs.locationprovider.domain;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tudor on 15.02.2018.
 */

@IgnoreExtraProperties
public class LocationObject {
    private double latitude;
    private double longitude;
    private String dateTime;
    private int apiLevel;

    public LocationObject(double latitude, double longitude, String dateTime, int apiLevel) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
        this.apiLevel = apiLevel;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(int apiLevel) {
        this.apiLevel = apiLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationObject that = (LocationObject) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (apiLevel != that.apiLevel) return false;
        return dateTime != null ? dateTime.equals(that.dateTime) : that.dateTime == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (dateTime != null ? dateTime.hashCode() : 0);
        result = 31 * result + apiLevel;
        return result;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("longitude", longitude);
        result.put("latitude", latitude);
        result.put("datetime", dateTime);
        result.put("api", apiLevel);
        return result;
    }
}
