package com.pwrobel.darkcam1;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pwrobel on 17.08.16.
 */

public class PhotoHandler implements Camera.PictureCallback {

    private final Context context;

    private StaticPhotoRenderBackend image_processor_;

    static String log_prefix = "darkcam debug log";
    public PhotoHandler(Context context, StaticPhotoRenderBackend image_processor) {
        this.context = context;
        this.image_processor_ = image_processor;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        Camera.Parameters parameters = camera.getParameters();
        int format = parameters.getPreviewFormat();

        this.image_processor_.setImgBuffer(data, ImageFormat.JPEG);
        this.image_processor_.processBuffer();

        int error_code = this.image_processor_.saveBufferToDisk();

        if(error_code == -1){
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }else if(error_code == 1){
            Toast.makeText(context, "New Image saved:",
                    Toast.LENGTH_LONG).show();
        };

        CamActivity act = (CamActivity) this.context;
        act.getProgressDialog().dismiss();

        /*File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(log_prefix, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "PQQQQ_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(context, "New Image saved:" + photoFile,
                    Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Log.d(log_prefix, "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
        */
        camera.stopPreview();
        camera.startPreview();

    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "YYY");
    }
}

