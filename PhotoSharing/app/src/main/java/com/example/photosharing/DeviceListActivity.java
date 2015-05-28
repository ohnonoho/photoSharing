package com.example.photosharing;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;


public class DeviceListActivity extends ActionBarActivity {
    final String TAG = "DeviceListActivity";
    final private PhotoSharingApplication app = (PhotoSharingApplication) getApplication();

    private ListView listView;
    private List<String> deviceDisplayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        Intent intent = getIntent();
        //String[] devices = intent.getStringArrayExtra("devices");
        final ArrayList<PhotoSharingApplication.DeviceInfo> deviceList = app.getDeviceList();
        if ( deviceList.isEmpty() )
            Toast.makeText(getApplicationContext(), "No device discoverable", Toast.LENGTH_LONG).show();
        else {

            deviceDisplayList = new ArrayList<String>();// this is the array of string which is used by listview adapter
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
                    intent.putExtra("isPublic", false);
                    String passcode = "";
                    if (passcode.equals(""))
                        passcode = "123";
                    intent.putExtra("passcode", passcode);

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
}
