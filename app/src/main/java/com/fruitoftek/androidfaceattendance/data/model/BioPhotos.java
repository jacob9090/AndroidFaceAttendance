package com.fruitoftek.androidfaceattendance.data.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiBioPhoto;
import com.fruitoftek.androidfaceattendance.util.Util;

import static com.fruitoftek.androidfaceattendance.util.Literals.TRUE;

@Entity(primaryKeys = {"user", "type"},
        indices = {
                @Index("user"),
                @Index("type"),
                @Index("isSync")})
public class BioPhotos {

    @NonNull
    public int user;// PK

    @NonNull
    // Type of BioPhoto record
    public int type;// PK

    /** BioPhoto content **/
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

    // Date format: "yyyy-MM-dd HH:mm:ss"
    public String lastUpdated;

    // Boolean. Is this record synced to SurfingTime already?
    public int isSync;

    /** ---------------------------------------------------------------------------------------- **/
    /** Method Overrides **/
    @Override
    public String toString() {
        return "BioPhotos{" +
                "user=" + user +
                ", type=" + type +
                '}';
    }

    /** ---------------------------------------------------------------------------------------- **/
    /** Useful calculated fields and methods **/

    @Ignore
    public Bitmap photo = null;

    @Ignore
    public Bitmap getPhoto() {
        if (photo == null) {
            byte[] bitmapdata = Base64.getDecoder().decode(photoIdContent);
            photo = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
        }
        return photo;
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

    public String getBioPhotoStringIdentifier() {
        if (User != null) {
            return user + "-" + User.name;
        }
        return Integer.toString(user);
    }

    @Ignore
    public Users User;

    @Ignore
    public BioPhotos Thumbnail;

    @Ignore
    public BioPhotoFeatures Features;

    @Ignore
    public ApiBioPhoto ToApiBioPhoto() {
        ApiBioPhoto apiBioPhoto = new ApiBioPhoto();
        apiBioPhoto.setUser(user);
        apiBioPhoto.setType(type);
        apiBioPhoto.setPhotoIdName(photoIdName);
        apiBioPhoto.setPhotoIdSize(photoIdSize);
        apiBioPhoto.setPhotoIdContent(photoIdContent);
        return apiBioPhoto;
    }

    @Ignore
    public static BioPhotos fromApiBioPhoto(ApiBioPhoto apiBioPhoto) {
        BioPhotos bioPhoto = new BioPhotos();
        bioPhoto.user = apiBioPhoto.getUser();
        bioPhoto.type = apiBioPhoto.getType();
        bioPhoto.photoIdName = apiBioPhoto.getPhotoIdName();
        bioPhoto.photoIdSize = apiBioPhoto.getPhotoIdSize();
        bioPhoto.photoIdContent = apiBioPhoto.getPhotoIdContent();
        bioPhoto.lastUpdated = Util.getDateTimeNow();
        bioPhoto.isSync = TRUE;
        return bioPhoto;
    }

}
