package com.myacceleration.myacceleration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.sql.Timestamp;

public class MainActivity extends AppCompatActivity
{
    private Button b,b2,b3;
    private TextView t;
    private TextView s,t1,t2,res,max;
    private LocationManager locationManager;
    private LocationListener listener;
    private float maxSpeed = 15;
    private long resultAcc = 0;
    private int noResults = 0;
    private boolean record;

    private long timer1,timer2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t = (TextView) findViewById(R.id.textView);
        b = (Button) findViewById(R.id.button);
        b2 = (Button) findViewById(R.id.button2);
        b3 = (Button) findViewById(R.id.button3);

        s = (TextView) findViewById(R.id.textView2);

        t1 = (TextView) findViewById(R.id.textView4);
        t2 = (TextView) findViewById(R.id.textView5);

        res = (TextView) findViewById(R.id.textView6);

        max = (TextView) findViewById(R.id.textView7);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                float lSpeed = location.getSpeed();
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

        configure_button();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                locationManager.requestLocationUpdates("gps", 0, 0, listener);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maxSpeed -= 5;
                max.setText("max: " + maxSpeed);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maxSpeed += 5;
                max.setText("max: " + maxSpeed);
            }
        });
    }
}







