package com.myacceleration.myacceleration.rest;

import com.myacceleration.myacceleration.db.Score;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ScoreService {
    @GET("/scores/{id}")
    Call<List<Score>> getScores(@Path("id") Long carId);

}
