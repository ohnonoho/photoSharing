package com.example.photosharing;

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

import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;

import java.util.ArrayList;


public class MenuActivity extends ActionBarActivity {
    private Button btnFindDevices;
    private Button btnGetPhotos;
    private Button btnSharePhotos;
    public static boolean wifiDirectConnected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        btnGetPhotos = (Button) this.findViewById(R.id.btnGetPhotos);
        btnSharePhotos = (Button) this.findViewById(R.id.btnSharePhotos);
        btnFindDevices = (Button) this.findViewById(R.id.btnFindDevices);
        btnGetPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get visible devices first, needs to be updated
                //do something on NFD !!!!!
                //get and update device list

                //app.addDevice()

                Intent intent = new Intent(MenuActivity.this, DeviceListActivity.class);
                //intent.putExtra("devices", devices);
                startActivity(intent);
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
        wifiDirectConnected = true;
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
        @Override
        protected ArrayList<DeviceInfo> doInBackground(String... params) {

            try {
                KeyChain keyChain = buildTestKeyChain();
                mFace = new Face("localhost");
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                // Invalid input
                if(params.length < 1) {
                    Log.e(RequestDeviceListTask.TAG, "No owner address!");
                    return null;
                }

                String oAddress = params[0];

                Interest interest = new Interest(new Name("/" + oAddress + "deviceList"));
                

            } catch (net.named_data.jndn.security.SecurityException e) {

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
