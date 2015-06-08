package com.example.photosharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.*;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class BrowsePhotosActivity extends ActionBarActivity {
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private Dialog dialog;
    String TAG = "BrowsePhotosActivity";

    String deviceName = "";
    boolean isPublic = true;
    String passcode = "";
    String enteredPasscode = "";
    String targetPhotoPrefix = "";
    String targetIP = "";

    JSONObject info;

    private ArrayList<ImageItem> imageItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_photos);
        Intent intent = getIntent();
        //TextView tv = (TextView)findViewById(R.id.tmp);
        String jsonString = intent.getStringExtra("info");
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, imageItemList);

        try {
            if(jsonString != null) {
                info = new JSONObject(jsonString);
                deviceName = intent.getStringExtra("deviceName");
                // isPublic = intent.getBooleanExtra("isPublic", true);
                // passcode = intent.getStringExtra("passcode");
                targetIP = intent.getStringExtra("targetIP");
                info.put("deviceName", deviceName);
                // info.put("isPublic", isPublic);
                // info.put("passcode", passcode);
                info.put("targetIP", targetIP);
                Log.i("JSON", info.toString());

                isPublic = info.getBoolean("isPublic");
                passcode = info.getString("passcode");
            }
        } catch (JSONException e) {
            Log.i("On Create", e.toString());
        }
        Log.i("Shwo Images", targetIP);
        Log.i("IsPublic", "" + isPublic);

        // the cotent that someone is sharing is public
        if (isPublic) {
            targetPhotoPrefix = deviceName + "/public";
            displayContent();
        }
        else{
            // pop up an alert and request user to enter passcode
            dialog = new Dialog(BrowsePhotosActivity.this);
            dialog.setContentView(R.layout.dialog_passcode_requirement);
            dialog.setTitle("Enter passcode");
            dialog.setCancelable(false);

            final EditText et = (EditText) dialog.findViewById(R.id.etPasscode);
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            Button btnAcc = (Button) dialog.findViewById(R.id.Access);
            Button btnCancel = (Button) dialog.findViewById(R.id.Cancel);
            btnAcc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et.clearFocus();
                    enteredPasscode = et.getText().toString();
                    String attempt = "";
                    try{
                        attempt = Encryption.encrypt("HelloWorld", enteredPasscode);
                    }
                    catch (Exception e){
                        Log.i(TAG, e.toString());
                    }
                    if( !passcode.equals( attempt )){
                        //showToast
                        Toast.makeText(getApplicationContext(), "Incorrect passcode", Toast.LENGTH_SHORT).show();
                        dialog.show();
                    }else{
                        targetPhotoPrefix = deviceName + "/" + enteredPasscode;
                        Log.i(TAG, "targetPhotoPrefix:" + targetPhotoPrefix);
                        dialog.dismiss();
                        displayContent();
                    }
                }
            });
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et.clearFocus();
                    Intent intent = new Intent(BrowsePhotosActivity.this, MenuActivity.class);
                    startActivity(intent);
                }
            });
            dialog.show();
        }

//        RequestImagesTask task = new RequestImagesTask(imageItemList, gridAdapter);
//        task.execute(info);
    }

