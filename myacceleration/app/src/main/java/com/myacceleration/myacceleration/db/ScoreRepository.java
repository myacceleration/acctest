package com.myacceleration.myacceleration.db;

import android.content.Context;
import android.util.Log;

import java.util.List;

public class ScoreRepository {

    private static String TAG = "ScoreRepository";

    public static void saveScores(List<Score> scores, Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        Log.d(TAG,"--------------- zapisywanie pobranych wynikow: "+scores.size());
        db.scoreDao().insertAll(scores.toArray( new Score[scores.size()]));
    }

    public static void clearScores(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        Log.d(TAG,"--------------- czyszczenie wynikow: ");
        db.scoreDao().deleteAll();
    }
}
