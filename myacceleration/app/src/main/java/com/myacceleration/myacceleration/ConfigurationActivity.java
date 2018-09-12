package com.myacceleration.myacceleration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.myacceleration.myacceleration.db.AppDatabase;
import com.myacceleration.myacceleration.db.Car;
import com.myacceleration.myacceleration.db.CarDao;
import com.myacceleration.myacceleration.db.User;
import com.myacceleration.myacceleration.db.UserRepository;
import com.myacceleration.myacceleration.rest.CarService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfigurationActivity extends AppCompatActivity {

    private static final String TAG = "ConfigurationActivity";
    private View mConfFormView;
    private View mProgressView;
    private LoadInitialDataTask mBrandsTask = null;
    private LoadCarsTask mCarsTask = null;
    private SaveConfTask msaveConfTask = null;
    private Spinner brandsSpinner;
    private Spinner carsSpinner;
    private EditText mNickNameView;

    private String mBrand;
    private List<Car> cars;
    private Car selectedCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.conf_toolbar);
        setSupportActionBar(myToolbar);

        brandsSpinner = (Spinner) findViewById(R.id.brands_spinner);
        carsSpinner = (Spinner) findViewById(R.id.models_spinner);
        mConfFormView = findViewById(R.id.conf_form);
        mProgressView = findViewById(R.id.brands_progress);
        mNickNameView = (EditText) findViewById(R.id.nick_name);

        showProgress(true);
        mBrandsTask = new LoadInitialDataTask();
        mBrandsTask.execute((Void) null);

        brandsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String name = (String)adapterView.getItemAtPosition(pos);
                Log.i(TAG, "selected brand--------->"+name);
                mBrand = name;
                showProgress(true);
                mCarsTask = new LoadCarsTask();
                mCarsTask.execute((Void) null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        carsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                selectedCar = cars.get(pos);
                Log.i(TAG, "selected car--------->"+selectedCar.getModel());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        Button configButton = (Button) findViewById(R.id.save_config);
        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msaveConfTask = new SaveConfTask();
                msaveConfTask.execute((Void) null);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conf_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_capture: {
                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivity);
                return true;
            }
            case R.id.action_list1: {
                //Toast.makeText(ConfigurationActivity.this, "Ranking", Toast.LENGTH_SHORT).show();
                Intent rankActivity = new Intent(getApplicationContext(), RankingActivity.class);
                startActivity(rankActivity);
                return true;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mConfFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mConfFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mConfFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mConfFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class LoadInitialDataTask extends AsyncTask<Void, Void, Boolean> {

        private Retrofit retrofit;
        private CarService service;

        LoadInitialDataTask() {
            retrofit = new Retrofit.Builder()
                    .baseUrl(MainActivity.SERVER)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                User u = UserRepository.getDefaultUser(getApplicationContext());
                if(u != null) {
                    mNickNameView.setText(u.getName());
                }
                Response<List<String>> response = loadBrandsFromServer();
                if (response.isSuccessful()) {
                    List<String> brands = response.body();
                    Log.i(TAG,"---------->"+brands.size());
                    ArrayAdapter adapter = new ArrayAdapter(ConfigurationActivity.this, android.R.layout.simple_spinner_item, brands);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    brandsSpinner.setAdapter(adapter);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return false;
            }
            return false;
        }

        private Response<List<String>> loadBrandsFromServer() throws IOException {
            service = retrofit.create(CarService.class);
            Call<List<String>> call = service.getBrands();
            return call.execute();
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            mBrandsTask = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mBrandsTask = null;
            showProgress(false);
        }
    }

    public class LoadCarsTask extends AsyncTask<Void, Void, List<Car>> {

        private Retrofit retrofit;
        private CarService service;

        LoadCarsTask() {
            retrofit = new Retrofit.Builder()
                    .baseUrl(MainActivity.SERVER)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        @Override
        protected List<Car> doInBackground(Void... params) {
            try {
                Response<List<Car>> response = loadCarsFromServer();
                if (response.isSuccessful()) {
                    return response.body();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return Collections.EMPTY_LIST;
            }
            return Collections.EMPTY_LIST;
        }

        private Response<List<Car>> loadCarsFromServer() throws IOException {
            service = retrofit.create(CarService.class);
            Call<List<Car>> call = service.getCars(mBrand);
            return call.execute();
        }

        @Override
        protected void onPostExecute(final List<Car> result) {
            cars = result;
            Log.i(TAG,"---------->"+cars.size());
            ArrayList<String> carNames = new ArrayList<>(cars.size());
            for(Car c : cars) {
                carNames.add(c.getModel());
            }
            ArrayAdapter adapter = new ArrayAdapter(ConfigurationActivity.this, android.R.layout.simple_spinner_item, carNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            carsSpinner.setAdapter(adapter);
            mBrandsTask = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mBrandsTask = null;
            showProgress(false);
        }
    }


    public class SaveConfTask extends AsyncTask<Void, Void, Boolean> {

        SaveConfTask() { }

        @Override
        protected Boolean doInBackground(Void... params) {
            saveConfiguration();
            return true;
        }

        private void saveConfiguration() {
            saveNickName();
            saveCar();
        }

        private void saveCar() {
            AppDatabase db = AppDatabase.getDatabase(ConfigurationActivity.this);
            CarDao carDao = db.carDao();
            carDao.deleteAll();
            carDao.insertAll(selectedCar);
        }

        private void saveNickName() {
            String name = mNickNameView.getText().toString();
            AppDatabase db = AppDatabase.getDatabase(ConfigurationActivity.this);
            db.userDao().update(name);
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            Log.i(TAG, "------------- zapis poprawny");
            if(result) {
                Toast.makeText(ConfigurationActivity.this,"Ustawienia zapisane", Toast.LENGTH_LONG).show();
            }
            msaveConfTask = null;
        }

        @Override
        protected void onCancelled() {
            msaveConfTask = null;
        }
    }
}
