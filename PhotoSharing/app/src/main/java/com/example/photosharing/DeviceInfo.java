package com.example.photosharing;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by peiyang on 15/5/29.
 */
public class DeviceInfo implements Parcelable {

    public String ipAddress;
    public String deviceName;

    public DeviceInfo(){

    }
    public DeviceInfo(String a, String b){
        ipAddress = a;
        deviceName = b;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ipAddress);
        dest.writeString(deviceName);
    }

    public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {

        @Override
        public DeviceInfo createFromParcel(Parcel source) {
            return new DeviceInfo(source);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    private DeviceInfo(Parcel in) {
        ipAddress = in.readString();
        deviceName = in.readString();
    }
}
