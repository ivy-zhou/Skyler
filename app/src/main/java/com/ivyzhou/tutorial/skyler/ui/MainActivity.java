package com.ivyzhou.tutorial.skyler.ui;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ivyzhou.tutorial.skyler.R;
import com.ivyzhou.tutorial.skyler.SkyAlgorithm;
import com.ivyzhou.tutorial.skyler.weather.Forecast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
                    LocationListener{

    private Date curDate;
    private Forecast currentWeather;
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    public final static int REQUEST_LOCATION = 728;

    public final static int REQUEST_TAKE_PHOTO = 912;
    private SkyAlgorithm skyMaker;

    //private RelativeLayout background = null; //= (RelativeLayout)findViewById(R.id.background);

    @BindView(R.id.degreeImage) ImageView degreeImage;
    @BindView(R.id.humidityLabel) TextView humidityLabel;
    @BindView(R.id.locationLabel) TextView locationLabel;
    @BindView(R.id.humidityValue) TextView humidityValue;
    @BindView(R.id.precipLabel) TextView precipLabel;
    @BindView(R.id.precipValue) TextView precipValue;
    @BindView(R.id.timeLabel) TextView timeLabel;
    @BindView(R.id.summary) TextView summary;
    @BindView(R.id.temperature) TextView temperature;
    @BindView(R.id.weatherIcon) ImageView weatherIcon;
    @BindView(R.id.refreshButton) ImageView refreshButton;
    @BindView(R.id.forecastProgress) ProgressBar forecastProgress;
    @BindView(R.id.windValue) TextView windValue;
    @BindView(R.id.cameraButton) ImageView cameraButton;
    @BindView(R.id.background) RelativeLayout background;
    @BindView(R.id.seekBar) SeekBar hourSelect;
    @BindView(R.id.dailyButton) Button dailyButton;

    // location based shenanigans
    private GoogleApiClient GoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (savedInstanceState != null)
            background.setBackgroundColor(savedInstanceState.getInt("backgroundColor"));
        else
            background.setBackgroundColor(Color.rgb(252, 151, 11));


        // Build Google API client
        GoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        forecastProgress.setVisibility(View.INVISIBLE);
        Log.d(TAG, "Main UI is running");
    }

    /* Main activity convenience methods */
    public void toggleRefresh() {
        if (forecastProgress.getVisibility() == View.INVISIBLE) {
            forecastProgress.setVisibility(View.VISIBLE);
            refreshButton.setVisibility(View.INVISIBLE);
        } else {
            forecastProgress.setVisibility(View.INVISIBLE);
            refreshButton.setVisibility(View.VISIBLE);
        }

    }

    public void updateDisplay() {
        if(currentWeather != null){
            JSONObject currently = currentWeather.getCurrently();
            JSONObject hourly = currentWeather.getHourly();

            // format information from currently
            try {
                // format time
                SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
                formatter.setTimeZone(TimeZone.getTimeZone(currentWeather.getTimezone()));
                String timeString = formatter.format(currentWeather.getDate());

                // convert to celsius
                double temperature = currently.getDouble("temperature");
                String tempString = (int) ((5.0 / 9) * (Math.round(temperature) - 32.0)) + "";

                String precipString = currently.getDouble("precipProbability") * 100 + "";

                String humidityString = currently.getDouble("humidity") + "";

                String summaryString = currently.getString("summary");

                // rounding
                double windSpeed = currently.getDouble("windSpeed");
                String windSpeedString = Math.round(windSpeed * 10) / 10.0 + "";

                this.temperature.setText(tempString);
                this.humidityValue.setText(humidityString);
                this.precipValue.setText(precipString);
                this.summary.setText(summaryString);
                this.timeLabel.setText("At " + timeString + " it will be");
                this.windValue.setText(windSpeedString + " mph");
                this.weatherIcon.setImageDrawable(getResources()
                        .getDrawable(Forecast.getIconID(currentWeather.getIconString())));
                this.locationLabel.setText(currentWeather.getLocation());

                // set the hourly weather UI


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        if(skyMaker != null)
            this.background.setBackgroundColor(skyMaker.getColorResult());
    }

    public boolean networkIsAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected())
            isAvailable = true;
        return isAvailable;
    }

    public void showDialog(String title, String message) {
        DialogFragment newFragment = AlertDialogFragment.newInstance(
                title, message);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /* Location override methods */
    @Override
    protected void onResume() {
        super.onResume();
        GoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (GoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(GoogleApiClient, this);
            GoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
            return;
        }
        // get the last known location
        Location location = LocationServices.FusedLocationApi.getLastLocation(GoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(GoogleApiClient, locationRequest, this);
        }
        else {
            handleNewLocation(location);
        };
    }

    // create a new current weather object to handle forecasts for this new location
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // extract the name of the town
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 5);
            if(addresses.size() >= 1) {
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String locationName = city + ", " + state;
                currentWeather = new Forecast(latitude, longitude, locationName,
                                                    this, MainActivity.this);
                //http://api.sunrise-sunset.org/json?lat=36.7201600&lng=-4.4203400&date=today
                currentWeather.getForecast();
                currentWeather.getSkyTimes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error (default Google handling)
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    // on click listeners
    @OnClick(R.id.refreshButton)
    public void refresh() {
        if(currentWeather != null)
            currentWeather.getForecast();
    }

    @OnClick(R.id.dailyButton)
    public void startDaily() {
        Intent dailyIntent = new Intent(this, DailyForecastActivity.class);
        dailyIntent.putExtra(DAILY_FORECAST, currentWeather.getDailyForecast());
        startActivity(dailyIntent);
    }

    @OnClick(R.id.timeLabel)
    public void toggleHourSelect() {
        if(hourSelect.getVisibility() == View.VISIBLE)
            hourSelect.setVisibility(View.INVISIBLE);
        else
            hourSelect.setVisibility(View.VISIBLE);
    }

    // start camera activity when prompted
    @OnClick(R.id.cameraButton)
    public void getSkyPhoto() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        if (takePhoto.resolveActivity(getPackageManager()) != null) {
            Toast.makeText(this, "Take a photo of the sky!", Toast.LENGTH_LONG).show();
            startActivityForResult(takePhoto, REQUEST_TAKE_PHOTO);
        }
    }

    // work on only the thumbnail
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            skyMaker = new SkyAlgorithm(imageBitmap, currentWeather);
            updateDisplay();
        }
    }

    // save the color value
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        int color = Color.TRANSPARENT;
        Drawable background_drawable = background.getBackground();
        if (background_drawable instanceof ColorDrawable)
            color = ((ColorDrawable) background_drawable).getColor();
        outState.putInt("backgroundColor", color);
    }
}
