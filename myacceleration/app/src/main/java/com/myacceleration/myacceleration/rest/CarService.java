package com.myacceleration.myacceleration.rest;

import com.myacceleration.myacceleration.db.Car;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CarService {
    @GET("/cars/brands")
    Call<List<String>> getBrands();

    @GET("/cars")
    Call<List<Car>> getCars(@Query("brand")String brand);

}
