package com.myacceleration.myacceleration.login;

import android.content.Context;
import android.util.Log;

import com.myacceleration.myacceleration.MainActivity;
import com.myacceleration.myacceleration.db.AppDatabase;
import com.myacceleration.myacceleration.db.User;
import com.myacceleration.myacceleration.rest.UserService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterAndLoginFromServer extends LoginStrategy {

    private static final String TAG = "LoginFromServer";
    private Retrofit retrofit;
    private UserService service;

    public RegisterAndLoginFromServer() {
        retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(UserService.class);
    }

    @Override
    public void doLogin(Context context, String username, String password) {
        this.username = username;
        this.password = password;
        try {
            createNewAccount(context);
        } catch(Exception e) {
            Log.e(TAG, "--------------- błędna rejestracja uzytkownika do servera!", e);
            message = "Błąd przy rejestracji użytkownika. Spróbuj jeszcze raz za chwilę.";
        }
    }

    private void createNewAccount(Context context) throws IOException {
        Log.i(TAG, "------------> zpisuje nowego usera");
        registerNewUser(username, password);
        User registerdUser = ensureSaved(username);
        if (registerdUser != null) {
            saveToLocalDb(registerdUser, context);
            status = Status.REGISTRATION_SUCCESS;
        }
    }

    private void registerNewUser(String mEmail, String mPassword) throws IOException {
        Log.d(TAG, "--------------- zapis uzytkownika do servera!: " + mEmail);
        User userNew = new User();
        userNew.setLogin(mEmail);
        userNew.setPassword(mPassword);
        Call<Void> call = service.createUser(userNew);
        call.execute();
    }

    private User ensureSaved(String mEmail) throws IOException {
        Response<User> response = loadUserFromServer();
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
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
