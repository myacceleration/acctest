package com.myacceleration.myacceleration.login;

import android.content.Context;

public abstract class LoginStrategy {
    public enum Status {LOGIN_SUCCESS, TO_REGISTER, REGISTRATION_SUCCESS, ERROR }

    protected Status status = Status.ERROR;
    protected String username;
    protected String carname;
    protected String password;

    protected String message = "Nieokreślony błąd";

    public abstract void doLogin(Context context, String username,String password);

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCarname() {
        return carname;
    }

    public void setCarname(String carname) {
        this.carname = carname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
