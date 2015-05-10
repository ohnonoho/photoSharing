package com.example.photosharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peiyang on 15/5/7.
 */
public class WiFiDirectBroadcast extends BroadcastReceiver{

    private static final String TAG = "WiFiDirectBroadcast";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private ProducerActivity mProducerActivity;

    private List peers = new ArrayList();

    public WiFiDirectBroadcast(WifiP2pManager manager, WifiP2pManager.Channel channel, ProducerActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mProducerActivity = mProducerActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if wifi p2p model is enable or not, alert the Activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mProducerActivity.setIsWifiP2pEnabled(true);
            } else {
                mProducerActivity.setIsWifiP2pEnabled(false);
            }

        } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if (mManager != null) {
                mManager.requestPeers(mChannel, (WifiP2pManager.PeerListListener)
                        mProducerActivity.getFragmentManager().findFragmentById(R.id.producer_fragment));
            }
            Log.d(WiFiDirectBroadcast.TAG, "P2P peers changed");

        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            ProducerActivityFragment fragment = (ProducerActivityFragment)
                    mProducerActivity.getFragmentManager().findFragmentById(R.id.producer_fragment);
        }
    }


}
