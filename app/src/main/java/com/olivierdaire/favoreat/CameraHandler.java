package com.olivierdaire.favoreat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handle Camera logic : take a picture, create an image, convert to Bitmap
 *
 * @author Olivier Daire
 * @version 1.1
 * @since 29/04/16
 */
public class CameraHandler {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static String picturePath;

    /**
     * Call to the phone camera to take a picture
     * @param context
     */
    public static void takePictureIntent(Context context){
        PackageManager pm = context.getPackageManager();
        boolean hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);

        if (hasCamera){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                File photo = null;

                try {
                    photo = createImageFile(context);
                } catch (IOException e){
                    Toast.makeText(context,"Failed to get the picture, please retry",Toast.LENGTH_SHORT).show();
                }

                if (photo !=null){
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                    ((Activity)context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        } else {
            Toast.makeText(context,"Your device has no camera please use the direct input solution",    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create a file (JPG) from the picture taken with the camera
     * @param context
     * @return File
     * @throws IOException
     */
    private static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Favoreat_" + timeStamp;
        File storageDir = context.getExternalFilesDir(null);
        // FIXME try to save image in context.getCacheDir(); without bug

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Store photoPath so we can retrieve it later
        picturePath = image.getAbsolutePath();
        return image;
    }


}
