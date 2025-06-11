package com.fruitoftek.androidfaceattendance.facerecognition.dto;

import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;

public class BioPhotoMatch implements Comparable<BioPhotoMatch> {
    private float similitude;
    private BioPhotos bioPhoto;

    public BioPhotoMatch(float similitude, BioPhotos bioPhoto) {
        this.similitude = similitude;
        this.bioPhoto = bioPhoto;
    }

    public float getSimilitude() {
        return similitude;
    }

    public void setSimilitude(float similitude) {
        this.similitude = similitude;
    }

    public BioPhotos getBioPhoto() {
        return bioPhoto;
    }

    public void setBioPhoto(BioPhotos bioPhoto) {
        this.bioPhoto = bioPhoto;
    }

    @Override
    public int compareTo(BioPhotoMatch o) {
        // Comparator reversed to order in Descending order
        return Float.compare(o.getSimilitude(), this.getSimilitude());
    }
}
