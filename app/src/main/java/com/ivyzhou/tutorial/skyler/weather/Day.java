package com.ivyzhou.tutorial.skyler.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Ivy Zhou on 7/24/2016.
 */
public class Day implements Parcelable{
    public long time;
    public String summary;
    public double tempMax;
    private String icon;
    private String timezone;

    public Day() {}

    public static final Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel in) {
            return new Day(in);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getTempMax() {
        return (int) Math.round(tempMax);
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getIconId() {
        return Forecast.getIconID(icon);
    }

    public String getDayOfWeek() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        formatter.setTimeZone(TimeZone.getTimeZone(timezone));
        Date datetime = new Date(time * 1000);
        return formatter.format(datetime);
    }

    @Override
    public int describeContents() {
        return 0; // not needed
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(time);
        parcel.writeString(summary);
        parcel.writeDouble(tempMax);
        parcel.writeString(icon);
        parcel.writeString(timezone);
    }

    private Day (Parcel in) {
        time = in.readLong();
        summary = in.readString();
        tempMax = in.readDouble();
        icon = in.readString();
        timezone = in.readString();
    }
}
