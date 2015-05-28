package com.example.photosharing;

import android.app.Application;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Zander on 2015/5/28.
 */
public class PhotoSharingApplication extends Application {

    private ArrayList<DeviceInfo> deviceList;
    private String myAddress, myDeviceName, ownerAddress, ownerName;
    private ArrayList<String> selectedPhotoPaths;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceList = new ArrayList<>();
    }

    public ArrayList<DeviceInfo> getDeviceList(){
        return deviceList;
    }
    public void addDevice(String address, String name){
        DeviceInfo d = new DeviceInfo(address, name);
        deviceList.add(d);
    }

    public ArrayList<String> getSelectedPhotoPaths(){
        return selectedPhotoPaths;
    }
    public void addSelectedPhoto(String path){
        if (path != null)
            selectedPhotoPaths.add(path);
    }

    public class DeviceInfo{
        String ipAddress;
        String deviceName;

        public DeviceInfo(){

        }
        public DeviceInfo(String a, String b){
            ipAddress = a;
            deviceName = b;
        }
    }
}
