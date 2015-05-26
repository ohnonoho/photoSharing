package com.example.photosharing;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MenuActivity extends ActionBarActivity {
    private Button btnFindDevices;
    private Button btnGetPhotos;
    private Button btnSharePhotos;
    public static boolean wifDirectConnected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        btnGetPhotos = (Button) this.findViewById(R.id.btnGetPhotos);
        btnSharePhotos = (Button) this.findViewById(R.id.btnSharePhotos);
        btnFindDevices = (Button) this.findViewById(R.id.btnFindDevices);
        btnGetPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get visible devices first, needs to be updated
                //do something on NFD !!!!!
                final String[] devices = {"王大傻","李二狗","蠢又笨"};

                Intent intent = new Intent(MenuActivity.this, DeviceListActivity.class);
                intent.putExtra("devices", devices);
                startActivity(intent);
            }
        });
        btnSharePhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, CustomPhotoGalleryActivity.class);
                startActivity(intent);
                //in order to send data
                //intent.putExtra(key,value);
                //in order to get result
                //startActivityForResult(intent, requestcode);
            }
        });
        btnFindDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send to wifi direct activity
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (wifDirectConnected){
            btnGetPhotos.setEnabled(true);
            btnGetPhotos.setBackground(getResources().getDrawable(R.drawable.btnviewothersenable));
            btnSharePhotos.setEnabled(true);
            btnSharePhotos.setBackground(getResources().getDrawable(R.drawable.btnsharemyphotosenable));
        }
        else{
            Toast.makeText(getApplicationContext(), "To get started, you may have to connect to other devices first", Toast.LENGTH_LONG).show();
            btnGetPhotos.setEnabled(false);
            btnGetPhotos.setBackground(getResources().getDrawable(R.drawable.btnviewothersdisable));
            btnSharePhotos.setEnabled(false);
            btnSharePhotos.setBackground(getResources().getDrawable(R.drawable.btnsharemyphotosdisable));
        }
    }
}