//    private ArrayList<ImageItem> getData() {
//        // use the device name to retrive photos from the other device
//        //do something on NFD !!!!!
//        //use targetPhotoPrefix to get photos
////        Log.e(TAG, "targetPhotoPrefix:" + targetPhotoPrefix);
////        final ArrayList<ImageItem> imageItems = new ArrayList<>();
////        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
////        for (int i = 0; i < imgs.length(); i++) {
////            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
////            imageItems.add(new ImageItem(bitmap, "Image#" + i));
////        }
//        RequestImagesTask task = new RequestImagesTask(imageItemList, gridAdapter;
//        task.execute(info);
//        return this.imageItemList;
//    }

    private void displayContent(){

        RequestImagesTask task = new RequestImagesTask(imageItemList, gridAdapter, (PhotoSharingApplication)getApplication());
        task.execute(info);

        setTitle(deviceName + "'s Gallery");
        gridView = (GridView) findViewById(R.id.gridView);
        // gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                //Create intent
                Intent intent = new Intent(BrowsePhotosActivity.this, PhotoDetailsActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse_photos, menu);
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

    //this is the adapter used to fit the grid view
    //Adapter is acts as a bridge between data source and adapter views such as ListView, GridView.
    //Adapter iterates through the data set from beginning till the end and generate Views for each item in the list.
    public class GridViewAdapter extends ArrayAdapter {
        private Context context;
        private int layoutResourceId;
        private ArrayList data = new ArrayList();

        public GridViewAdapter(Context context, int layoutResourceId, ArrayList data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new ViewHolder();
                holder.imageTitle = (TextView) row.findViewById(R.id.text);
                holder.image = (ImageView) row.findViewById(R.id.image);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            ImageItem item = (ImageItem)data.get(position);
            holder.imageTitle.setText(item.getTitle());
            holder.image.setImageBitmap(item.getImage());
            return row;
        }

        public class ViewHolder {
            TextView imageTitle;
            ImageView image;
        }
    }

    // AsyncTask to request the images
    private class RequestImagesTask extends AsyncTask<JSONObject, Void, ArrayList<ImageItem>> {

        private static final String TAG = "Request Image Task";
        private Face mFace;
        private boolean shouldStop = false;
        private ArrayList<ImageItem> images;
        private int seqNumber = Integer.MAX_VALUE;
        private HashMap<Integer, String> results = new HashMap<>();
        private byte[] bitmapData = new byte[0];
        private GridViewAdapter gridViewAdapter;
        private PhotoSharingApplication app;

        public RequestImagesTask(ArrayList<ImageItem> images, GridViewAdapter gridViewAdapter, PhotoSharingApplication app) {
            this.gridViewAdapter = gridViewAdapter;
            this.images = images;
            this.app = app;
        }
        @Override
        protected ArrayList<ImageItem> doInBackground(JSONObject... params) {

            if(params.length < 1) {
                Log.e(RequestImagesTask.TAG, "No images path");
                return null;
            }

            JSONObject info = params[0];
            Log.i(RequestImagesTask.TAG, info.toString());
            ArrayList<String> filePaths = new ArrayList<>();
            boolean isPublic = true;
            String passcode = "";
            String targetIP = "";
            try {
                JSONArray array = info.getJSONArray("filePath");
                for(int i = 0; i < array.length(); ++i) {
                    filePaths.add(array.getString(i));
                }
                isPublic = info.getBoolean("isPublic");
                if(isPublic == false)
                    passcode = info.getString("passcode");
                targetIP = info.getString("targetIP");

            } catch (JSONException e) {
                Log.e(RequestImagesTask.TAG, e.toString());
            }

            try {
                // final KeyChain keyChain = buildTestKeyChain();
                final KeyChain keyChain = app.keyChain;
                mFace = new Face("localhost");
                mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
                for(String path : filePaths) {

                    results.clear();
                    shouldStop = false;

                    String[] strs = path.split("/");
                    String filename = strs[strs.length - 1];

                    final Name requestName;

                    if(isPublic == true) {
                        requestName = new Name(targetIP + "/public/" + filename);
                    }
                    else {
                        requestName = new Name(targetIP + "/" + passcode + "/" + filename);
                    }

                    Name firstRequest = new Name(requestName);
                    firstRequest.appendSequenceNumber(1);
                    Interest interest = new Interest(firstRequest);
                    interest.setInterestLifetimeMilliseconds(20000);
                    mFace.expressInterest(interest, new OnData() {
                        @Override
                        public void onData(Interest interest, Data data) {
                            Log.i(RequestImagesTask.TAG, data.getName().toUri());
                            seqNumber = Integer.parseInt(data.getContent().toString());
                            Log.i(RequestImagesTask.TAG, "" + seqNumber);

                            Face contentFace = new Face("localhost");
                            for(int i = 2; i < 2+seqNumber; ++i) {
                                shouldStop = false;
                                Log.i(RequestImagesTask.TAG, "Request for data sequence " + i);
                                try {
                                    contentFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                                    Name contentName = new Name(requestName);
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
                                                Log.i(RequestImagesTask.TAG, "Receive data " + seqNo);
                                                shouldStop = true;
                                            } catch(EncodingException e) {
                                                Log.e(RequestImagesTask.TAG, e.toString());
                                            }
                                        }
                                    }, new OnTimeout() {
                                        @Override
                                        public void onTimeout(Interest interest) {
                                            Log.e(RequestImagesTask.TAG, "Time Out During Retriving Data");
                                            shouldStop = true;
                                        }
                                    });

                                    while (!shouldStop) {
                                        contentFace.processEvents();
                                    }
                                } catch (SecurityException e) {
                                    Log.e(RequestImagesTask.TAG, e.toString());
                                } catch (IOException e) {
                                    Log.e(RequestImagesTask.TAG, e.toString());
                                } catch (EncodingException e) {
                                    Log.e(RequestImagesTask.TAG, e.toString());
                                }
                            }
                        }
                    }, new OnTimeout() {
                        @Override
                        public void onTimeout(Interest interest) {
                            Log.e(RequestImagesTask.TAG, "Time out");
                            shouldStop = true;
                        }
                    });

                    while(!shouldStop) {
                        mFace.processEvents();
                    }

                    StringBuffer sb = new StringBuffer();
                    if(results.size() != seqNumber) {
                        Log.e(RequestImagesTask.TAG, "Failed to obtain some data, ignore it!");
                        continue;
                    }

                    for(int i = 2; i < 2+seqNumber; i++) {
                        sb.append(results.get(i));
                    }
                    String decodedImgString = sb.toString();
                    if (!isPublic){
                        try {
                            decodedImgString = Encryption.decrypt(sb.toString(), enteredPasscode);
                        }
                        catch (Exception e){
                            Log.i(TAG, e.toString());
                        }
                    }

                    bitmapData = Base64.decode(decodedImgString, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                    ImageItem imageItem = new ImageItem(bitmap, path);
                    images.add(imageItem);
                }

            } catch (net.named_data.jndn.security.SecurityException e) {
                Log.e(RequestImagesTask.TAG, e.toString());
            } catch (IOException e) {
                Log.e(RequestImagesTask.TAG, e.toString());
            } catch (EncodingException e) {
                Log.e(RequestImagesTask.TAG, e.toString());
            }

            return images;
        }

        @Override
        protected void onPostExecute(ArrayList<ImageItem> imageItems) {
            super.onPostExecute(imageItems);
            // imageItemList = imageItems;
            images = imageItems;
            gridViewAdapter.notifyDataSetChanged();
        }
    }

    public class ImageItem {
        private Bitmap image;
        private String title;

        public ImageItem(Bitmap image, String title) {
            super();
            this.image = image;
            this.title = title;
        }

        public Bitmap getImage() {
            return image;
        }

        public void setImage(Bitmap image) {
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
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
