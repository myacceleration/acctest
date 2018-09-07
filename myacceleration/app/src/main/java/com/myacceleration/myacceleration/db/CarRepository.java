package com.myacceleration.myacceleration.db;

import android.content.Context;
import android.util.Log;

import java.util.List;

public class CarRepository {

    private static String TAG = "CarRepository";

    public static Car getDefaultCar(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        List<Car> cars = db.carDao().getAll();
        Log.d(TAG,"--------------- pobranie pojazdu z cache: "+cars.size());
        if(cars.size() == 1) {
            return cars.get(0);
        }
        return null;
    }
}
