package com.example.photosharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class CustomPhotoGalleryActivity extends ActionBarActivity {
    final String TAG = "CustomPhotoGalleryActivity";
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    boolean[] isChecked;
    String[] arrPath;
    int numTotalPhotos = 0;
    private Cursor imagecursor;

    private PhotoSharingApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_photos);
        //Intent intent = getIntent();
        //TextView tv = (TextView)findViewById(R.id.tmp);
        //tv.setText(intent.getStringExtra("deviceName"));
        app =  (PhotoSharingApplication) getApplication();
        app.clearSelectedPhotoPaths();

        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_select_layout, getData());
        gridView.setAdapter(gridAdapter);
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
//                //Create intent
//
//            }
//        });
    }

    private ArrayList<ImageItem> getData() {
        //get from device's gallary

        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media._ID;
        //@SuppressWarnings("deprecation")
        imagecursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
        if (imagecursor == null){
            Log.i(TAG, "image cursor is null, indicates that there is no images");
            return imageItems;
        }
        int image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
        numTotalPhotos = imagecursor.getCount();
        arrPath = new String[numTotalPhotos];
        isChecked = new boolean[numTotalPhotos];
        for (int i = 0; i <numTotalPhotos; i++) {
            imagecursor.moveToPosition(i);
            arrPath[i] = imagecursor.getString(imagecursor.getColumnIndex(MediaStore.Images.Media.DATA));
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 4;
            Bitmap image = BitmapFactory.decodeFile(arrPath[i], option);
            imageItems.add(new ImageItem(image, "Image#" + i));
        }

        /*
        // use drawable photos to test
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
        for (int i = 0; i < imgs.length(); i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(bitmap, "Image#" + i));
        }
        isChecked = new boolean[imgs.length()];
        arrPath = new String[imgs.length()];
        numTotalPhotos = imgs.length();*/

        return imageItems;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_custom_photo_gallery, menu);
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
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.cancel:
                //Toast.makeText(this, "Are you sure to cancel?", Toast.LENGTH_SHORT).show();
                intent.setClass(CustomPhotoGalleryActivity.this, MenuActivity.class);
                startActivity(intent);
                return true;
            case R.id.share:
                Log.i("Share", "" + numTotalPhotos);
                ArrayList<String> selectedPhotoPaths = new ArrayList<String>();
                int count = 0;
                for (int i = 0; i < numTotalPhotos; i++) {
                    Log.i("Check", "" + i + " : " + isChecked[i]);
                    if (isChecked[i]) {
                        count++;
                        // selectedPhotoPaths.add(arrPath[i]);// = selectImages + arrPath[i] + "|";
                        app.addSelectedPhoto(arrPath[i]);
                        Log.e(TAG, arrPath[i]);
                    }
                }
                if (count == 0) {
                    //Log.e(TAG, "NO photo selected");
                    Toast.makeText(getApplicationContext(), "Please select at least one image", Toast.LENGTH_LONG).show();
                }
                else {
                    //Log.d("SelectedImages", selectedPhotoPaths);
                    intent.putExtra("selectedPhotoPaths", app.getSelectedPhotoPaths().toArray(new String[app.getSelectedPhotoPathsLength()]));
                    intent.setClass(CustomPhotoGalleryActivity.this, ConfirmActivity.class);
                    //Log.e(TAG, "send intent to finishActivity");
                    startActivity(intent);
                    //setResult(Activity.RESULT_OK, i);
                }
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            final ViewHolder holder;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new ViewHolder();
                holder.imageTitle = (TextView) row.findViewById(R.id.text);
                holder.image = (ImageView) row.findViewById(R.id.image);
                holder.chkbox = (CheckBox) row.findViewById(R.id.chkImage);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            ImageItem imgitm = (ImageItem)data.get(position);
            holder.imageTitle.setText(imgitm.title);
            holder.image.setImageBitmap(imgitm.image);
            holder.chkbox.setChecked(isChecked[position]);

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Image", "click!");
                    if (isChecked[position]) {
                        holder.chkbox.setChecked(false);
                        isChecked[position] = false;
                    } else {
                        holder.chkbox.setChecked(true);
                        isChecked[position] = true;
                    }
                }
            });
            return row;
        }
    }

    public class ViewHolder {
        TextView imageTitle;
        ImageView image;
        CheckBox chkbox;
    }
    public class ImageItem {
        public Bitmap image;
        public String title;
        public ImageItem(Bitmap image, String title) {
            super();
            this.image = image;
            this.title = title;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        imagecursor.close();
        Log.v(TAG, "onStop");
    }
}
