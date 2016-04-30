package com.olivierdaire.favoreat;

import android.content.Intent;
import android.graphics.Bitmap;
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
                CameraHandler.takePictureIntent(SelectorActivity.this);
                // Then see onActivityResult which handle the process of getting the data back
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CameraHandler.REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                // Create new file using photo we just took
                Bitmap photo = CameraHandler.createBitmapFromPicture(this, CameraHandler.picturePath);

                // TODO Extract data from photo
            }
        }
    }
}
