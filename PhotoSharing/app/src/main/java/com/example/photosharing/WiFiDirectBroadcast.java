package com.example.photosharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import android.os.AsyncTask;
import android.util.Log;

import com.intel.jndn.management.NFD;

import net.named_data.jndn.Face;
import net.named_data.jndn.Name;

import java.net.InetAddress;
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
        this.mProducerActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.d(ProducerActivity.TAG, action);
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if wifi p2p model is enable or not, alert the Activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.i(ProducerActivity.TAG, "WiFi P2P Enabled");
                mProducerActivity.setIsWifiP2pEnabled(true);
            } else {
                Log.i(ProducerActivity.TAG, "WiFi P2P Not Enabled");
                mProducerActivity.setIsWifiP2pEnabled(false);
            }

        } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if (mManager != null) {
                mManager.requestPeers(mChannel, (WifiP2pManager.PeerListListener)
                        mProducerActivity.getFragmentManager().findFragmentById(R.id.producer_fragment));
            }
            Log.i(WiFiDirectBroadcast.TAG, "P2P peers changed");

        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            Log.i(ProducerActivity.TAG, "Connection Changed");

            if(mManager != null) {
                NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()) {
                    RequestOwner task = new RequestOwner();
                    task.execute();
                }
            }

        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            ProducerActivityFragment fragment = (ProducerActivityFragment)
                    mProducerActivity.getFragmentManager().findFragmentById(R.id.producer_fragment);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private class RequestOwner extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo info) {

                    try {
                        InetAddress groupOwnerAddress = info.groupOwnerAddress;
                        ProducerActivityFragment fragment = (ProducerActivityFragment)
                                mProducerActivity.getFragmentManager().findFragmentById(R.id.producer_fragment);
                        String oAddress = groupOwnerAddress.getHostAddress();
                        boolean isOwner = info.isGroupOwner;
                        // String name = groupOwnerAddress.getHostName();
                        Log.i(ProducerActivity.TAG, "Owner Address: " + oAddress);
                        fragment.updateGroupOwner(isOwner, oAddress);

                        // Register the prefix on the slaves NFD
                        // The ip address now is hard code to see if we could register the prefix on NFD using the libaray
                        if (isOwner == false) {
                            NFD.register(new Face("localhost"), "udp://192.168.49.1", new Name("/test"), 1);
                        }
                    } catch(Exception e) {
                        Log.e(ProducerActivity.TAG, e.toString());
                    }
                }
            });
            return null;
        }
    }

}
