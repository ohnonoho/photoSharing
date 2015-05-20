package com.example.photosharing;

import android.app.ListFragment;
import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterest;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.transport.Transport;
import net.named_data.jndn.util.Blob;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProducerActivityFragment extends ListFragment implements PeerListListener{

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pDevice device = null;
    private View mView = null;

    public ProducerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_producer, container, false);

        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.list_item, peers));

        Button btnProduce = (Button) mView.findViewById(R.id.produce_button);
        btnProduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProduceTask produceTask = new ProduceTask();
                produceTask.execute();
            }
        });

        Button btnRequire = (Button) mView.findViewById(R.id.require_button);
        btnRequire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestTask requestTask = new RequestTask();
                requestTask.execute();
            }
        });
        return mView;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        peers.clear();
        peers.addAll(peerList.getDeviceList());

        Log.d(ProducerActivity.TAG, peerList.toString());

        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(ProducerActivity.TAG, "No devices found");
            return;
        }
    }

    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter)getListAdapter()).notifyDataSetChanged();
    }

    public WifiP2pDevice getDevice() {
        return this.device;
    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(ProducerActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    /**
     * Initiate a connection with the peer
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        ((ProducerActionListener)getActivity()).connect(config);
    }

    /**
     * ArrayAapater for ListFragment to maintain the peer list.
     */

    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;



        public WiFiPeerListAdapter(Context context, int resource, List<WifiP2pDevice> objects) {
            super(context, resource, objects);
            this.items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }
            WifiP2pDevice device = items.get(position);
            if(device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_status);

                top.setText("Name: " + device.deviceName + ", Address: " + device.deviceAddress);
                bottom.setText(getDeviceStatus(device.status));
            }
            return v;
        }
    }

    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView top = (TextView) mView.findViewById(R.id.producer_name);
        top.setText("Device: " + device.deviceName + ", Address: " + device.deviceAddress);
        TextView bottom = (TextView) mView.findViewById(R.id.producer_status);
        bottom.setText("Status: " + getDeviceStatus(device.status));
    }

    public void updateGroupOwner(boolean isOwner, String oAddress) {
//        TextView groupOwnerName = (TextView) mView.findViewById(R.id.group_owner_name);
//        groupOwnerName.setText("Owner Name: " + name);
        TextView groupOwnerAddress = (TextView) mView.findViewById(R.id.group_owner_address);
        groupOwnerAddress.setText("Owner Address: " + oAddress);
        TextView ownerStatus = (TextView) mView.findViewById(R.id.owner_status);
        ownerStatus.setText("Owner Status: " + isOwner);
    }

    public void updateMyAddress(String addr) {
//        TextView groupOwnerName = (TextView) mView.findViewById(R.id.group_owner_name);
//        groupOwnerName.setText("Owner Name: " + name);
        TextView myAddress = (TextView) mView.findViewById(R.id.my_address);
        myAddress.setText("My Address: " + addr);
    }

    public interface ProducerActionListener {
        public void connect(WifiP2pConfig config);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ProduceTask extends AsyncTask<Void, Void, Void> {

        private Face mFace;

        private static final String TAG = "Produce Task";

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KeyChain keyChain = buildTestKeyChain();
                mFace = new Face("localhost");
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
                mFace.registerPrefix(new Name("/test"),
                        new OnInterest() {
                            @Override
                            public void onInterest(Name name, Interest interest, Transport transport, long l) {
                                Data data = new Data(interest.getName());
                                data.setContent(new Blob("This is the test data!"));
                                try {
                                    Log.i(ProduceTask.TAG, "The data has been send");
                                    mFace.putData(data);
                                } catch(IOException e) {
                                    Log.e(ProduceTask.TAG, "Failed to send data");
                                }
                            }
                        },
                        new OnRegisterFailed() {
                            @Override
                            public void onRegisterFailed(Name name) {
                                Log.e(ProduceTask.TAG, "Failed to register the data");
                            }
                        }
                );

                while(true) {
                    mFace.processEvents();
                }
            } catch (Exception e){
                Log.e(ProduceTask.TAG, e.toString());
            }

            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class RequestTask extends AsyncTask<Void, Void, String> {

        private static final String TAG = "Request Task";

        private Face mFace;
        private String receiveVal = "I have not received data";
        private boolean shouldStop = false;
        @Override
        protected String doInBackground(Void... params) {

            try {
                mFace = new Face("localhost");
                KeyChain keyChain = buildTestKeyChain();
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                Log.i(RequestTask.TAG, "Send the request");

                Interest interest = new Interest(new Name("/test"));
                interest.setInterestLifetimeMilliseconds(10000);
                mFace.expressInterest(interest, new OnData() {
                    @Override
                    public void onData(Interest interest, Data data) {
                        Log.i(RequestTask.TAG, "The data has been received");
                        receiveVal = data.getContent().toString();
                        shouldStop = true;
                    }
                }, new OnTimeout() {
                    @Override
                    public void onTimeout(Interest interest) {
                        Log.e(RequestTask.TAG, "TimeOut!");
                    }
                });

                while(!shouldStop) {
                    // Log.i(RequestTask.TAG, "Requiring For the Data");
                    mFace.processEvents();
                }
            } catch (Exception e) {
                Log.e(RequestTask.TAG, e.toString());
            }
            return receiveVal;
        }

        @Override
        protected void onPostExecute(String s) {
            TextView view = (TextView)mView.findViewById(R.id.result_text);
            view.setText(s);
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
