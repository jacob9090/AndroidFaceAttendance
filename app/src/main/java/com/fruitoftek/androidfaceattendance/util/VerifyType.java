package com.fruitoftek.androidfaceattendance.util;

public enum VerifyType {

    FINGERPRINT(1),
    PASSWORD(3),
    CARD(4),
    FACE(15);

    private int type;

    VerifyType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
