package com.myacceleration.myacceleration;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.myacceleration.myacceleration.db.AppDatabase;
import com.myacceleration.myacceleration.db.Score;

import java.util.List;

public class ScoresViewModel extends AndroidViewModel {

    private final LiveData<List<Score>> scores;

    public ScoresViewModel(@NonNull Application application) {
        super(application);

        scores = AppDatabase
                .getDatabase(getApplication())
                .scoreDao()
                .loadAll();
    }

    public LiveData<List<Score>> getScores() {
        return scores;
    }
}

