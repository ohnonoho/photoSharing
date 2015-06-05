package com.example.photosharing;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;


public class MenuActivity extends ActionBarActivity {
    private Button btnFindDevices;
    private Button btnGetPhotos;
    private Button btnSharePhotos;
    public static boolean wifiDirectConnected = false;

    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        btnGetPhotos = (Button) this.findViewById(R.id.btnGetPhotos);
        btnSharePhotos = (Button) this.findViewById(R.id.btnSharePhotos);
        btnFindDevices = (Button) this.findViewById(R.id.btnFindDevices);
        intent = new Intent(this, DeviceListActivity.class);
        btnGetPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get visible devices first, needs to be updated
                //do something on NFD !!!!!
                //get and update device list

                //app.addDevice()
                String oAddress = ((PhotoSharingApplication)getApplication()).getOwnerAddress();
                String mAddress = ((PhotoSharingApplication)getApplication()).getMyAddress();
                if(!oAddress.equals(mAddress)) {
                    RequestDeviceListTask task = new RequestDeviceListTask((PhotoSharingApplication)getApplication(), getApplicationContext());
                    // String oAddress = ((PhotoSharingApplication) getApplication()).getOwnerAddress();
                    Log.i("Request Deivce List", "Start to request device list from " + oAddress);
                    task.execute(oAddress);
                }
                else {
                    RegisterNFD task = new RegisterNFD((PhotoSharingApplication)getApplication());
                    Log.i("Menu Activity", "Register on NFD");
                    ArrayList<DeviceInfo> deviceInfos = ((PhotoSharingApplication)getApplication()).getDeviceList();
                    Log.i("Menu Activity", "The device list " + deviceInfos.toString());
                    task.execute(deviceInfos);

                    // Intent intent = new Intent(MenuActivity.this, DeviceListActivity.class);
                    // ArrayList<DeviceInfo> deviceInfos = ((PhotoSharingApplication)getApplication()).getDeviceList();
                    intent.putParcelableArrayListExtra("devices", deviceInfos);

                    startActivity(intent);
                }

//                Intent intent = new Intent(MenuActivity.this, DeviceListActivity.class);
//                ArrayList<DeviceInfo> deviceInfos = ((PhotoSharingApplication)getApplication()).getDeviceList();
//                intent.putParcelableArrayListExtra("devices", deviceInfos);
//                //intent.putExtra("devices", devices);
//
//                // Register the routes
//                // ArrayList<String> ips = new ArrayList<String>();
//                // for(DeviceInfo info : deviceInfos) {
//                    // ips.add(info.ipAddress);
//                // }
//                // RegisterNFD rTask = new RegisterNFD();
//                // rTask.execute(ips);
//
//                startActivity(intent);
            }
        });
        btnSharePhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, CustomPhotoGalleryActivity.class);
                startActivity(intent);
                //in order to send data
                //intent.putExtra(key,value);
                //in order to get result
                //startActivityForResult(intent, requestcode);
            }
        });
        btnFindDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send to wifi direct activity
                Intent intent = new Intent(MenuActivity.this, ProducerActivity.class);
                //startActivityForResult(intent);
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //wifiDirectConnected = true; // for test purpose, enable the two buttons on main menu page
        if (wifiDirectConnected){
            btnGetPhotos.setEnabled(true);
            btnGetPhotos.setBackground(getResources().getDrawable(R.drawable.btnviewothersenable));
            btnSharePhotos.setEnabled(true);
            btnSharePhotos.setBackground(getResources().getDrawable(R.drawable.btnsharemyphotosenable));
        }
        else{
            Toast.makeText(getApplicationContext(), "To get started, you may have to connect to other devices first", Toast.LENGTH_LONG).show();
            btnGetPhotos.setEnabled(false);
            btnGetPhotos.setBackground(getResources().getDrawable(R.drawable.btnviewothersdisable));
            btnSharePhotos.setEnabled(false);
            btnSharePhotos.setBackground(getResources().getDrawable(R.drawable.btnsharemyphotosdisable));
        }
    }

    // AsyncTask for requesting device list, input is the owner's address
    // /oAddress/deveicList
    private class RequestDeviceListTask extends AsyncTask<String, Void, ArrayList<DeviceInfo>> {

        private static final String TAG = "Request Device List";
        private Face mFace;
        private ArrayList<DeviceInfo> deviceInfos = new ArrayList<>();
        private boolean shouldStop = false;
        private PhotoSharingApplication app;
        private Context context;
        public RequestDeviceListTask(PhotoSharingApplication application, Context context) {
            this.app = application;
            this.context = context;
        }
        @Override
        protected ArrayList<DeviceInfo> doInBackground(String... params) {

            try {
                // KeyChain keyChain = buildTestKeyChain();
                KeyChain keyChain = app.keyChain;
                mFace = new Face("localhost");
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                // Invalid input
                if(params.length < 1) {
                    Log.e(RequestDeviceListTask.TAG, "No owner address!");
                    return null;
                }

                String oAddress = params[0];
                Log.i(RequestDeviceListTask.TAG, "Owner Address: " + oAddress);

                Nfdc nfdc = new Nfdc();
                int faceId = nfdc.faceCreate("udp://" + oAddress);
                nfdc.ribRegisterPrefix(new Name("/" + oAddress), faceId, 10, true, false);
                nfdc.shutdown();


                Interest interest = new Interest(new Name("/" + oAddress + "/deviceList"));
                interest.setInterestLifetimeMilliseconds(10000);

                mFace.expressInterest(interest, new OnData() {
                    @Override
                    public void onData(Interest interest, Data data) {

                        String content = data.getContent().toString();
                        Log.i(RequestDeviceListTask.TAG, "The content has been received." + content);
                        // Parse the content to a list deviceInfo
                        try {
                            JSONArray array = new JSONArray(content);
                            for (int i = 0; i < array.length(); ++i) {
                                JSONObject object = array.getJSONObject(i);
                                DeviceInfo info = new DeviceInfo();
                                info.deviceName = object.getString("deviceName");
                                info.ipAddress = object.getString("ipAddress");
                                deviceInfos.add(info);
                                shouldStop = true;
                            }
                        } catch (JSONException e) {
                            Log.e(RequestDeviceListTask.TAG, "Failed to construct the JSON");
                        }
                    }
                }, new OnTimeout() {
                    @Override
                    public void onTimeout(Interest interest) {
                        Log.e(RequestDeviceListTask.TAG, "Time Out!");
                        shouldStop = true;
                    }
                });

            while(!shouldStop) {
                mFace.processEvents();
            }

            } catch (net.named_data.jndn.security.SecurityException e) {
                Log.e(RequestDeviceListTask.TAG, "Secrity Failed");
            } catch (IOException e) {
                Log.e(RequestDeviceListTask.TAG, "IO Failed");
            } catch (EncodingException e) {
                Log.e(RequestDeviceListTask.TAG, "Encoding Error");
            } catch (Exception e) {
                Log.e(RequestDeviceListTask.TAG, e.toString());
            }
            return deviceInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<DeviceInfo> deviceInfos) {
            super.onPostExecute(deviceInfos);
            if(deviceInfos != null) {
                // PhotoSharingApplication application = (PhotoSharingApplication) getApplication();
                for (DeviceInfo info : deviceInfos) {
                    app.addDevice(info.ipAddress, info.deviceName);
                }

                intent.putParcelableArrayListExtra("devices", deviceInfos);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                RegisterNFD task = new RegisterNFD(app);
                task.execute(deviceInfos);
            }
        }
    }

    // AsyncTask for register the routes on NFD
    private class RegisterNFD extends AsyncTask<ArrayList<DeviceInfo>, Void, Void> {

        private static final String TAG = "Register NFD Task";
        private boolean shouldStop = false;
        private Face mFace;
        private PhotoSharingApplication app;
        private Context context;

        public RegisterNFD(PhotoSharingApplication app) {
            this.app = app;
            this.context = context;
        }
        @Override
        protected Void doInBackground(ArrayList<DeviceInfo>... params) {

            try {
                // KeyChain keyChain = buildTestKeyChain();

                if(params.length < 1) {
                    Log.e(RegisterNFD.TAG, "No device list");
                    return null;
                }

                ArrayList<DeviceInfo> list = params[0];
                Nfdc ndfc = new Nfdc();
                for(DeviceInfo info : list) {
                    if(info.ipAddress.equals(app.getMyAddress()))
                        continue;
                    int faceID = ndfc.faceCreate("udp:/" + info.ipAddress);
                    ndfc.ribRegisterPrefix(new Name(info.ipAddress), faceID, 10, true, false);
                }
                ndfc.shutdown();

            } catch (SecurityException e) {
                Log.e(RegisterNFD.TAG, "Register Failed");
            } catch(Exception e) {
                Log.e(RegisterNFD.TAG, e.toString());
            }
            return null;
        }
    }

    public static KeyChain buildTestKeyChain() throws net.named_data.jndn.security.SecurityException {
        MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
        MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);
        net.named_data.jndn.security.KeyChain keyChain = new net.named_data.jndn.security.KeyChain(identityManager);
        try {
            keyChain.getDefaultCertificateName();
        } catch (net.named_data.jndn.security.SecurityException e) {
            keyChain.createIdentity(new Name("/test/identity"));
            keyChain.getIdentityManager().setDefaultIdentity(new Name("/test/identity"));
        }
        return keyChain;
    }
}
