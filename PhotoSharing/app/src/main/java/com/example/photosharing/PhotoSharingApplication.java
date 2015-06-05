package com.example.photosharing;

import android.app.Application;

import net.named_data.jndn.Name;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;

import java.util.ArrayList;

/**
 * Created by Zander on 2015/5/28.
 */
public class PhotoSharingApplication extends Application {

    private ArrayList<DeviceInfo> deviceList;
    private String myAddress, myDeviceName, ownerAddress, ownerName;
    private ArrayList<String> selectedPhotoPaths;

    public static KeyChain keyChain = buildTestKeyChain();

    @Override
    public void onCreate() {
        super.onCreate();
        deviceList = new ArrayList<>();
        selectedPhotoPaths = new ArrayList<>();
    }

    public void setMyAddress(String myAddress) {
        this.myAddress = myAddress;
    }

    public String getMyAddress() {
        return myAddress;
    }

    public void setMyDeviceName(String myDeviceName) {
        this.myDeviceName = myDeviceName;
    }

    public String getMyDeviceName() {
        return myDeviceName;
    }

    public void setOwnerAddress(String ownerAddress) {
        this.ownerAddress = ownerAddress;
    }

    public String getOwnerAddress() {
        return ownerAddress;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public ArrayList<DeviceInfo> getDeviceList(){
        return deviceList;
    }

    public void addDevice(String address, String name){
        if (deviceList.isEmpty()){
            DeviceInfo d = new DeviceInfo(address, name);
            deviceList.add(d);
        }
        else{
            for( int i = 0 ; i < deviceList.size() ; i ++){
                if (deviceList.get(i).ipAddress.equals(address)){
                    deviceList.remove(i);
                    break;
                }
            }
            DeviceInfo d = new DeviceInfo(address, name);
            deviceList.add(d);
        }
    }

    public void addDevice(DeviceInfo info) {
        deviceList.add(info);
    }

    public void clearSelectedPhotoPaths(){
        selectedPhotoPaths.clear();
        selectedPhotoPaths = new ArrayList<>();
    }

    public ArrayList<String> getSelectedPhotoPaths(){
        return selectedPhotoPaths;
    }
    public void addSelectedPhoto(String path){
        if (path != null)
            selectedPhotoPaths.add(path);
    }
    public void clearDeviceList(){
        deviceList.clear();
        deviceList = new ArrayList<>();
    }

    public int getDeviceListLength(){
        return deviceList.size();
    }
    public int getSelectedPhotoPathsLength(){
        return selectedPhotoPaths.size();
    }

    public static KeyChain buildTestKeyChain() {
        MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
        MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);
        net.named_data.jndn.security.KeyChain keyChain = new net.named_data.jndn.security.KeyChain(identityManager);
        try {
            keyChain.getDefaultCertificateName();
        } catch (net.named_data.jndn.security.SecurityException e) {
            try {
                keyChain.createIdentity(new Name("/test/identity"));
                keyChain.getIdentityManager().setDefaultIdentity(new Name("/test/identity"));
            } catch(net.named_data.jndn.security.SecurityException ee) {
                e.printStackTrace();
            }
        }
        return keyChain;
    }
}
