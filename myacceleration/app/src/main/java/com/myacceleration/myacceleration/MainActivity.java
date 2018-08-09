package com.myacceleration.myacceleration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.myacceleration.myacceleration.db.AppDatabase;
import com.myacceleration.myacceleration.db.Car;
import com.myacceleration.myacceleration.db.User;

import java.sql.Timestamp;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    public static final int MY_PREMISSION_LOCALIZATION = 10;
    //public static String SERVER = "http://192.168.1.23:8080/";
    public static String SERVER = "http://192.168.43.13:8080/";
    //public static String SERVER = "https://acctest33.herokuapp.com/";
    private static String TAG  = "myacceleration_MainActivity";
    private ToggleButton startBtn;
    private Button speedUpBtn, speedDownBtn;
    private TextView t;
    private TextView tvCarName;
    private TextView s,t1,t2,res,max;
    private LocationManager locationManager;
    private LocationListener listener;
    private float maxSpeed = 15;
    private long resultAcc = 0;
    private int noResults = 0;
    private boolean record;

    private long timer1,timer2;
    private String mUsername = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        t = (TextView) findViewById(R.id.textView);
        startBtn = (ToggleButton) findViewById(R.id.startBtn);
        speedUpBtn = (Button) findViewById(R.id.speedUpBtn);
        speedDownBtn = (Button) findViewById(R.id.speedDownBtn);

        s = (TextView) findViewById(R.id.textView2);

        t1 = (TextView) findViewById(R.id.textView4);
        t2 = (TextView) findViewById(R.id.textView5);
        res = (TextView) findViewById(R.id.textView6);
        max = (TextView) findViewById(R.id.textView7);
        tvCarName = (TextView) findViewById(R.id.car_name);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "location changed");
                final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                // dla testow zakomentowane
                //float lSpeed = location.getSpeed();
                // dla testow losujemy predkosc:
                float lSpeed = 1+((float)Math.random()*20);

                t.setText("\n " + location.getLongitude() + "\n" + location.getLatitude());
                s.setText( lSpeed + "m/s");
                if (lSpeed <= 0.5)
                {
                    record = true;
                    timer1 = timestamp.getTime();
                    timer2 = 0;
                    t1.setText("t1: " + timer1);
                    t2.setText("t2: " + timer2);
                }

                if (lSpeed >= maxSpeed && record == true)
                {
                    record = false;
                    timer2 = timestamp.getTime();
                    t2.setText("t2: " + timer2);
                    resultAcc = timer2 - timer1;
                    noResults++;
                    res.append("\n[" + noResults + "]: " + resultAcc);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) { }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        if (savedInstanceState != null) {
            mUsername = savedInstanceState.getString("user");
        }
        configureButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(listener);
        startBtn.setChecked(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("user", mUsername);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rank: {
                Toast.makeText(MainActivity.this, "Ranking", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.action_settings: {
                Intent configurationActivity = new Intent(getApplicationContext(), ConfigurationActivity.class);
                startActivity(configurationActivity);
                return true;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PREMISSION_LOCALIZATION:
                Log.i(TAG, "grantResults.length-------------------->"+grantResults.length);
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "uprawnienia dodane");
                } else {
                    Intent configurationActivity = new Intent(getApplicationContext(), ConfigurationActivity.class);
                    startActivity(configurationActivity); // TODO do rankingow moze?
                }
                break;
            default:
                break;
        }
    }

    void configureButton(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}
                        , MY_PREMISSION_LOCALIZATION);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        startBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    locationManager.removeUpdates(listener);
                    Log.d(TAG, "zbieranie gps wylaczone");
                } else {
                    Log.d(TAG, "odpalamy");
                    locationManager.requestLocationUpdates("gps", 0, 0, listener);
                }
            }
        });

        speedUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maxSpeed -= 5;
                max.setText("max: " + maxSpeed);
            }
        });

        speedDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maxSpeed += 5;
                max.setText("max: " + maxSpeed);
            }
        });
    }
}







