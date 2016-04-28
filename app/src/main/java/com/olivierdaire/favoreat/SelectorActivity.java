package com.olivierdaire.favoreat;

import android.content.Intent;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SelectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);

        PercentRelativeLayout cameraSelector = (PercentRelativeLayout) findViewById(R.id.cameraSelector);
        cameraSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME Uncomment when camera activity is created
                //Intent intent = new Intent(SelectorActivity.this, CameraActivity.class);
                //startActivity(intent);
            }
        });

        PercentRelativeLayout inputSelector = (PercentRelativeLayout) findViewById(R.id.inputSelector);
        inputSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME Uncomment when the form is ready
                //Intent intent = new Intent(SelectorActivity.this, FormActivity.class);
                //startActivity(intent);
            }
        });
    }
}
