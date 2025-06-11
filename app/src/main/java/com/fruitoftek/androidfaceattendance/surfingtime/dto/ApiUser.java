package com.fruitoftek.androidfaceattendance.surfingtime.dto;

public class ApiUser {
    private int user;

    private int privilege;

    private String name;

    // 'A' Active           - Able to punch
    // 'B' Inactive         - Not able to punch
    private String status;

    private String password;

    private String mainCard;

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

    public int getPrivilege() {
        return privilege;
    }

    public void setPrivilege(int privilege) {
        this.privilege = privilege;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMainCard() {
        return mainCard;
    }

    public void setMainCard(String mainCard) {
        this.mainCard = mainCard;
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
