package com.teliver.sdk.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.teliver.sdk.util.TUtils;

public class PushData implements Parcelable {

    private String payload;

    private String message;

    private String[] users;

    public PushData(String[] userIDs) {
        this.users = userIDs;
    }

    public PushData(String userId) {
        users = new String[]{userId};
    }

    private PushData(Parcel in) {
        payload = in.readString();
        message = in.readString();
    }

    public static final Creator<PushData> CREATOR = new Creator<PushData>() {
        @Override
        public PushData createFromParcel(Parcel in) {
            return new PushData(in);
        }

        @Override
        public PushData[] newArray(int size) {
            return new PushData[size];
        }
    };

    public String getPayload() {
        return TUtils.clearNull(payload);
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String[] getUsers() {
        return users == null ? new String[]{} : users;
    }

    public String getMessage() {
        return TUtils.clearNull(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(payload);
        parcel.writeString(message);
    }
}
