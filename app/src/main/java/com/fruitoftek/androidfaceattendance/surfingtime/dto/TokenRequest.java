package com.fruitoftek.androidfaceattendance.surfingtime.dto;

public class TokenRequest {
    private String ClientId;
    private String ClientSecret;
    private String DeviceSn;

    /** ---------------------------------------------------------------------------------------- **/
    /** Getters and Setters **/

    public String getClientId() {
        return ClientId;
    }

    public void setClientId(String clientId) {
        ClientId = clientId;
    }

    public String getClientSecret() {
        return ClientSecret;
    }

    public void setClientSecret(String clientSecret) {
        ClientSecret = clientSecret;
    }

    public String getDeviceSn() {
        return DeviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        DeviceSn = deviceSn;
    }
}
