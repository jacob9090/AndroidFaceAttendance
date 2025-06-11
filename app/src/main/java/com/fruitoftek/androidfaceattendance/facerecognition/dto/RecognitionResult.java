package com.fruitoftek.androidfaceattendance.facerecognition.dto;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;

public class RecognitionResult {
    private BioPhotos matchedBioPhoto;
    private float confidence;
    private float[] features;
    private String title;
    private Integer color;
    private Bitmap crop;
    private RectF location;
    private String bottomMessage;
    private boolean showFaceLabel = true;
    private boolean showConfidence = true;


    public RecognitionResult() {
    }

    public BioPhotos getMatchedBioPhoto() {
        return matchedBioPhoto;
    }

    public void setMatchedBioPhoto(BioPhotos matchedBioPhoto) {
        this.matchedBioPhoto = matchedBioPhoto;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public float[] getFeatures() {
        return features;
    }

    public void setFeatures(float[] features) {
        this.features = features;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Bitmap getCrop() {
        return crop;
    }

    public void setCrop(Bitmap crop) {
        this.crop = crop;
    }

    public RectF getLocation() {
        return location;
    }

    public void setLocation(RectF location) {
        this.location = location;
    }

    public String getBottomMessage() {
        return bottomMessage;
    }

    public void setBottomMessage(String bottomMessage) {
        this.bottomMessage = bottomMessage;
    }

    public boolean isShowFaceLabel() {
        return showFaceLabel;
    }

    public void setShowFaceLabel(boolean showFaceLabel) {
        this.showFaceLabel = showFaceLabel;
    }

    public boolean isShowConfidence() {
        return showConfidence;
    }

    public void setShowConfidence(boolean showConfidence) {
        this.showConfidence = showConfidence;
    }
}
