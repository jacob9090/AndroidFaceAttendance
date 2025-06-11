package com.fruitoftek.androidfaceattendance.data.model;

import static com.fruitoftek.androidfaceattendance.util.Literals.TRUE;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.google.android.gms.common.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import com.fruitoftek.androidfaceattendance.util.BioDataType;
import com.fruitoftek.androidfaceattendance.util.Util;

@Entity(indices = {@Index("isSync")})
public class Users {

    // User ID number
    // user == Empleado
    @NonNull
    @PrimaryKey
    public int user; // PK

    /**User privilege*/
    // 0 - Normal User
    // 1 - Admin User
    public int privilege;

    /**User name*/
    // Length(100)
    public String name;

    // 'A' Active           - Able to punch
    // 'B' Inactive         - Not able to punch
    // status == Estatus
    public String status;

    /**User password*/
    // Length(16)
    public String password;

    /**Main card number*/
    // NFC Card Number
    public String mainCard;

    /** Profile Photo **/
    /** ------------------------------------------------------------------------------------------*/
    /**Name of user photo*/
    // Length(50)
    public String photoIdName;

    /**The size of user photo data in Base64 format*/
    public int photoIdSize;

    /**User photo data in Base64 format*/
    public String photoIdContent;
    /** ------------------------------------------------------------------------------------------*/

    // Date format: "yyyy-MM-dd HH:mm:ss"
    public String lastUpdated;

    // Boolean. Is this record synced already?
    public int isSync;


    /** ---------------------------------------------------------------------------------------- **/
    /** Override Methods  **/
    @Override
    public String toString() {
        return "Users{" +
                "user=" + user +
                ", name='" + name + '\'' +
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
    // Returns true if the search string passed matches the name and/or id of this user
    // returns false otherwise
    // The search string passed is assumed to never be null
    public boolean search(String searchString) {
        String lowerSearchString = searchString.toLowerCase();
        String userString = String.valueOf(user);
        String userName = name == null ? "" : name.toLowerCase();
        return userString.contains(lowerSearchString) || userName.contains(lowerSearchString);
    }

    @Ignore
    public List<Areas> Areas;

    @Ignore
    public List<BioPhotos> BioPhotos;

    @Ignore
    public Bitmap findFirstBioPhotoThumbnailPhoto() {
        if (!CollectionUtils.isEmpty(BioPhotos)) {
            BioPhotos thumbNailBioPhoto = BioPhotos.stream()
                    .filter(bp -> bp.type == BioDataType.BIOPHOTO_THUMBNAIL_JPG.getType())
                    .findFirst()
                    .orElse(null);
            if (thumbNailBioPhoto != null) {
                return thumbNailBioPhoto.getPhoto();
            }
        }
        return null;
    }

    @Ignore
    public BioPhotos findFirstBioPhoto() {
        if (!CollectionUtils.isEmpty(BioPhotos)) {
            BioPhotos bioPhoto = BioPhotos.stream()
                    .filter(bp -> bp.type == BioDataType.BIOPHOTO_JPG.getType())
                    .findFirst()
                    .orElse(null);
            return bioPhoto;
        }
        return null;
    }

    @Ignore
    public Bitmap findFirstBioPhotoPhoto() {
        BioPhotos bioPhoto = findFirstBioPhoto();
        if (bioPhoto != null) {
            return bioPhoto.getPhoto();
        }
        return null;
    }
}
