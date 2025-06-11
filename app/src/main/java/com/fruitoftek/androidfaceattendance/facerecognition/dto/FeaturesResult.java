package com.fruitoftek.androidfaceattendance.facerecognition.dto;

import android.graphics.Bitmap;

public class FeaturesResult {
    private Bitmap croppedPhoto;
    private float[] features;
    public FeaturesResult(Bitmap croppedPhoto, float[] features) {
        this.croppedPhoto = croppedPhoto;
        this.features = features;
    }

    public Bitmap getCroppedPhoto() {
        return croppedPhoto;
    }

    public void setCroppedPhoto(Bitmap croppedPhoto) {
        this.croppedPhoto = croppedPhoto;
    }

    public float[] getFeatures() {
        return features;
    }

    public void setFeatures(float[] features) {
        this.features = features;
    }
}