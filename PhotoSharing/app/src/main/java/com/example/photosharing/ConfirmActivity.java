package com.example.photosharing;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;


public class ConfirmActivity extends ActionBarActivity {
    final String TAG="ConfirmActivity";
    private Button btnConfirm;
    private Switch switchPrivacy;
    private ImageView hint;
    private boolean isPublic = true;
    private EditText passcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        btnConfirm = (Button) this.findViewById(R.id.btnBackToMenu);
        switchPrivacy = (Switch) this.findViewById(R.id.switchPrivacy);
        hint = (ImageView) this.findViewById(R.id.imghint);
        passcode = (EditText) this.findViewById(R.id.passcode);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ConfirmActivity.this, FinishActivity.class);
                //startActivity(intent);
            }
        });
        Intent intent = getIntent();
        String[] selectedPhotoPaths = intent.getStringArrayExtra("selectedPhotoPaths");
        Log.e(TAG, "LENGTH:" + selectedPhotoPaths.length);
        //do something on NFD !!!!!
        int i = 0;
        for (i =0 ; i < selectedPhotoPaths.length; i ++){
            Log.e(TAG, selectedPhotoPaths[i]);
        }
        switchPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPublic = !isPublic;
                if (isPublic){
                    switchPrivacy.setChecked(true);
                    switchPrivacy.setText("Public ");
                    hint.setBackground(getResources().getDrawable(R.drawable.imgpublichint));
                    passcode.setVisibility(View.INVISIBLE);
                }
                else{
                    switchPrivacy.setChecked(false);
                    switchPrivacy.setText("Private");
                    hint.setBackground(getResources().getDrawable(R.drawable.imgprivatehint));
                    passcode.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent = new Intent();
        //noinspection SimplifiableIfStatement
        if (id == R.id.cancel) {
                //Toast.makeText(this, "Are you sure to cancel?", Toast.LENGTH_SHORT).show();
                intent.setClass(ConfirmActivity.this, MenuActivity.class);
                startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
