package com.olivierdaire.favoreat;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description
 *
 * @author Olivier Daire
 * @version 1.0
 * @since 03/05/16
 */
public class OCR {
    private static final String TAG = OCR.class.getName();
    private static final String OCR_DATA_FOLDER = "tessdata";
    private static String ASSETS_FOLDER;
    private final Context context;
    private final Bitmap bitmap;
    private final String lang;

    public OCR(Bitmap bitmap, String lang, Context context) {
        this.bitmap = bitmap;
        this.lang = lang;
        this.context = context;
        ASSETS_FOLDER = context.getFilesDir().getPath() + File.separator;

        // Copy OCR data to internal storage in order to be able to access it later
        String OCRDataFile = ASSETS_FOLDER + OCR_DATA_FOLDER + File.separator + lang + ".traineddata";

        if (!new File(OCRDataFile).exists()) {
            copyFileOrDir(OCR_DATA_FOLDER);
        }

    }

    /**
     * Copy from the given path, directories or files to internal storage.
     * Allow access to files which are normally packed inside the APK and thus not readable.
     * @param path
     */
    private void copyFileOrDir(String path) {
        AssetManager assetManager = context.getAssets();
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
        AssetManager assetManager = context.getAssets();

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

    /**
     * Use Tesseract API to recognize text from a bitmap
     * @return String
     */
    public String recognizeText(){
        TessBaseAPI baseApi = new TessBaseAPI();

        baseApi.init(ASSETS_FOLDER, lang);
        baseApi.setImage(bitmap);
        String recognizedText = baseApi.getUTF8Text();
        baseApi.end();

        return recognizedText;
    }
}
