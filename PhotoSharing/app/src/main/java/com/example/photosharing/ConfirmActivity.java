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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;


public class ConfirmActivity extends ActionBarActivity {
    final String TAG="ConfirmActivity";
    private Button btnConfirm;
    private RadioGroup switchPrivacy;
    private ImageView hint;
    private boolean isPublic = true;
    private EditText passcode;
    private TextView passcodeHint;
    private String[] selectedPhotoPaths;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        btnConfirm = (Button) this.findViewById(R.id.btnConfirm);
        switchPrivacy = (RadioGroup) this.findViewById(R.id.switchPrivacy);
        hint = (ImageView) this.findViewById(R.id.imghint);
        passcodeHint = (TextView) this.findViewById(R.id.passcodeHint);
        passcode = (EditText) this.findViewById(R.id.passcode);

        //get data from CustomPhotoGalleryActivity
        //get selected photos
        Intent intent = getIntent();
        selectedPhotoPaths = intent.getStringArrayExtra("selectedPhotoPaths");
        Log.e(TAG, "LENGTH:" + selectedPhotoPaths.length);
        //do something on NFD !!!!!
        int i = 0;
        for (i =0 ; i < selectedPhotoPaths.length; i ++){
            Log.e(TAG, selectedPhotoPaths[i]);
        }

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ConfirmActivity.this, FinishActivity.class);
                //startActivity(intent);
            }
        });

        final RadioButton btnPub = (RadioButton) this.findViewById(R.id.rdbtnPublic);
        switchPrivacy.check(btnPub.getId());
        switchPrivacy.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
               if (checkedId == btnPub.getId()) {
                   isPublic = true;
                   hint.setBackground(getResources().getDrawable(R.drawable.imgpublichint));
                   passcode.setVisibility(View.INVISIBLE);
                   passcodeHint.setVisibility(View.INVISIBLE);
               }
               else {
                   isPublic = false;
                   hint.setBackground(getResources().getDrawable(R.drawable.imgprivatehint));
                   passcode.setVisibility(View.VISIBLE);
                   passcodeHint.setVisibility(View.VISIBLE);
               }

               // RadioButton radbtn = (RadioButton) findViewById(checkedId);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfirmActivity.this, FinishActivity.class);
                intent.putExtra("isPublic", isPublic);
                intent.putExtra("selectedPhotoPaths", selectedPhotoPaths);
                startActivity(intent);
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
