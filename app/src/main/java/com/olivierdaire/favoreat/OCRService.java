package com.olivierdaire.favoreat;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * OCR Service process image in background and extract text
 *
 * @author Olivier Daire
 * @version 1.0
 * @since 03/05/16
 */
public class OCRService extends IntentService {
    private static final String TAG = OCRService.class.getName();
    public static final String RECOGNIZED_TEXT = "RECOGNIZED_TEXT";
    public static final String RESULT = "RESULT";
    public static final String RECEIVER = "com.olivierdaire.favoreat.android.service.receiver";
    private int result = Activity.RESULT_CANCELED;

    private static final String OCR_DATA_FOLDER = "tessdata";
    private static final String LANG = "fra";
    private static String ASSETS_FOLDER;
    public static final double SCALE_FACTOR = 0.2;

    public OCRService() {
        super("OCRService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ASSETS_FOLDER = getApplicationContext().getFilesDir().getPath() + File.separator;
        String filepath = intent.getStringExtra("FILEPATH");

        // Copy OCRService data to internal storage in order to be able to access it later
        String OCRDataFile = ASSETS_FOLDER + OCR_DATA_FOLDER + File.separator + LANG + ".traineddata";
        if (!new File(OCRDataFile).exists()) {
            copyFileOrDir(OCR_DATA_FOLDER);
        }

        Bitmap bitmap = createBitmapFromPicture(filepath);
        String recognizedText = recognizeText(bitmap);

        result = Activity.RESULT_OK;
        publishResults(recognizedText, result);
    }

    /**
     * Send recognized text when processing is done
     * @param text
     * @param result
     */
    private void publishResults(String text, int result) {
        Intent intent = new Intent();
        intent.setAction(RECEIVER);
        intent.putExtra(RECOGNIZED_TEXT, text);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }

    /**
     * Use Tesseract API to recognize text from a bitmap
     * @return String
     */
    public String recognizeText(Bitmap bitmap){
        TessBaseAPI baseApi = new TessBaseAPI();

        baseApi.init(ASSETS_FOLDER, LANG);
        baseApi.setImage(bitmap);
        String recognizedText = baseApi.getUTF8Text();
        baseApi.end();

        // Remove non alpha numeric characters
        recognizedText = recognizedText.replaceAll("[^a-zA-Z\\d\\s:]+", " ");

        // TODO improve recognition :
        // train tesseract https://medium.com/apegroup-texts/training-tesseract-for-labels-receipts-and-such-690f452e8f79
        // Test Google vision API

        return recognizedText;
    }


    /**
     * Create a bitmap from the picture path given by the camera
     * @param picturePath
     * @return bitmap
     */
    public Bitmap createBitmapFromPicture(String picturePath){
        File file = new File(picturePath);
        Uri photoUri = Uri.fromFile(file);
        Bitmap photo = null;

        try {
            photo = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), photoUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return prepareBitmapForOCR(photo, picturePath);
    }

    /**
     * Prepare a bitmap file for the OCRService : rotate if needed, convert to ARGB_8888, grey scale..
     * @param bitmap
     * @return bitmap
     */
    private static Bitmap prepareBitmapForOCR(Bitmap bitmap, String picturePath){
        bitmap = rotateBitmap(bitmap, picturePath);
        // Convert to ARGB_8888, required by Tesseract
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        // Scale image down
        bitmap = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*SCALE_FACTOR), (int)(bitmap.getHeight()*SCALE_FACTOR), true);
        bitmap = doGreyscale(bitmap);

        return bitmap;
    }

    /**
     * Rotate a bitmap given its original orientation
     * @param bitmap
     * @return bitmap
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, String picturePath){
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

    /**
     * Copy from the given path, directories or files to internal storage.
     * Allow access to files which are normally packed inside the APK and thus not readable.
     * @param path
     */
    private void copyFileOrDir(String path) {
        AssetManager assetManager = getApplicationContext().getAssets();
        String assets[];

        try {
            assets = assetManager.list(path);

            if (assets.length == 0) {
                // Only one file
                copyFile(path);
            } else {
                // Create dir and loop through its files
                String fullPath =  ASSETS_FOLDER + path;

                File dir = new File(fullPath);
                if (!dir.exists()) {
                    if (!dir.mkdirs()){
                        Log.i(TAG, "Could not create dir " + fullPath);
                    }
                }

                for (int i = 0; i < assets.length; ++i) {
                    String p;

                    if (path.equals("")) {
                        p = "";
                    } else {
                        p = path + "/";
                    }
                    copyFileOrDir(p + assets[i]);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "I/O Exception", e);
        }
    }

    /**
     * Copy a file from assets to internal storage
     * @param filename
     */
    private void copyFile(String filename) {
        AssetManager assetManager = getApplicationContext().getAssets();

        InputStream in;
        OutputStream out;
        String newFileName = null;

        try {
            in = assetManager.open(filename);
            newFileName = ASSETS_FOLDER + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception in copyFile() of " + newFileName + ":" + e.toString());
        }
    }
}
