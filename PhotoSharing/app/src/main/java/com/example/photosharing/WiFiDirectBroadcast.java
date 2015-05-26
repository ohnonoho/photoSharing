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


import com.google.protobuf.ByteString;
import com.intel.jndn.management.NFD;

import com.example.photosharing.ControlParametersProto.ControlParametersTypes.ControlParametersMessage;
import net.named_data.jndn.ControlParameters;
import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.ProtobufTlv;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.ForwardingFlags;
import net.named_data.jndn.util.Blob;

import java.net.Inet4Address;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by peiyang on 15/5/7.
 */
public class WiFiDirectBroadcast extends BroadcastReceiver{

    private static final String TAG = "WiFiDirectBroadcast";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private ProducerActivity mProducerActivity;

    String oAddress;
    String localIP;
    boolean isOwner;


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

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(ProducerActivity.TAG, "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            Log.e(ProducerActivity.TAG, "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

    private class RequestOwner extends AsyncTask<Void, Void, Void> {
        private InetAddress groupOwnerAddress;
        private ProducerActivityFragment fragment;
        // private String oAddress;
        // private String localIP;
        // private boolean isOwner;
        private Face mFace;

        private String returnData = "No return data";

        @Override
        protected Void doInBackground(Void... params) {
            mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo info) {

                    try {
                        groupOwnerAddress = info.groupOwnerAddress;
                        fragment = (ProducerActivityFragment)
                                mProducerActivity.getFragmentManager().findFragmentById(R.id.producer_fragment);
                        oAddress = groupOwnerAddress.getHostAddress();
                        isOwner = info.isGroupOwner;
                        mFace = new Face("localhost");
                        KeyChain keyChain = ProducerActivityFragment.buildTestKeyChain();
                        mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
                        // String name = groupOwnerAddress.getHostName();
                        if (!isOwner) {
                            localIP = getDottedDecimalIP(getLocalIPAddress());
                            //NFD nfd = new NFD();
//                            Thread thread = new Thread(new Runnable(){
//
//                                class RegisterTask extends AsyncTask<Void, Void, Integer> {
//
//                                    Nfdc nfdc = new Nfdc();
//                                    @Override
//                                    protected Integer doInBackground(Void... params) {
//                                        int mFaceID = 0;
//                                        try {
//                                            mFaceID = nfdc.faceCreate("udp://" + oAddress);
//                                            nfdc.ribRegisterPrefix(new Name("/" + oAddress + "/test"), mFaceID, 10, true, false);
//                                            // nfdc.ribRegisterPrefix(new Name("/test2"), mFaceID, 10, true, false);
//                                            nfdc.shutdown();
//                                        } catch(Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        return mFaceID;
//                                    }
//
//                                }
//                                @Override
//                                public void run() {
//                                    try {
//                                        RegisterTask task = new RegisterTask();
//                                        task.execute();
//
/////////////////// //////////////////////////////////////////////////////////////////////////////////////////////////////
//
//
////                                        Face face = new Face();
////                                        ControlParametersMessage.Builder builder = ControlParametersMessage.newBuilder();
////                                        builder.getControlParametersBuilder().setUri("udp4://" + oAddress + ":6363");
////                                        ControlParametersProto.ControlParametersTypes.Name.Builder nameBuilder =
////                                                builder.getControlParametersBuilder().getNameBuilder();
////                                        Name prefix = new Name("/test");
////                                        for (int i = 0; i < prefix.size(); ++i)
////                                            nameBuilder.addComponent(ByteString.copyFrom(prefix.get(i).getValue().buf()));
////                                        builder.getControlParametersBuilder().setFaceId(280);
////                                        builder.getControlParametersBuilder().setOrigin(255);
////                                        builder.getControlParametersBuilder().setCost(0);
////                                        builder.getControlParametersBuilder().setFlags(1);
////                                        Blob encodedControlParameters = ProtobufTlv.encode(builder.build());
////
////                                        Interest interest = new Interest(new Name("/localhost/nfd/rib/register"));
////                                        interest.getName().append(encodedControlParameters);
////                                        interest.setInterestLifetimeMilliseconds(10000);
////
////                                        // Sign and express the interest.
////                                        face.makeCommandInterest(interest);
////
////
////                                        face.expressInterest(interest,
////                                                new OnData() {
////                                                    @Override
////                                                    public void onData(Interest interest, Data data) {
////                                                        Log.i(ProducerActivity.TAG, interest.getName().toString());
////                                                        Log.i(ProducerActivity.TAG, data.getContent().toString());
////                                                        returnData = "Register Success";
////                                                    }
////                                                },
////                                                new OnTimeout() {
////                                                    @Override
////                                                    public void onTimeout(Interest interest) {
////                                                        Log.e(ProducerActivity.TAG, "Time out!!!!!!!!!!!");
////                                                        returnData = "Register Failed";
////                                                    }
////                                                });
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
//                            thread.run();
//                            Log.i(ProducerActivity.TAG, "register");

                        } else {
                            Log.i(ProducerActivity.TAG, "i'm not the owner");
                            localIP = oAddress;
                        }
                        Log.i(ProducerActivity.TAG, "Owner Address: " + oAddress);
                        Log.i(ProducerActivity.TAG, "My Address:" + localIP);
                        Log.i(ProducerActivity.TAG, "Register status: " + returnData);
                        fragment.updateGroupOwner(isOwner, oAddress);


                        fragment.updateMyAddress(localIP);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(ProducerActivity.TAG, e.toString());
                    }
                }
            });


            return null;
        }
    }

}
