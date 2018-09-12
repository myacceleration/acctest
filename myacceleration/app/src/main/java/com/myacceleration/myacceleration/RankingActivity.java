package com.myacceleration.myacceleration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.myacceleration.myacceleration.db.Car;
import com.myacceleration.myacceleration.db.CarRepository;
import com.myacceleration.myacceleration.db.Score;
import com.myacceleration.myacceleration.db.ScoreRepository;
import com.myacceleration.myacceleration.rest.CarService;
import com.myacceleration.myacceleration.rest.ScoreService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RankingActivity extends AppCompatActivity implements ScoreFragment.OnListFragmentInteractionListener {

    private static final String TAG = "RankingActivity";
    private static final String CAR_ID = "CAR_ID_BUNDLE";
    private Integer mCarId;
    private LoadScoresTask mScoreTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.rank_toolbar);
        setSupportActionBar(myToolbar);

        mScoreTask = new RankingActivity.LoadScoresTask();
        mScoreTask.execute((Void) null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt(CAR_ID, mCarId);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        mCarId = savedInstanceState.getInt(CAR_ID);
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rank_menu, menu);
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
            case R.id.action_settings: {
                Toast.makeText(RankingActivity.this, "Ustawienia", Toast.LENGTH_SHORT).show();
                Intent configurationActivity = new Intent(getApplicationContext(), ConfigurationActivity.class);
                startActivity(configurationActivity);
                return true;
            }
        }
        return true;
    }

    @Override
    public void onListFragmentInteraction(Score item) {  }


    public class LoadScoresTask extends AsyncTask<Void, Void, Boolean> {

        private Retrofit retrofit;
        private ScoreService service;

        LoadScoresTask() {
            retrofit = new Retrofit.Builder()
                    .baseUrl(MainActivity.SERVER)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Car c = CarRepository.getDefaultCar(getApplicationContext());
                if(c != null) {
                    Log.d(TAG, "-----Pobieram wyniki dla pojazdu:"+c.getModel());
                    mCarId = c.getId();
                } else {
                    Log.d(TAG, "-----Brak pojazdu w cache!!!");
                    return false;
                }
                Response<List<Score>> response = loadCarsFromServer(mCarId);
                if (response.isSuccessful()) {
                    List<Score> scores =  response.body();
                    Log.d(TAG, "-----Pobrane wyniki:"+scores.size());
                    ScoreRepository.clearScores(getApplicationContext());
                    ScoreRepository.saveScores(scores,getApplicationContext());
                } else {
                    return false;
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return false;
            }
            return true;
        }

        private Response<List<Score>> loadCarsFromServer(Integer carId) throws IOException {
            service = retrofit.create(ScoreService.class);
            Call<List<Score>> call = service.getScores(carId.longValue());
            return call.execute();
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            mScoreTask = null;
        }

        @Override
        protected void onCancelled() {
            mScoreTask = null;
        }
    }
}
