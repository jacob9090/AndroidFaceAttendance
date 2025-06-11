package com.fruitoftek.androidfaceattendance.util;

public enum BioDataType {

    BIOPHOTO_JPG(9),
    BIOPHOTO_THUMBNAIL_JPG(101);

    private int type;

    BioDataType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
