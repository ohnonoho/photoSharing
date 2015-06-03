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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DeviceListActivity extends ActionBarActivity {
    final String TAG = "DeviceListActivity";
    private PhotoSharingApplication app;

    private ListView listView;
    //private List<String> deviceDisplayList;
    private JSONObject info;

    SimpleAdapter adapter;
    ArrayList<HashMap<String, Object>> displayContent;

    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        // Intent intent = getIntent();
        //String[] devices = intent.getStringArrayExtra("devices");

        app = (PhotoSharingApplication) this.getApplication();

        final ArrayList<DeviceInfo> deviceList = app.getDeviceList();

//        ArrayList<DeviceInfo> deviceList_debug = new ArrayList<DeviceInfo>();
//        DeviceInfo dummy = new DeviceInfo("192.168.1.1", "dummyDevice" );
//        deviceList_debug.add(dummy);

        if ( deviceList.isEmpty() ) {
            Log.i(TAG, "device list is empty");
            Toast.makeText(getApplicationContext(), "No device discoverable", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Your own IP address: " + app.getMyAddress(), Toast.LENGTH_LONG).show();

            // These are the code for ArrayAdapter
            //deviceDisplayList = new ArrayList<String>();// this is the array of string which is used by listview adapter//
//            int i = 0;
//            for (i = 0; i < deviceList.size(); i++) {
//                deviceDisplayList.add(deviceList.get(i).ipAddress);
//                Log.i(TAG, "decive list index: " + i);
//            }
            //listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, deviceDisplayList));
//            SimpleAdapter adapter = new SimpleAdapter(this, getData() ,R.layout.vlist,
//                                        new String[]{"title","info","img"},
//                                        new int[]{R.id.title,R.id.info,R.id.img});

            displayContent = new ArrayList<HashMap<String, Object>>();

            //final ArrayList<DeviceInfo> deviceList = deviceList_debug;
            for (int i = 0; i < deviceList.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("device_name", deviceList.get(i).deviceName);
                map.put("device_ip", deviceList.get(i).ipAddress);
                map.put("device_status", "available");
//                if isPublic
//                       map.put("isPublic", R.drawable....)
//                else
//                        map.put()
                displayContent.add(map);
                Log.i(TAG, "on create decive list index: " + i);
            }
            listView = new ListView(this);


            adapter = new SimpleAdapter(this, displayContent,R.layout.listview_content,
                                        new String[]{"device_name","device_ip", "device_status"},
                                        new int[]{R.id.device_name, R.id.device_ip, R.id.device_status});
            listView.setAdapter(adapter);
            setContentView(listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String targetIP = deviceList.get(position).ipAddress;
                    Log.i("Target IP", "" + targetIP);
                    intent = new Intent(DeviceListActivity.this, BrowsePhotosActivity.class);
                    intent.putExtra("deviceName", deviceList.get(position).deviceName);
                    intent.putExtra("targetIP", targetIP);
                    //get info from the device
                    //do something on NFD !!!!!
                    //get /target/info, isPublic, passcode

                    RequestInfo task = new RequestInfo(getApplicationContext(), (PhotoSharingApplication)getApplication());

                    task.execute(targetIP);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//
//
//        app = (PhotoSharingApplication) this.getApplication();
//        final ArrayList<DeviceInfo> deviceList = app.getDeviceList();
//        displayContent.clear();
//        displayContent = new ArrayList<HashMap<String, Object>>();
//        //final ArrayList<DeviceInfo> deviceList = deviceList_debug;
//        for (int i = 0; i < deviceList.size(); i++) {
//            HashMap<String, Object> map = new HashMap<String, Object>();
//            map.put("device_name", deviceList.get(i).deviceName);
//            map.put("device_ip", deviceList.get(i).ipAddress);
//            map.put("device_status", "available");
//            displayContent.add(map);
//            Log.i(TAG, "decive list index: " + i);
//        }
//        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.cancel:
                //Toast.makeText(this, "Are you sure to cancel?", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private class RequestInfo extends AsyncTask<String, Void, JSONObject> {

        private static final String TAG = "Request Info";
        private Face mFace;
        private JSONObject mInfo;
        private boolean shouldStop = false;

        Context context;
        private PhotoSharingApplication app;

        public RequestInfo(Context context, PhotoSharingApplication app) {
            this.context = context;
            this.app = app;
        }
        @Override
        protected JSONObject doInBackground(String... params) {

            try {
                // Send the interest to /oAddres/info
                // KeyChain keyChain = buildTestKeyChain();
                KeyChain keyChain = app.keyChain;
                mFace = new Face("localhost");
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                if(params.length < 1) {
                    Log.e(RequestInfo.TAG, "No owner address");
                    return null;
                }

                String oAddress = params[0];
                Log.i(RequestInfo.TAG, "Target Address " + oAddress);
                Interest interest = new Interest(new Name(oAddress + "/info"));
                interest.setInterestLifetimeMilliseconds(10000);
                mFace.expressInterest(interest, new OnData() {
                    @Override
                    public void onData(Interest interest, Data data) {

                        String content = data.getContent().toString();
                        Log.i(RequestInfo.TAG, "The info content " + content);
                        // Parse the content to JSONObject
                        try {
                            mInfo = new JSONObject(content);
                        } catch (JSONException e) {
                            Log.i(RequestInfo.TAG, "Failed to construct json object");
                        }
                        shouldStop = true;
                    }
                }, new OnTimeout() {
                    @Override
                    public void onTimeout(Interest interest) {
                        Log.e(RequestInfo.TAG, "Time Out!");
                        shouldStop = true;
                    }
                });

                while(!shouldStop) {
                    mFace.processEvents();
                }
            } catch (net.named_data.jndn.security.SecurityException e) {
                Log.e(RequestInfo.TAG, e.toString());
            } catch (IOException e) {
                Log.e(RequestInfo.TAG, e.toString());
            } catch (EncodingException e) {
                Log.e(RequestInfo.TAG, e.toString());
            }

            return mInfo;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if(jsonObject != null) {
                info = jsonObject;
                intent.putExtra("info", jsonObject.toString());
            }
            Log.i(RequestInfo.TAG, intent.toString());
            Log.i(RequestInfo.TAG, jsonObject.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
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
