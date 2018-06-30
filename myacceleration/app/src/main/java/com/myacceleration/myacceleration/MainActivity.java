package com.myacceleration.myacceleration;

import android.Manifest;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.myacceleration.myacceleration.db.AppDatabase;

import java.sql.Timestamp;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    //public static String SERVER = "http://192.168.1.23:8080/";
    public static String SERVER = "https://acctest33.herokuapp.com/";
    private static String TAG  = "myacceleration_MainActivity";
    private ToggleButton startBtn;
    private Button speedUpBtn, speedDownBtn;
    private TextView t;
    private TextView s,t1,t2,res,max;
    private LocationManager locationManager;
    private LocationListener listener;
    private float maxSpeed = 15;
    private long resultAcc = 0;
    private int noResults = 0;
    private boolean record;

    private long timer1,timer2;
    private UserCheckTask mAuthTask;

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
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configureButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuthTask = new UserCheckTask();
        mAuthTask.execute((Void) null);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configureButton();
                break;
            default:
                break;
        }
    }

    void configureButton(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        startBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

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


    public class UserCheckTask extends AsyncTask<Void, Void, Boolean> {

        UserCheckTask() { }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(userExistsLocalDb()) {
                Log.d(TAG, "user w cache, autologwanie");
                return true;
            }
            return false;
        }

        private boolean userExistsLocalDb() {
            AppDatabase db = AppDatabase.getDatabase(MainActivity.this);
            List users = db.userDao().getAll();
            Log.d(TAG,"--------------- pobranie z cache: "+users.size());
            return users.size() > 0;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (!success) {
                Log.d(TAG, "logowanie manualne start");
                Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginActivity);
            }
        }

        @Override
        protected void onCancelled() { mAuthTask = null; }
    }
}







