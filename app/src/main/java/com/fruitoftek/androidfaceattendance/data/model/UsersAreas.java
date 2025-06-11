package com.fruitoftek.androidfaceattendance.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"user", "area"})
public class UsersAreas {

    @NonNull
    public int user; // PK

    @NonNull
    // Length(6)
    public String area; // PK

}
