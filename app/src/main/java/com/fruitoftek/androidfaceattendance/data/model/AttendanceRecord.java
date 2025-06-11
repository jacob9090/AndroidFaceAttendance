package com.fruitoftek.androidfaceattendance.data.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiAttLog;

@Entity(primaryKeys = {"user", "verifyTime"},
            indices = { @Index("verifyTimeEpochMilliSeconds"),
                        @Index(value = {"user", "verifyTimeEpochMilliSeconds"}, unique = true),
                        @Index("user"),
                        @Index("isSync")})
public class AttendanceRecord {

    @NonNull
    public int user;// PK

    @NonNull
    // Date format: "yyyy-MM-dd HH:mm:ss"
    public String verifyTime;// PK

    // VerifyTime as a numeric value counting
    // The number of milliseconds from the Java epoch of 1970-01-01T00:00:00Z.
    // Useful for correctly and efficiently sorting dates
    // Usually set with java.util.Date#getTime()
    public long verifyTimeEpochMilliSeconds;

    // 1  - Fingerprint verification
    // 3  - Password verification
    // 4  - Card verification
    // 15 - Face verification
    public int verifyType;

    /** Verification photo if done by Face Recognition **/
    /** ------------------------------------------------------------------------------------------*/
    /**Name of user photo*/
    // Length(50)
    public String photoIdName;

    /**The size of user photo data in Base64 format*/
    public int photoIdSize;

    /**User photo data in Base64 format*/
    // Length(max)
    public String photoIdContent;
    /** ------------------------------------------------------------------------------------------*/

    /**
     * Precision of 6 decimal digits
     * <Longitude>,<Latitude>
     * Example: -99.986500,25.742400
     * */
    public String geoLocation;

    // Boolean. Is this record synced to SurfingTime already?
    public int isSync;


    /** ---------------------------------------------------------------------------------------- **/
    /** Method Overrides  **/
    @Override
    public String toString() {
        return "AttendanceRecord{" +
                "user=" + user +
                ", verifyTime='" + verifyTime + '\'' +
                '}';
    }

    /** ---------------------------------------------------------------------------------------- **/
    /** Useful calculated/related fields  **/

    @Ignore
    public Bitmap getPhoto() {
        if (!StringUtils.isEmpty(photoIdContent)) {
            byte[] bitmapdata = Base64.getDecoder().decode(photoIdContent);
            return BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
        }
        return null;
    }

    @Ignore
    public void setBioPhotoContent(Bitmap bitmap) {
        // Compressing Bitmap into a JPG, extract its file bytes
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, blob);
        byte[] bitmapdata = blob.toByteArray();
        // Encode JPG file bytes to Base64
        photoIdName = String.format("%d.jpg", user);
        photoIdContent = Base64.getEncoder().encodeToString(bitmapdata);
        photoIdSize = photoIdContent.length();
    }

    @Ignore
    public String verifyTypeStr;

    @Ignore
    public Users userObj;

    @Ignore
    public ApiAttLog toApiAttLog() {
        ApiAttLog apiAttLog = new ApiAttLog();
        apiAttLog.setUser(user);
        apiAttLog.setVerifyTime(verifyTime);
        apiAttLog.setPhotoIdName(photoIdName);
        apiAttLog.setPhotoIdSize(photoIdSize);
        apiAttLog.setPhotoIdContent(photoIdContent);
        apiAttLog.setGeoLocation(geoLocation);
        return apiAttLog;
    }

}
