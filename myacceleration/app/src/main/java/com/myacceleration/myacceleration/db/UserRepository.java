package com.myacceleration.myacceleration.db;

import android.content.Context;
import android.util.Log;

import java.util.List;

public class UserRepository {

    private static String TAG = "UserRepository";

    public static User getDefaultUser(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        List<User> users = db.userDao().getAll();
        Log.d(TAG,"--------------- pobranie z cache: "+users.size());
        if(users.size() == 1) {
            return users.get(0);
        }
        return null;
    }
}
