package com.myacceleration.myacceleration.login;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.myacceleration.myacceleration.MainActivity;
import com.myacceleration.myacceleration.db.AppDatabase;
import com.myacceleration.myacceleration.db.Car;
import com.myacceleration.myacceleration.db.User;
import com.myacceleration.myacceleration.rest.UserService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginFromServer extends LoginStrategy {

    private static final String TAG = "LoginFromServer";
    private Retrofit retrofit;
    private UserService service;

    public LoginFromServer() {
        retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Override
    public void doLogin(Context context, String username, String password) {
        this.username = username;
        this.password = password;
        try {
            Response<User> response = loadUserFromServer();
            if (response.isSuccessful()) {
                User u = response.body();
                if (password.equals(u.getPassword())) {
                    Log.i(TAG, "zalogowany ------------>" + u.getName());
                    saveToLocalDb(u, context);
                    status = Status.LOGIN_SUCCESS;
                } else {
                    message = "Hasło niepoprawne.";
                }
            } else if (response.code() == 404) {
                status = Status.TO_REGISTER;
            }
        } catch (Exception e) {
            message = "Błąd przy logowaniu użytkownika przez sieć. Spróbuj jeszcze raz za chwilę.";
            Log.e(TAG, "problem z zaladowaniem uzytkownika");
        }
    }

    private Response<User> loadUserFromServer() throws IOException {
        service = retrofit.create(UserService.class);
        Call<User> call = service.getUser(username);
        return call.execute();
    }

    private void saveToLocalDb(User u, Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        db.userDao().insertAll(u);
        Log.d(TAG, "--------------- zapis uzytkownika do cache: " + u.getLogin());
    }
}
