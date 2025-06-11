package com.fruitoftek.androidfaceattendance.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Areas {

    @PrimaryKey
    @NonNull
    // Length(6)
    public String area;

    // Length(50)
    public String description;

}
