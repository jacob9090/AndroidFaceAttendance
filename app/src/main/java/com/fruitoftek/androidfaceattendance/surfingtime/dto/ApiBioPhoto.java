package com.fruitoftek.androidfaceattendance.surfingtime.dto;

public class ApiBioPhoto {

    private int user;

    private int type;

    /* Profile Photo **/
    /* ------------------------------------------------------------------------------------------*/
    /* Name of user photo*/
    private String photoIdName;

    /**The size of user photo data in Base64 format*/
    private int photoIdSize;

    /**User photo data in Base64 format*/
    private String photoIdContent;
    /* ------------------------------------------------------------------------------------------*/

    /** ---------------------------------------------------------------------------------------- **/
    /** Getters and Setters **/

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPhotoIdName() {
        return photoIdName;
    }

    public void setPhotoIdName(String photoIdName) {
        this.photoIdName = photoIdName;
    }

    public int getPhotoIdSize() {
        return photoIdSize;
    }

    public void setPhotoIdSize(int photoIdSize) {
        this.photoIdSize = photoIdSize;
    }

    public String getPhotoIdContent() {
        return photoIdContent;
    }

    public void setPhotoIdContent(String photoIdContent) {
        this.photoIdContent = photoIdContent;
    }
}
