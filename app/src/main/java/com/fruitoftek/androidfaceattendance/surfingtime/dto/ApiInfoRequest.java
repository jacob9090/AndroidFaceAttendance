package com.fruitoftek.androidfaceattendance.surfingtime.dto;

public class ApiInfoRequest {

    public String DeviceName;

    public int UsersCount;

    // TransCount
    public int AttLogsCount;

    // DevLanguage
    public String Language;

    public int FaceCount;

    public String TimeZone;

    // Example: SurfingAttendance
    // This field will be re-used to identify the device as a SurfingAttendance application
    public String Model;

    // Example: Android 12
    // This field will be re-used to identify the device as a SurfingAttendance application
    // Running on an Android phone
    public String Platform;

    // Example: moto g power (2022)
    // This field will be re-used to contain the Model of the phone/tablet/device
    public String DeviceType;

    // Goes to Info field
    public String MacAddress;

    // Goes to Info Field
    // Remaining free flash memory in bytes
    // Example: 34000000
    public Long FreeFlashMemory;

    // Goes to Info Field
    // Total amount of flash memory in bytes
    // Example: 64000000
    public Long FlashMemoryTotalAmount;

    // Goes to Info Field
    public Boolean IsBioPhotoVerificationAvailable;

    // Goes to Info Field
    public Boolean IsFaceVerificationAvailable;

    // Goes to Info Field
    public Boolean IsVisibleFaceVerificationAvailable;

    // Goes to Info Field
    public Boolean IsFingerprintVerificationAvailable;


    /** ---------------------------------------------------------------------------------------- **/
    /** Getters and Setters **/

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public int getUsersCount() {
        return UsersCount;
    }

    public void setUsersCount(int usersCount) {
        UsersCount = usersCount;
    }

    public int getAttLogsCount() {
        return AttLogsCount;
    }

    public void setAttLogsCount(int attLogsCount) {
        AttLogsCount = attLogsCount;
    }

    public String getLanguage() {
        return Language;
    }

    public void setLanguage(String language) {
        Language = language;
    }

    public int getFaceCount() {
        return FaceCount;
    }

    public void setFaceCount(int faceCount) {
        FaceCount = faceCount;
    }

    public String getTimeZone() {
        return TimeZone;
    }

    public void setTimeZone(String timeZone) {
        TimeZone = timeZone;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public String getPlatform() {
        return Platform;
    }

    public void setPlatform(String platform) {
        Platform = platform;
    }

    public String getDeviceType() {
        return DeviceType;
    }

    public void setDeviceType(String deviceType) {
        DeviceType = deviceType;
    }

    public String getMacAddress() {
        return MacAddress;
    }

    public void setMacAddress(String macAddress) {
        MacAddress = macAddress;
    }

    public Long getFreeFlashMemory() {
        return FreeFlashMemory;
    }

    public void setFreeFlashMemory(Long freeFlashMemory) {
        FreeFlashMemory = freeFlashMemory;
    }

    public Long getFlashMemoryTotalAmount() {
        return FlashMemoryTotalAmount;
    }

    public void setFlashMemoryTotalAmount(Long flashMemoryTotalAmount) {
        FlashMemoryTotalAmount = flashMemoryTotalAmount;
    }

    public Boolean getBioPhotoVerificationAvailable() {
        return IsBioPhotoVerificationAvailable;
    }

    public void setBioPhotoVerificationAvailable(Boolean bioPhotoVerificationAvailable) {
        IsBioPhotoVerificationAvailable = bioPhotoVerificationAvailable;
    }

    public Boolean getFaceVerificationAvailable() {
        return IsFaceVerificationAvailable;
    }

    public void setFaceVerificationAvailable(Boolean faceVerificationAvailable) {
        IsFaceVerificationAvailable = faceVerificationAvailable;
    }

    public Boolean getVisibleFaceVerificationAvailable() {
        return IsVisibleFaceVerificationAvailable;
    }

    public void setVisibleFaceVerificationAvailable(Boolean visibleFaceVerificationAvailable) {
        IsVisibleFaceVerificationAvailable = visibleFaceVerificationAvailable;
    }

    public Boolean getFingerprintVerificationAvailable() {
        return IsFingerprintVerificationAvailable;
    }

    public void setFingerprintVerificationAvailable(Boolean fingerprintVerificationAvailable) {
        IsFingerprintVerificationAvailable = fingerprintVerificationAvailable;
    }
}
