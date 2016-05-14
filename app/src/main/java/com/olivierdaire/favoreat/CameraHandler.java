package com.olivierdaire.favoreat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
    public static final double SCALE_FACTOR = 0.2;
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
            Toast.makeText(context,"Your device has no camera please use the direct input solution",Toast.LENGTH_SHORT).show();
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

    public static Bitmap createBitmapFromPicture(Context context, String picturePath){
        File file = new File(picturePath);
        Uri photoUri = Uri.fromFile(file);
        Bitmap photo = null;

        try {
            photo = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return prepareBitmapForOCR(photo);
    }

    /**
     * Prepare a bitmap file for the OCR : rotate if needed, convert to ARGB_8888, grey scale..
     * @param bitmap
     * @return bitmap
     */
    private static Bitmap prepareBitmapForOCR(Bitmap bitmap){
        bitmap = rotateBitmap(bitmap);
        // Convert to ARGB_8888, required by Tesseract
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        // Scale image down
        bitmap = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*SCALE_FACTOR), (int)(bitmap.getHeight()*SCALE_FACTOR), true);
        // TODO test contrast efficiency
        //bitmap = createContrast(bitmap, 20);
        bitmap = doGreyscale(bitmap);

        return bitmap;
    }

    /**
     * Rotate a bitmap given its original orientation
     * @param bitmap
     * @return bitmap
     */
    private static Bitmap rotateBitmap(Bitmap bitmap){
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(picturePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        int rotate = 0;

        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
        }

        if (rotate != 0) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            // Setting pre rotate
            Matrix mtx = new Matrix();
            mtx.preRotate(rotate);

            // Rotating Bitmap
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
        }

        return bitmap;
    }

    /**
     * Create a grey level image from a bitmap
     * @param src
     * @return bitmap
     */
    private static Bitmap doGreyscale(Bitmap src) {
        // constant factors
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;

        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // pixel information
        int A, R, G, B;
        int pixel;

        // get image size
        int width = src.getWidth();
        int height = src.getHeight();

        // scan through every single pixel
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get one pixel color
                pixel = src.getPixel(x, y);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // take conversion up to one single value
                R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    private static Bitmap createContrast(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.red(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.red(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }
}
