package com.myacceleration.myacceleration.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Car {
    @PrimaryKey
    private Integer id;
    private String manufacturer;
    private String model;
    private Float manufacturerScore;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Float getManufacturerScore() {
        return manufacturerScore;
    }

    public void setManufacturerScore(Float manufacturerScore) {
        this.manufacturerScore = manufacturerScore;
    }
}
