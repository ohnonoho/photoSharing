package com.example.photosharing;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterest;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.transport.Transport;
import net.named_data.jndn.util.Blob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by peiyang on 15/5/28.
 */
public class ProducerService extends IntentService {

    private ArrayList<String> prefixData = new ArrayList<>();
    private static final String TAG = "Producer Service";
    private Face mFace;

    public ProducerService() {
        super("ProducerService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ProducerService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            KeyChain keyChain = buildTestKeyChain();
            mFace = new Face("localhost");
            mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

            String myIP = intent.getStringExtra("IP");
            Log.i(ProducerService.TAG, "My Address is: " + myIP);
            mFace.registerPrefix(new Name("/" + myIP + "/test"), new OnInterest() {
                @Override
                public void onInterest(Name name, Interest interest, Transport transport, long l) {
                    try {
                        prefixData.clear();
                        Name requestName = interest.getName();
                        String component = requestName.get(2).toEscapedString();
                        int seqNo = (int) requestName.get(3).toSequenceNumber();
                        Data data = new Data(requestName);

                        // Read in the image here
                        Drawable d = getResources().getDrawable(R.drawable.py6);
                        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bitmapdata = stream.toByteArray();
                        String content = Base64.encodeToString(bitmapdata, Base64.DEFAULT);

                        // Split the data
                        int fixLength = 8000;
                        int cnt = (content.length() / fixLength) + 1;

                        for (int i = 0; i < cnt; i++) {
                            prefixData.add(content.substring(i * fixLength, Math.min((i + 1) * fixLength, content.length())));
                        }

                        if (seqNo == 1) {
                            data.setContent(new Blob("" + prefixData.size()));
                        } else {
                            data.setContent(new Blob(prefixData.get(seqNo - 2)));
                        }
                        mFace.putData(data);
                        Log.i(ProducerService.TAG, "Send out the data " + interest.getName().toUri());

                    } catch (EncodingException e) {
                        Log.e(ProducerService.TAG, "Failed to encode");
                    } catch (IOException e) {
                        Log.e(ProducerService.TAG, "Failed to send the data");
                    }
                }
            }, new OnRegisterFailed() {
                @Override
                public void onRegisterFailed(Name name) {
                    Log.e(ProducerService.TAG, "Failed to register the data");
                }
            });

            while(true) {
                // Log.i(ProducerService.TAG, "Service is running");
                mFace.processEvents();
            }

        } catch (Exception e) {
            Log.e(ProducerService.TAG, e.toString());
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
