package com.myacceleration.myacceleration.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CarDao {

    @Query("SELECT * FROM car")
    List<Car> getAll();

    @Query("delete FROM car")
    void deleteAll();

    @Insert
    void insertAll(Car... users);

    @Delete
    void delete(Car user);

}
