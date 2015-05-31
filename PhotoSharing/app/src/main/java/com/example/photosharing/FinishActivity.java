package com.example.photosharing;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class FinishActivity extends ActionBarActivity {
    private Button btnMenu;

    private PhotoSharingApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);


        Intent intent = getIntent();
        boolean isPublic = intent.getBooleanExtra("isPublic", true);
        String passcode = intent.getStringExtra("passcode");
        app = (PhotoSharingApplication) getApplication();
        // String [] selectedPhotoPaths = intent.getStringArrayExtra("selectedPhotoPaths");
        // String[] selectedPhotoPaths = (String[])app.getSelectedPhotoPaths().toArray();
        String[] selectedPhotoPaths = app.getSelectedPhotoPaths().toArray(new String[app.getDeviceListLength()]);
        for (int i = 0 ; i < selectedPhotoPaths.length ; i++){
            app.addSelectedPhoto(selectedPhotoPaths[i]);
        }
        //do something on NFD !!!!!
        //update /ip/info{
        //register photos

        Intent prodIntent = new Intent(this, ProducerService.class);

        prodIntent.putExtra("isPublic", isPublic);
        prodIntent.putStringArrayListExtra("filePath", app.getSelectedPhotoPaths());
        prodIntent.putExtra("passcode", passcode);
        prodIntent.putExtra("mAddress", app.getMyAddress());
        prodIntent.putExtra("oAddress", app.getOwnerAddress());
        prodIntent.putParcelableArrayListExtra("deviceList", app.getDeviceList());

        this.startService(prodIntent);

        btnMenu = (Button) this.findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinishActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_finish, menu);
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
