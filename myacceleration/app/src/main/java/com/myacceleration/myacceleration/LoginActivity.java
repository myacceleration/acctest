package com.myacceleration.myacceleration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myacceleration.myacceleration.db.AppDatabase;
import com.myacceleration.myacceleration.db.User;
import com.myacceleration.myacceleration.rest.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static String TAG = "myacceleration_LoginActivity";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }

        Log.d(TAG, ".................. koniec login");
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public enum LoginResult { LOGIN, REGISTER, ERROR }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, LoginResult> {

        private final String mEmail;
        private final String mPassword;
        private Retrofit retrofit;
        private UserService service;
        private User registerdUser;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            retrofit = new Retrofit.Builder()
                    .baseUrl(MainActivity.SERVER)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        @Override
        protected LoginResult doInBackground(Void... params) {
            try {
                Response<User> response = loadUserFromServer();
                if (response.isSuccessful()) {
                    User u = response.body();
                    if (mPassword.equals(u.getPassword())) {
                        Log.i(TAG, "zalogowany ------------>" + u.getName());
                        saveToLocalDb(u);
                        return LoginResult.LOGIN;
                    }
                } else if (response.code() == 404) {
                    return createNewAccount();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return LoginResult.ERROR;
            }
            return LoginResult.ERROR;
        }

        private Response<User> loadUserFromServer() throws IOException {
            service = retrofit.create(UserService.class);
            Call<User> call = service.getUser(mEmail);
            return call.execute();
        }

        private LoginResult createNewAccount() throws IOException {
            Log.i(TAG, "------------> zpisuje nowego usera");
            registerNewUser(mEmail, mPassword);
            registerdUser = ensureSaved(mEmail);
            if (registerdUser != null) {
                saveToLocalDb(registerdUser);
                return LoginResult.REGISTER;
            }
            return LoginResult.ERROR;
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
            if(response.isSuccessful()) {
                return response.body();
            }
            return null;
        }

        private void saveToLocalDb(User u) {
            AppDatabase db = AppDatabase.getDatabase(LoginActivity.this);
            db.userDao().insertAll(u);
            Log.d(TAG, "--------------- zapis uzytkownika do cache: " + u.getLogin());
        }

        @Override
        protected void onPostExecute(final LoginResult result) {
            mAuthTask = null;
            showProgress(false);

            if (result == LoginResult.LOGIN) {
                Log.d(TAG, "logowanie poprawne");
                Toast.makeText(LoginActivity.this, "Zostałeś zalogowany!", Toast.LENGTH_LONG).show();
                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivity);
            } else if(result == LoginResult.REGISTER){
                Log.d(TAG, "rejestracja poprawna");
                Toast.makeText(LoginActivity.this, "Nowy użytkownik "+registerdUser.getLogin()+" zarejestrowany!", Toast.LENGTH_LONG).show();
                Intent configurationActivity = new Intent(getApplicationContext(), ConfigurationActivity.class);
                startActivity(configurationActivity);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

