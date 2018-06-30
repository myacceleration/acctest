package com.myacceleration.myacceleration.rest;

import com.myacceleration.myacceleration.db.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserService {
    @GET("/users")
    Call<User> getUser(@Query("login")String login);

    @POST("/users")
    Call<Void> createUser(@Body User user);
}
