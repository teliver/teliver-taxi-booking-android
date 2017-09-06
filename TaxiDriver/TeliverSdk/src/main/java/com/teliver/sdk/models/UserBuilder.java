package com.teliver.sdk.models;


public class UserBuilder {

    private String id;

    private boolean isRegister;

    private String type;

    public enum USER_TYPE {
        CONSUMER, OPERATOR
    }

    public UserBuilder(String id) {
        this.id = id;
    }

    public UserBuilder registerPush() {
        this.isRegister = true;
        return this;
    }

    public UserBuilder setUserType(USER_TYPE type) {
        this.type = type == USER_TYPE.OPERATOR ? "1" : "2";
        return this;
    }

    public User build() {
        return new User(id, type, isRegister);
    }
}
