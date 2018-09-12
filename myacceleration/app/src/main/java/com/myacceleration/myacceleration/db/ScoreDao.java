package com.myacceleration.myacceleration.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.lifecycle.LiveData;

import java.util.List;

@Dao
public interface ScoreDao {

    @Query("SELECT * FROM score")
    List<Score> getAll();

    @Query("SELECT * FROM score ORDER BY value")
    LiveData<List<Score>> loadAll();

    @Query("delete FROM score")
    void deleteAll();

    @Insert
    void insertAll(Score... scores);

    @Delete
    void delete(Score score);

}
