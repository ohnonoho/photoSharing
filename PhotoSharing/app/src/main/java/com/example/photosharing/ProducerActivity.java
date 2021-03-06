package com.example.photosharing;

import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnTimeout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ProducerActivity extends ActionBarActivity implements ProducerActivityFragment.ProducerActionListener{

    public static final String TAG = "Producer Activity";

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WiFiDirectBroadcast mReceiver;

    private boolean isWifiP2pEnabled = false;

    private ListView listView;
    private ArrayList<HashMap<String, Object>> displayContent;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private SimpleAdapter adapter;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer);
        Log.e(TAG, "Oncreate");
        //setContentView(R.layout.activity_producer);

        // Indicate a change in the Wi-Fi P2P status
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicate a change in the list of available peers
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicate the state of Wi-Fi P2P connectivity has changed
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicate this device's details have changed
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(this.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcast(mManager, mChannel, this);

        Log.d(this.TAG, "Start to discover peers");

        //for display purpose
        listView = new ListView(this);
        displayContent = new ArrayList<HashMap<String, Object>> ();
        adapter = new SimpleAdapter(this, displayContent,R.layout.listview_content,
                new String[]{"device_name","device_ip", "device_status"},
                new int[]{R.id.device_name, R.id.device_ip, R.id.device_status});
        listView.setAdapter(adapter);
        setContentView(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pDevice device = peers.get(position);
                //WifiP2pDevice device = (WifiP2pDevice) displayContent.get(position).values();

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                ((ProducerActivityFragment.ProducerActionListener) ProducerActivity.this).connect(config);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_producer, menu);
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
        registerReceiver(mReceiver, intentFilter);

        Log.e(TAG, "display content. size:" + displayContent.size());

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(ProducerActivity.this, "Discovery Initiated", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(ProducerActivity.this, "Discovery Failed" + reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(ProducerActivity.this, "Connect failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getIPAddress() {
        return mReceiver.localIP;
    }

    public boolean isOwner() {
        return mReceiver.isOwner;
    }

    public String getOwnerIPAddress() {
        return mReceiver.oAddress;
    }

    public ArrayList<HashMap<String, Object>> getDisplayContent(){
        return displayContent;
    }
    public void clearDisplayContent(){
        displayContent.clear();
    }
    public void addDisplayContent(HashMap<String, Object> map){
        displayContent.add(map);
    }

    public void notifyDataSetChanged(){
        adapter.notifyDataSetChanged();
    }
    public void cleerPeers(){
        peers.clear();
    }
    public void addPeers(WifiP2pDevice p){
        peers.add(p);
    }

}
