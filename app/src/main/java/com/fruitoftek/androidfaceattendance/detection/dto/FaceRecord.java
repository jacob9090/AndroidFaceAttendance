package com.fruitoftek.androidfaceattendance.detection.dto;

import android.graphics.Bitmap;
import com.google.mlkit.vision.face.Face;

public class FaceRecord {
    private Face face;

    private Bitmap faceFullPhoto;

    public FaceRecord() {
    }

    public FaceRecord(Face face, Bitmap faceImage) {
        this.face = face;
        this.faceFullPhoto = faceImage;
    }
}
