package com.example.photosharing;

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
import java.util.List;


public class DeviceListActivity extends ActionBarActivity {
    final String TAG = "DeviceListActivity";
    final private PhotoSharingApplication app = (PhotoSharingApplication) getApplication();

    private ListView listView;
    private List<String> deviceDisplayList;
    private JSONObject info;

    private boolean isPublic;
    private String passcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        Intent intent = getIntent();
        //String[] devices = intent.getStringArrayExtra("devices");
        final ArrayList<DeviceInfo> deviceList = app.getDeviceList();
        if ( deviceList.isEmpty() )
            Toast.makeText(getApplicationContext(), "No device discoverable", Toast.LENGTH_LONG).show();
        else {

            deviceDisplayList = new ArrayList<String>();// this is the array of string which is used by listview adapter
            // filePaths = new ArrayList<>(); // this is the array of filepath name to request the images
            int i = 0;
            for (i = 0; i < deviceList.size(); i++) {
                deviceDisplayList.add(deviceList.get(i).ipAddress);
            }
            listView = new ListView(this);
            listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, deviceDisplayList));
            setContentView(listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String targetIP = deviceDisplayList.get(position);
                    Intent intent = new Intent(DeviceListActivity.this, BrowsePhotosActivity.class);
                    intent.putExtra("deviceName", deviceList.get(position).deviceName);
                    intent.putExtra("targetIP", targetIP);
                    //get info from the device
                    //do something on NFD !!!!!
                    //get /target/info, isPublic, passcode

                    RequestInfo task = new RequestInfo();
                    task.execute(targetIP);

                    intent.putExtra("isPublic", isPublic);
                    // String passcode = "";
                    // if (passcode.equals(""))
                    //    passcode = "123";
                    intent.putExtra("passcode", passcode);
                    intent.putExtra("info", info.toString());

                    startActivity(intent);
                }
            });
        }
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
        private JSONObject info;
        private boolean shouldStop = false;
        @Override
        protected JSONObject doInBackground(String... params) {

            try {
                // Send the interest to /oAddres/info
                KeyChain keyChain = buildTestKeyChain();
                mFace = new Face("localhost");
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                if(params.length < 1) {
                    Log.e(RequestInfo.TAG, "No owner address");
                    return null;
                }

                String oAddress = params[0];

                Interest interest = new Interest(new Name("/" + oAddress + "/info"));
                interest.setInterestLifetimeMilliseconds(10000);
                mFace.expressInterest(interest, new OnData() {
                    @Override
                    public void onData(Interest interest, Data data) {

                        String content = data.getContent().toString();
                        // Parse the content to JSONObject
                        try {
                            info = new JSONObject(content);
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

            return info;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if(info != null) {
                info = jsonObject;
            }
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
