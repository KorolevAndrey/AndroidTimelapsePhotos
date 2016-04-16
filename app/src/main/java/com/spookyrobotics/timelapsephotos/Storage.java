package com.spookyrobotics.timelapsephotos;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Storage {
    private static final String TAG = "Storage";
    private final File mRoot;

    public Storage(File root) {
        mRoot = root;
        if(!mRoot.exists() && !mRoot.mkdir()){
            throw new IllegalAccessError("Couldn't create picture storage root");
        }
    }

    public void saveFile(final Context context, final String fileName, final byte[] data){
        File file = new File(mRoot, fileName);
        try {
            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            OutputStream os = new FileOutputStream(file);
            os.write(data);
            os.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(context,
                    new String[] { file.toString() }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        }

    }


}
