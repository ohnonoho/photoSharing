package com.example.photosharing;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.os.Bundle;

import android.security.*;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterest;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.transport.Transport;
import net.named_data.jndn.util.Blob;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProducerActivityFragment extends ListFragment implements PeerListListener{

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pDevice device = null;
    private View mView = null;

    // The map used to hold the prefix and the data
    HashMap<String, String> prefixMap = new HashMap<>();
    HashMap<String, String> dataMap = new HashMap<>();

    public ProducerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_producer, container, false);

        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.list_item, peers));

        // prefixMap.put("/test1", "This is test data 1.");
        // prefixMap.put("/test2", "This is test data 2.");

        Button btnProduce = (Button) mView.findViewById(R.id.produce_button);
        btnProduce.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String localIP = ((ProducerActivity)getActivity()).getIPAddress();
                // prefixMap.put("/" + localIP + "/test", "");
                // prefixMap.put("/" + localIP + "/test1", "This is test data 1!");
                // dataMap.put("1", "This is test data 1!");
                // prefixMap.put("/" + localIP + "/test2", "This is test data 2!");
                // dataMap.put("2", "This is test data 2!");
                // ProduceTask produceTask = new ProduceTask();
                // produceTask.execute();
                Intent intent = new Intent(getActivity(), ProducerService.class);
                intent.putExtra("IP", ((ProducerActivity) getActivity()).getIPAddress());
                getActivity().startService(intent);
            }
        });

        final Button btnRequire1 = (Button) mView.findViewById(R.id.require_button1);
        btnRequire1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestTask requestTask = new RequestTask();
                String prefix = btnRequire1.getText().toString();
                requestTask.execute(prefix);
            }
        });

        final Button btnRequire2 = (Button) mView.findViewById(R.id.require_button2);
        btnRequire2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestTask requestTask = new RequestTask();
                String prefix = btnRequire2.getText().toString();
                requestTask.execute(prefix);
            }
        });

        Button btnInfo = (Button) mView.findViewById(R.id.info_button);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestInfoTask task = new RequestInfoTask();
                task.execute();
            }
        });

        Button btnSync = (Button) mView.findViewById(R.id.sync_button);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterNDF registerTask = new RegisterNDF();
                registerTask.execute();
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
        private String prefix;
        private ArrayList<String> prefixData = new ArrayList<>();

        private static final String TAG = "Produce Task";

        @Override
        protected Void doInBackground(Void... params) {
            try {
                KeyChain keyChain = buildTestKeyChain();
                mFace = new Face("localhost");
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                String myIP = ((ProducerActivity)getActivity()).getIPAddress();

                // prefixData.add("This is test data1!");
                // prefixData.add("This is test data2!");

//                for(String key : prefixMap.keySet()) {
////                    mFace = new Face("localhost");
////                    mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
//                    prefix = key;
//                    mFace.registerPrefix(new Name(key), new OnInterest() {
//                        @Override
//                        public void onInterest(Name name, Interest interest, Transport transport, long l) {
//                            Data data = new Data(interest.getName());
//                            prefix = interest.getName().toUri();
//                            data.setContent(new Blob(prefixMap.get(interest.getName().toUri())));
//                            try {
//                                Log.i(ProduceTask.TAG, "The data has been send.");
//                                mFace.putData(data);
//                            } catch(IOException e) {
//                                Log.e(ProduceTask.TAG, "Failed to send data" + interest.getName().toString());
//                            }
//                        }
//                    }, new OnRegisterFailed() {
//                        @Override
//                        public void onRegisterFailed(Name name) {
//                            Log.e(ProduceTask.TAG, "Failed to register the data");
//                        }
//                    });
//                    // mFaces.add(mFace);
//                }

                mFace.registerPrefix(new Name("/" + myIP + "/test"), new OnInterest() {
                    @Override
                    public void onInterest(Name name, Interest interest, Transport transport, long l) {
//                        Data data = new Data(interest.getName());
//                        // prefix = interest.getName().toUri();
//                        String component = interest.getName().get(2).toEscapedString();
//                        Log.i(ProduceTask.TAG, component);
//                        data.setContent(new Blob(dataMap.get(component)));
//                        try {
//                            Log.i(ProduceTask.TAG, "The data has been send." + component);
//                            mFace.putData(data);
//                        } catch(IOException e) {
//                            Log.e(ProduceTask.TAG, "Failed to send data" + interest.getName().toString());
//                        }
//                        Name requestName = new Name(interest.getName());
//                        String component = requestName.get(2).toEscapedString();
//                        requestName.appendSequenceNumber(1);
//                        Log.i(ProduceTask.TAG, requestName.toUri());
//                        Data data = new Data(requestName);
//                        Log.i(ProduceTask.TAG, component);
//                        data.setContent(new Blob("2"));
//                        try {
//                            mFace.putData(data);
//                            for (int i = 2; i <= 3; i++) {
//                                Name cName = new Name(interest.getName());
//                                cName.appendSequenceNumber(i);
//                                Data content = new Data(cName);
//                                content.setContent(new Blob(dataMap.get(component)));
//                                Log.i(ProduceTask.TAG, "Send out the data sequence " + cName.toUri());
//                                mFace.putData(content);
//                            }
//                        } catch(IOException e) {
//                            Log.e(ProduceTask.TAG, "Failed to send data");
//                        }
                        try {
                            prefixData.clear();
                            Name requestName = interest.getName();
                            String component = requestName.get(2).toEscapedString();
                            int seqNo = (int)requestName.get(3).toSequenceNumber();
                            Data data = new Data(requestName);

                            // Read in the image here
                            Drawable d = getActivity().getResources().getDrawable(R.drawable.py6);
                            Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] bitmapdata = stream.toByteArray();
                            String content = Base64.encodeToString(bitmapdata, Base64.DEFAULT);

                            // Split the data
                            int fixLength = 8000;
                            int cnt = (content.length() / fixLength) + 1;

                            for(int i = 0; i < cnt; i++) {
                                prefixData.add(content.substring(i*fixLength, Math.min((i+1)*fixLength, content.length())));
                            }

                            if(seqNo == 1) {
                                data.setContent(new Blob(""+prefixData.size()));
                            }
                            else {
                                data.setContent(new Blob(prefixData.get(seqNo - 2)));
                            }
                            mFace.putData(data);
                            Log.i(ProduceTask.TAG, "Send out the data " + interest.getName().toUri());

                        } catch(EncodingException e) {
                            Log.e(ProduceTask.TAG, "Failed to encode");
                        } catch (IOException e) {
                            Log.e(ProduceTask.TAG, "Failed to send the data");
                        }
                    }

                }, new OnRegisterFailed() {
                    @Override
                    public void onRegisterFailed(Name name) {
                        Log.e(ProduceTask.TAG, "Failed to register the data");
                    }
                });


                while(true) {
                    mFace.processEvents();
                }
            } catch (Exception e){
                Log.e(ProduceTask.TAG, e.toString() + " " + prefix);
            }

            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class RequestTask extends AsyncTask<String, Void, String> {

        private static final String TAG = "Request Task";

        private Face mFace;
        private String receiveVal = "I have not received data";
        private boolean shouldStop = false;
        private HashMap<Integer, String> results = new HashMap<Integer, String>();
        private int seqNumber = Integer.MAX_VALUE;
        private KeyChain keyChain;
        private byte[] bitmapdata = new byte[0];
        @Override
        protected String doInBackground(final String... params) {

            if(params.length == 0)
                return null;

            try {
                Log.i(RequestTask.TAG, "Request for " + params[0]);
                mFace = new Face("localhost");
                keyChain = buildTestKeyChain();
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                final String oAddress = ((ProducerActivity)getActivity()).getOwnerIPAddress();
                Log.i(RequestTask.TAG, "Send the request to" + oAddress);

                Name requestName = new Name("/" + oAddress + params[0]);
                requestName.appendSequenceNumber(1);
                Interest interest = new Interest(requestName);
                interest.setInterestLifetimeMilliseconds(100000);
                mFace.expressInterest(interest, new OnData() {
                    @Override
                    public void onData(Interest interest, Data data) {
                        try {
//                            Name respondName = data.getName();
//                            int nameSize = respondName.size();
//                            Log.i(RequestTask.TAG, String.valueOf(nameSize));
//                            long seqNo = respondName.get(nameSize - 1).toSequenceNumber();
//                            if(seqNo == 1) {
//                                seqNumber = Long.parseLong(data.getContent().toString());
//                            }
//                            else {
//                                results.put(seqNo - 1, data.getContent().toString());
//                                Log.i(RequestTask.TAG, "" + results.keySet().size());
//                                if(results.keySet().size() == seqNumber) {
//                                    StringBuilder sb = new StringBuilder();
//                                    for(long i = 1; i <= seqNumber; i++) {
//                                        sb.append(results.get(i));
//                                    }
//                                    receiveVal = sb.toString();
//                                    shouldStop = true;
//                                }
//                            }
//                            Log.i(RequestTask.TAG, "" + seqNumber);
                            Log.i(RequestTask.TAG, data.getName().toUri());
                            seqNumber = Integer.parseInt(data.getContent().toString());
                            Log.i(RequestTask.TAG, "" + seqNumber);
                            Face contentFace = new Face("localhost");
                            for(int i = 2; i < 2+seqNumber; i++) {
                                shouldStop = false;
                                Log.i(RequestTask.TAG, "Request for data sequence " + i);
                                // Face contentFace = new Face("localhost");
                                contentFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                                Name contentName = new Name("/" + oAddress + params[0]);
                                contentName.appendSequenceNumber(i);
                                Interest cInterest = new Interest(contentName);
                                cInterest.setInterestLifetimeMilliseconds(10000);
                                contentFace.expressInterest(cInterest, new OnData() {
                                    @Override
                                    public void onData(Interest interest, Data data) {
                                        try {
                                            Name dName = data.getName();
                                            int size = dName.size();
                                            int seqNo = (int) dName.get(size - 1).toSequenceNumber();
                                            String content = data.getContent().toString();
                                            results.put(seqNo, content);
                                            Log.i(RequestTask.TAG, "" + results.keySet().size());
                                            // Log.i(RequestTask.TAG, "" + content);
                                            shouldStop = true;
                                        } catch(EncodingException e) {
                                            Log.i(RequestTask.TAG, data.getName().toUri());
                                            Log.e(RequestTask.TAG, "Encoding Failed " + seqNumber);
                                        }
                                    }
                                }, new OnTimeout() {
                                    @Override
                                    public void onTimeout(Interest interest) {
                                        Log.e(RequestTask.TAG, "Time Out During Retriving Data");
                                        shouldStop = true;
                                    }
                                });

                                while(!shouldStop) {
                                    contentFace.processEvents();
                                }
                            }
                        } catch (SecurityException e) {
                            Log.e(RequestTask.TAG, "Security Failed " + seqNumber);
                        } catch (IOException e) {
                            Log.e(RequestTask.TAG, "IO Exception");
                        } catch (EncodingException e) {
                            Log.e(RequestTask.TAG, "Encoding Failed");
                        }

                        StringBuffer sb = new StringBuffer();
                        for(int i = 2; i < 2 + seqNumber; i++) {
                            sb.append(results.get(i));
                        }
                        // receiveVal = sb.toString();
                        bitmapdata = Base64.decode(sb.toString(), Base64.DEFAULT);
                    }
                }, new OnTimeout() {
                    @Override
                    public void onTimeout(Interest interest) {
                        Log.e(RequestTask.TAG, "TimeOut!");
                        shouldStop = true;
                    }
                });

                while(!shouldStop) {
                    mFace.processEvents();
                }
            } catch (Exception e) {
                Log.e(RequestTask.TAG, e.toString());
            }
            return receiveVal;
        }

        @Override
        protected void onPostExecute(String s) {
            // TextView view = (TextView)mView.findViewById(R.id.result_text);
            // view.setText(s);
            Drawable image = new BitmapDrawable(BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length));
            ImageView view = (ImageView)mView.findViewById(R.id.picture);
            view.setImageDrawable(image);
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

    private class RegisterNDF extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Thread thread = new Thread(new Runnable(){

                class RegisterTask extends AsyncTask<Void, Void, Integer> {

                    String oAddress = ((ProducerActivity)getActivity()).getOwnerIPAddress();
                    @Override
                    protected Integer doInBackground(Void... params) {
                        int mFaceID = 0;
                        try {
                            Nfdc nfdc = new Nfdc();
                            mFaceID = nfdc.faceCreate("udp://" + oAddress);
                            nfdc.ribRegisterPrefix(new Name("/" + oAddress), mFaceID, 10, true, false);
                            nfdc.shutdown();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        return mFaceID;
                    }

                }
                @Override
                public void run() {
                    try {
                        RegisterTask task = new RegisterTask();
                        task.execute();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.run();
            Log.i(ProducerActivity.TAG, "register");

            return null;
        }
    }

    private class RequestInfoTask extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "Request Info Task";
        private Face mFace;
        private boolean shouldStop = false;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                // Send the interest to /IP/info
                KeyChain keyChain = buildTestKeyChain();
                mFace = new Face("localhost");
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                String oAddress = ((ProducerActivity)getActivity()).getOwnerIPAddress();

                Interest interest = new Interest(new Name("/" + oAddress + "/info"));
                interest.setInterestLifetimeMilliseconds(10000);
                mFace.expressInterest(interest, new OnData() {
                    @Override
                    public void onData(Interest interest, Data data) {
                        String content = data.getContent().toString();
                        try {
                            JSONObject json = new JSONObject(content);
                            JSONArray array = json.getJSONArray("file");
                            Log.i(RequestInfoTask.TAG, "JSON mode:" + json.getString("mode"));
                            for(int i = 0; i < array.length(); ++i) {
                                Log.i(RequestInfoTask.TAG, "JSON file: " + array.getString(i));
                            }
                        } catch (JSONException e) {
                            Log.e(RequestInfoTask.TAG, "Failed to construct the JSON");
                        }
                        shouldStop = true;
                    }
                }, new OnTimeout() {
                    @Override
                    public void onTimeout(Interest interest) {
                        Log.e(RequestInfoTask.TAG, "Time Out!");
                    }
                });

                while(!shouldStop) {
                    mFace.processEvents();
                }

            } catch(SecurityException e) {
                Log.e(RequestInfoTask.TAG, "Security Failed");
            } catch(IOException e) {
                Log.e(RequestInfoTask.TAG, e.toString());
            } catch(EncodingException e) {
                Log.e(RequestInfoTask.TAG, e.toString());
            }

            return null;
        }
    }
}
