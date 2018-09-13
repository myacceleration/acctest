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
import android.os.PersistableBundle;
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
import com.myacceleration.myacceleration.db.CarRepository;
import com.myacceleration.myacceleration.db.Score;
import com.myacceleration.myacceleration.db.ScoreDao;
import com.myacceleration.myacceleration.db.User;
import com.myacceleration.myacceleration.db.UserRepository;
import com.myacceleration.myacceleration.rest.CarService;
import com.myacceleration.myacceleration.rest.ScoreService;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
{
    public static final int MY_PREMISSION_LOCALIZATION = 10;
    private static final float KM_H_FAKTOR = 3.6f;
    public static final String USER_KEY = "ma_USER_KEY";
    public static final String USER_ID_KEY = "ma_USER_ID_KEY";
    public static final String CAR_ID_KEY = "ma_CAR_ID_KEY";
    public static final String CAR_KEY = "ma_CAR_KEY";
    //public static String SERVER = "http://192.168.1.23:8080/";
    //public static String SERVER = "http://192.168.43.13:8080/";
    public static String SERVER = "https://acctest33.herokuapp.com/";
    private static String TAG  = "myacceleration_MainActivity";
    private ToggleButton startBtn;
    private Button speedUpBtn, speedDownBtn;
    private Button saveScoreBtn;
    private TextView tvCarName;
    private TextView s, time, res, max;
    private LocationManager locationManager;
    private LocationListener listener;
    private float maxSpeed = 60;
    private boolean record;
    private ArrayList<Float> results = new ArrayList();

    private long timer1;
    private String mUsername = "";
    private String mCarname = "";
    private Integer mCarId;
    private Integer mUserId;

    private static float fakegpsValue = 0.0f;
    private LoadInitialDataTask mcarTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        startBtn = (ToggleButton) findViewById(R.id.startBtn);
        speedUpBtn = (Button) findViewById(R.id.speedUpBtn);
        speedDownBtn = (Button) findViewById(R.id.speedDownBtn);
        saveScoreBtn = (Button) findViewById(R.id.saveResult);

        s = (TextView) findViewById(R.id.textView2);
        time = (TextView) findViewById(R.id.textView4);
        res = (TextView) findViewById(R.id.textView6);
        max = (TextView) findViewById(R.id.textView7);
        tvCarName = (TextView) findViewById(R.id.car_name);

        mcarTask = new LoadInitialDataTask();
        mcarTask.execute((Void) null);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                final long timestamp = System.currentTimeMillis();
                // dla testow zakomentowane
                //float lSpeed = location.getSpeed() * KM_H_FAKTOR;
                // dla testow losujemy predkosc:
                float lSpeed = fakeGPS() * KM_H_FAKTOR;
                s.setText( round1Digit(lSpeed)+ " km/h");

                if (lSpeed <= 3.0f)
                {
                    record = true;
                    timer1 = timestamp;
                }

                float resultAccSeconds = (timestamp - timer1)/1000;
                if (record == true) {
                    time.setText("Czas: " + round3Digit(resultAccSeconds));
                } else {
                    time.setText("Zwolnij aby zacząć kolejną rundę");
                }
                Log.d(TAG, "speed changed:"+lSpeed + " | max:" +maxSpeed+ " | record:" +record + " | sec"+resultAccSeconds);

                if (lSpeed >= maxSpeed && record == true)
                {
                    record = false;
                    results.add(round3Digit(resultAccSeconds));
                    res.setText(displayResults(results));
                    timer1 = timestamp;
                }
            }

            private CharSequence displayResults(ArrayList<Float> results) {
                int no = 0;
                String text = "Wyniki:";
                for(Float result : results) {
                    no++;
                    text += "\n" + no + ". " + round1Digit(result) + " s";
                }
                return text;
            }

            private float fakeGPS() {
                if(fakegpsValue > maxSpeed/KM_H_FAKTOR) fakegpsValue = 0.0f;
                fakegpsValue += (float)Math.random();
                return fakegpsValue;
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

    private float round1Digit(float number) {
        return Math.round(number*10f)/10f;
    }

    private float round3Digit(float number) {
        return Math.round(number*1000f)/1000f;
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(listener);
        startBtn.setChecked(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(USER_KEY, mUsername);
        outState.putInt(USER_ID_KEY, mUserId);
        outState.putString(CAR_KEY, mCarname);
        outState.putInt(CAR_ID_KEY, mCarId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        mUsername = savedInstanceState.getString(USER_KEY);
        mUserId = savedInstanceState.getInt(USER_ID_KEY);
        mCarname = savedInstanceState.getString(CAR_KEY);
        mCarId = savedInstanceState.getInt(CAR_ID_KEY);
        super.onRestoreInstanceState(savedInstanceState, persistentState);
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
                Log.d(TAG, "----------------------- rank");
                Intent rankActivity = new Intent(getApplicationContext(), RankingActivity.class);
                startActivity(rankActivity);
                return true;
            }
            case R.id.action_settings: {
                Log.d(TAG, "----------------------- conf");
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
                    timer1 = System.currentTimeMillis();
                    record = false;
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

        saveScoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!results.isEmpty()) {
                //results.add(10.3f);
                    SaveScoreTask task = new SaveScoreTask(results);
                    task.execute();
                }
            }
        });
    }

    public class SaveScoreTask extends AsyncTask<Void, Void, Boolean> {

        private final Retrofit retrofit;
        private final ScoreService service;
        ArrayList<Float> results;
        SaveScoreTask(ArrayList<Float> results) {
            this.results = results;
            retrofit = new Retrofit.Builder()
                    .baseUrl(MainActivity.SERVER)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            service = retrofit.create(ScoreService.class);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Score s = new Score();
            s.setValue(bestValue(results));
            s.setCarId(Long.valueOf(mCarId));
            s.setUserId(Long.valueOf(mUserId));
            Call<Void> call = service.createScore(s);
            try {
                Log.d(TAG, "zapisu wyniku");
                Response<Void> response = call.execute();
                Log.d(TAG, "Odpowiedz serwera: "+ response.code()+" | "+response.message());
                if(! response.isSuccessful()) {
                    return false;
                }
            } catch (IOException e) {
                Log.e(TAG, "Błąd zapisu wyniku", e);
                return false;
            }
            return true;
        }

        private Float bestValue(ArrayList<Float> results) {
            Float min = Float.MAX_VALUE;
            for(Float f:results){
                if(f<min) min = f;
            }
            return min;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if(result == true){
                Toast.makeText(MainActivity.this, "Wynik został zapisany na serwerze", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Wystąpił problem z zapisem!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {  }
    }

    public class LoadInitialDataTask extends AsyncTask<Void, Void, Boolean> {

        LoadInitialDataTask() { }

        @Override
        protected Boolean doInBackground(Void... params) {
            loadCarDetails();
            loadUserDetails();
            tvCarName.setText(mCarname);
            return true;
        }

        private void loadCarDetails() {
            Car car = CarRepository.getDefaultCar(getApplicationContext());
            if(car != null) {
                mCarname = car.getManufacturer() + " " +car.getModel();
                mCarId = car.getId();
            }
        }

        private void loadUserDetails() {
            User user = UserRepository.getDefaultUser(getApplicationContext());
            if(user != null) {
                mUsername = TextUtils.isEmpty(user.getName()) ? user.getLogin() : user.getName();
                mUserId = user.getId();
            }
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            mcarTask = null;
        }

        @Override
        protected void onCancelled() {
            mcarTask = null;
        }
    }
}
