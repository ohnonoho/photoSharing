package com.example.photosharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
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

import java.util.ArrayList;


public class BrowsePhotosActivity extends ActionBarActivity {
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private Dialog dialog;
    String TAG = "BrowsePhotosActivity";

    String deviceName;
    boolean isPublic;
    int picNum;
    String passcode;
    String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_photos);
        Intent intent = getIntent();
        //TextView tv = (TextView)findViewById(R.id.tmp);
        deviceName = intent.getStringExtra("deviceName");
        isPublic = intent.getBooleanExtra("isPublic", true);
        picNum = intent.getIntExtra("picNum", 1);
        passcode = intent.getStringExtra("passcode");

        // the cotent that someone is sharing is public
        if (isPublic ) {
            photoPath = deviceName + "/public";
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
                    if( !passcode.equals( et.getText().toString() )){
                        //showToast
                        Toast.makeText(getApplicationContext(), "Incorrect passcode", Toast.LENGTH_SHORT).show();
                        dialog.show();
                    }else{
                        photoPath = deviceName + "/" + passcode;
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





    }

    private ArrayList<ImageItem> getData() {
        // use the device name to retrive photos from the other device
        //do something on NFD !!!!!
        //use photoPath to get photos
        Log.e(TAG, "photoPath:" + photoPath);
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
        for (int i = 0; i < imgs.length(); i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(bitmap, "Image#" + i));
        }
        return imageItems;
    }

    private void displayContent(){
        setTitle(deviceName + "'s Gallery");
        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
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
}
