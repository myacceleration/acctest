package com.myacceleration.myacceleration.login;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.myacceleration.myacceleration.db.AppDatabase;
import com.myacceleration.myacceleration.db.Car;
import com.myacceleration.myacceleration.db.User;

import java.util.List;

public class LoginFromCache extends LoginStrategy {

    private static final String TAG = "LoginFromCache";

    @Override
    public void doLogin(Context context, String username, String password) {
        loadUserFromLocalDb(context);
        loadCarFromLocalDb(context);
    }

    private void loadUserFromLocalDb(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        List<User> users = db.userDao().getAll();
        Log.d(TAG,"--------------- pobranie z cache: "+users.size());
        if(users.size() == 1) {
            username = users.get(0).getName();
            Log.d(TAG,"--------------- pobranie z cache name: "+username);
            username = TextUtils.isEmpty(username) ? users.get(0).getLogin() : username;
            status = Status.LOGIN_SUCCESS;
        }
    }

    private void loadCarFromLocalDb(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        List<Car> cars = db.carDao().getAll();
        Log.d(TAG,"--------------- pobranie pojazdu z cache: "+cars.size());
        if(cars.size() == 1) {
            carname = cars.get(0).getManufacturer() + " " + cars.get(0).getModel();
            Log.d(TAG,"--------------- pobranie pojazdu z cache name: "+carname);
            status = Status.LOGIN_SUCCESS;
        }
    }
}
