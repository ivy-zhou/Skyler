package com.ivyzhou.tutorial.skyler.weather;

import com.ivyzhou.tutorial.skyler.ui.MainActivity;
import com.ivyzhou.tutorial.skyler.R;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Ivy Zhou on 7/16/2016.
 * Makes HTTP requests for the forecast and stores them as JSON
 */
public class Forecast {
    public static final String TAG = Forecast.class.getSimpleName();

    private static final String apiKey = "9a7225a89a321830f46edadb00844325";

    private double longitude;
    private double latitude;
    private String location;

    private String timezone;

    private Date curDate;
    private JSONObject currently;
    private String icon;

    private JSONObject hourly;
    private JSONObject daily;
    private Hour[] hours;
    private Day[] days;

    private static Context context;
    private MainActivity activityInstance;


    // call me when you have your latitude and longitude figured out, or I can't make a request
    // the location thing is a bonus
    public Forecast(double latitude, double longitude, String location,
                    Context context, MainActivity mainActivity) {
        // location, longitude and latitude must be given to me
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;

        this.context = context;
        this.activityInstance = mainActivity;
    }

    // makes an HTTP request for the current forecast
    public void getForecast() {
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        if (activityInstance.networkIsAvailable()) {
            activityInstance.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activityInstance.toggleRefresh();
                }
            });

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastUrl).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    activityInstance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityInstance.toggleRefresh();
                        }
                    });
                    activityInstance.showDialog("Rats!", "Sparky's request failed, please try again!");
                    Log.e(TAG, "Failed response: ", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    activityInstance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityInstance.toggleRefresh();
                        }
                    });
                    try {
                        // add button for try again
                        if (response.isSuccessful()) {
                            // read all jsondata, then update the display
                            String jsonData = response.body().string();
                            readJSON(jsonData);
                            activityInstance.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activityInstance.updateDisplay();
                                }
                            });
                        } else
                            activityInstance.showDialog(
                                    "Rats!", "The network response was unsuccessful, so Sparks got sad." +
                                    " Please try again!");

                    } catch (IOException e) {
                        Log.e(TAG, "Exception in checking if response was successful: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON exception: ", e);
                    }
                }
            });
        } else
            activityInstance.showDialog("Rats!", "There is no network, so Sparks got sad." +
                    " Please try again!");
    }

    private void readJSON(String jsonData) throws JSONException {
        JSONObject json = new JSONObject(jsonData);
        JSONObject currently = json.getJSONObject("currently");

        timezone = json.getString("timezone");
        icon = currently.getString("icon");
        this.currently = currently; // all other weather information should be stored as JSON
        long time = currently.getLong("time"); // except the time, which is frequently used
        curDate = new Date(time * 1000);

        // parse hourly weather
        hourly = json.getJSONObject("hourly");
        JSONArray hourlyData = hourly.getJSONArray("data");
        hours = new Hour[hourlyData.length()];

        for(int i = 0; i < hourlyData.length(); i++){
            JSONObject jsonHour = hourlyData.getJSONObject(i);

            Hour hour = new Hour();
            hour.setSummary(jsonHour.getString("summary"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTimezone(timezone);
            hour.setTime(jsonHour.getLong("time"));
            hour.setTemperature(jsonHour.getDouble("temperature"));

            hours[i] = hour;
        }

        // parse daily data
        daily = json.getJSONObject("daily");
        JSONArray dailyData = daily.getJSONArray("data");
        days = new Day[dailyData.length()];

        for(int i = 0; i < dailyData.length(); i++){
            JSONObject jsonDay = dailyData.getJSONObject(i);

            Day day = new Day();
            day.setIcon(jsonDay.getString("icon"));
            day.setSummary(jsonDay.getString("summary"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);
            day.setTempMax(jsonDay.getDouble("temperatureMax"));

            days[i] = day;
        }

    }

    public JSONObject getHourly() {return hourly;}

    /* Getters used in updateDisplay */
    public static int getIconID(String icon) {
        // clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night
        int iconID =  context.getResources().getIdentifier(icon.replaceAll("-", "_"), "drawable", context.getPackageName());
        if(iconID == 0)
            iconID = R.drawable.clear_day;
        return iconID;
    }

    public String getIconString() {
        return this.icon;
    }

    public Day [] getDailyForecast() {
        return days;
    }

    public JSONObject getCurrently() {
        return currently;
    }

    public String getTimezone() {return timezone;}

    public Date getDate() {
        return curDate;
    }

    public String getLocation() {return location;}

    // makes an HTTP request for sunrise and sunset times
    public void getSkyTimes() {
        //http://api.sunrise-sunset.org/json?lat=36.7201600&lng=-4.4203400&date=today
        String skyURL = "http://api.sunrise-sunset.org/json?lat=" +
                latitude + "&lng=" + longitude + "&date=today";

        if (activityInstance.networkIsAvailable()) {
            activityInstance.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activityInstance.toggleRefresh();
                }
            });

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(skyURL).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    activityInstance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityInstance.toggleRefresh();
                        }
                    });
                    activityInstance.showDialog("Rats!", "Sparky's request failed, please try again!");
                    Log.e(TAG, "Failed response: ", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    activityInstance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityInstance.toggleRefresh();
                        }
                    });
                    try {
                        // add button for try again
                        if (response.isSuccessful()) {
                            // read all jsondata, then update the display
                            String jsonData = response.body().string();
                            //readJSON(jsonData);
                            activityInstance.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //activityInstance.updateDisplay();
                                }
                            });
                        } else
                            activityInstance.showDialog(
                                    "Rats!", "The network response was unsuccessful, so Sparks got sad." +
                                            " Please try again!");

                    } catch (IOException e) {
                        Log.e(TAG, "Exception in checking if response was successful: ", e);
                    }
                }
            });
        } else
            activityInstance.showDialog("Rats!", "There is no network, so Sparks got sad." +
                    " Please try again!");
    }
}
