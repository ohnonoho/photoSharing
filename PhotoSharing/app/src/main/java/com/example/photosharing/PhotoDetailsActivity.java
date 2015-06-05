package com.example.photosharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class PhotoDetailsActivity extends ActionBarActivity {
    private String TAG = "PhotoDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_details);

        String title = getIntent().getStringExtra("title");
        final Bitmap bitmap = getIntent().getParcelableExtra("image");

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(title);

        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);
        imageView.setLongClickable(true);
        // long press to save images
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(PhotoDetailsActivity.this);
                alert.setMessage("Save the image to gallery?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do your work here
                        //needs to be updated
                        dialog.dismiss();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
                        Bitmap combination = bitmap;//get my bitmap!
                        //save in gallery
                        String url = MediaStore.Images.Media.insertImage(PhotoDetailsActivity.this.getContentResolver(), bitmap,"test_"+ timeStamp + ".jpg",timeStamp.toString());
//                        ContentValues values = new ContentValues();
//                        values.put(MediaStore.Images.Media.TITLE, title);
//                        values.put(MediaStore.Images.Media.DESCRIPTION, description);
//                        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//                        values.put(MediaStore.MediaColumns.DATA, filepath);
                        if (url != null){
                            Toast.makeText(PhotoDetailsActivity.this, "Image saved.", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Image saved");
                        }
                        else {
                            Toast.makeText(PhotoDetailsActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "save failed");
                        }
                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }  );

                alert.show();
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_details, menu);
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
}
