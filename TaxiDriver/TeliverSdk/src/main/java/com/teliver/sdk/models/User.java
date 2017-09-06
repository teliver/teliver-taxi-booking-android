package com.teliver.sdk.models;

import com.teliver.sdk.util.TUtils;

public class User {

    private String id;

    private String type;

    private boolean isRegisterPush;

    User(String id, String type, boolean isRegisterPush) {
        this.id = id;
        this.isRegisterPush = isRegisterPush;
        this.type = type;
    }

    public String getId() {
        return TUtils.clearNull(id);
    }

    public String getType() {
        return TUtils.clearNull(type);
    }

    public boolean isRegisteredForPush() {
        return isRegisterPush;
    }


}
