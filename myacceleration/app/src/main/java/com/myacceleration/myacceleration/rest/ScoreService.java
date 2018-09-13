package com.myacceleration.myacceleration.rest;

import com.myacceleration.myacceleration.db.Score;
import com.myacceleration.myacceleration.db.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ScoreService {
    @GET("/scores/{id}")
    Call<List<Score>> getScores(@Path("id") Long carId);

    @POST("/scores")
    Call<Void> createScore(@Body Score score);

}
