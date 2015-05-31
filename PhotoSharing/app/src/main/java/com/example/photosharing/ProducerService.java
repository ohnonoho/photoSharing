package com.example.photosharing;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterest;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.transport.Transport;
import net.named_data.jndn.util.Blob;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by peiyang on 15/5/28.
 */
public class ProducerService extends IntentService {

    private ArrayList<String> prefixData = new ArrayList<>();
    private static final String TAG = "Producer Service";
    private Face mFace;

    private HashMap<String, String> fileMap = new HashMap<>();
    private ArrayList<String> filePaths = new ArrayList<>();
    private ArrayList<DeviceInfo> deviceInfos = new ArrayList<>();
    private boolean isPublic = true;
    private String passcode = "";
    private String mAddress = "";
    private String oAddress = "";

    public ProducerService() {
        super("ProducerService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ProducerService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            Log.i("Produce Serivce", "Start Produce Service");

            isPublic = intent.getBooleanExtra("isPublic", true);
            Log.i("Produce Serive", "" + isPublic);
            filePaths = intent.getStringArrayListExtra("filePath");
            for(String str : filePaths) {
                Log.i("Produce Service", str);
                String[] strs = str.split("/");
                fileMap.put(strs[strs.length - 1], str);
            }
            Log.i("Produce Service", fileMap.toString());
            passcode = intent.getStringExtra("passcode");
            mAddress = intent.getStringExtra("mAddress");
            oAddress = intent.getStringExtra("oAddress");
            // Only avalible when the device is the group owner
            if(mAddress.equals(oAddress))
                deviceInfos = intent.getParcelableArrayListExtra("deviceList");

            Log.i("Produce Service", deviceInfos.toString());

            // Read in all images here, store in a hashmap for later retrieve





            KeyChain keyChain = buildTestKeyChain();
            mFace = new Face("localhost");
            mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

            Log.i(ProducerService.TAG, "My Address is: " + mAddress);

            // Register the prefix with the device's address
            mFace.registerPrefix(new Name("/" + mAddress), new OnInterest() {
                @Override
                public void onInterest(Name name, Interest interest, Transport transport, long l) {
                    try {
                        int size = interest.getName().size();
                        Log.i(ProducerService.TAG, "Size: " + size);
                        Log.i(ProducerService.TAG, "Interest Name: " + interest.getName().toUri());
                        // When request for /IP/info
                        // When the device is also the owner, also serve the deviceList
                        if(size == 2) {
                            Name requestName = interest.getName();
                            String component = requestName.get(1).toEscapedString();
                            if(component.equals("info")) {

                                // Construct the JSON object
                                JSONObject json = new JSONObject();
                                json.put("isPublic", isPublic);
                                json.put("passcode", passcode);
                                // ArrayList<String> list = new ArrayList<String>();
                                // list.add("filename1");
                                // list.add("filename2");
                                JSONArray array = new JSONArray(filePaths);
                                json.put("filePath", array);
                                Log.i(ProducerService.TAG, "The info data " + json.toString());

                                // Construct the data packet
                                Data data = new Data(requestName);
                                data.setContent(new Blob(json.toString()));

                                mFace.putData(data);
                                Log.i(ProducerService.TAG, "The info data has been send");
                            }
                            // The owner serve this
                            else if(component.equals("deviceList")) {

                                // The device should be the owner
                                JSONArray array = new JSONArray();
                                for(DeviceInfo info : deviceInfos) {
                                    JSONObject object = new JSONObject();
                                    object.put("ipAddress", info.ipAddress);
                                    object.put("deviceName", info.deviceName);
                                    array.put(object);
                                }

                                JSONObject object = new JSONObject();
                                object.put("ipAddress", "/"+mAddress);
                                object.put("deviceName", "owner");
                                array.put(object);

                                String content = array.toString();
                                Data data = new Data(requestName);
                                data.setContent(new Blob(content));

                                mFace.putData(data);
                                Log.i(ProducerService.TAG, "The device info has been send");
                                Log.i(ProducerService.TAG, "The content is: " + content);
                            }
                        }
                        // When request for /IP/(public or private)/filename/#seq
                        else if(size == 4) {
                            prefixData.clear();
                            Name requestName = interest.getName();
                            String component = requestName.get(2).toEscapedString();
                            int seqNo = (int) requestName.get(3).toSequenceNumber();
                            Data data = new Data(requestName);

                            // Read in the image here
                            // Drawable d = getResources().getDrawable(R.drawable.py6);
                            // Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                            // ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            BitmapFactory.Options option = new BitmapFactory.Options();
                            option.inSampleSize = 8;
                            Bitmap bitmap = BitmapFactory.decodeFile(fileMap.get(component), option);
                            // Bitmap bitmap = BitmapFactory.decodeFile(fileMap.get(component));
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] bitmapdata = stream.toByteArray();
                            String content = Base64.encodeToString(bitmapdata, Base64.DEFAULT);

                            // Split the data
                            int fixLength = 8000;
                            int cnt = (content.length() / fixLength) + 1;

                            for (int i = 0; i < cnt; i++) {
                                prefixData.add(content.substring(i * fixLength, Math.min((i + 1) * fixLength, content.length())));
                            }

                            if (seqNo == 1) {
                                data.setContent(new Blob("" + prefixData.size()));
                            } else {
                                data.setContent(new Blob(prefixData.get(seqNo - 2)));
                            }
                            mFace.putData(data);
                            Log.i(ProducerService.TAG, "Send out the data " + interest.getName().toUri());
                        }

                    } catch (EncodingException e) {
                        Log.e(ProducerService.TAG, "Failed to encode");
                    } catch (IOException e) {
                        Log.e(ProducerService.TAG, "Failed to send the data");
                    } catch (JSONException e) {
                        Log.e(ProducerService.TAG, "Failed to construct the json object");
                    }
                }
            }, new OnRegisterFailed() {
                @Override
                public void onRegisterFailed(Name name) {
                    Log.e(ProducerService.TAG, "Failed to register the data");
                }
            });

            while(true) {
                // Log.i(ProducerService.TAG, "Service is running");
                mFace.processEvents();
            }

        } catch (Exception e) {
            Log.e(ProducerService.TAG, e.toString());
        }
    }

    public static KeyChain buildTestKeyChain() throws net.named_data.jndn.security.SecurityException {
        MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
        MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);
        KeyChain keyChain = new KeyChain(identityManager);
        try {
            keyChain.getDefaultCertificateName();
        } catch (net.named_data.jndn.security.SecurityException e) {
            keyChain.createIdentity(new Name("/test/identity"));
            keyChain.getIdentityManager().setDefaultIdentity(new Name("/test/identity"));
        }
        return keyChain;
    }
}
