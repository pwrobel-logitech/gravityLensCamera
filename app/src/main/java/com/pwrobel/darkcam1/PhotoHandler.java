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
        CamActivity act = (CamActivity) this.context;
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
        int format = parameters.getPreviewFormat();

        this.image_processor_.setImgBuffer(data, ImageFormat.JPEG);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final CamActivity act = (CamActivity) PhotoHandler.this.context;

                image_processor_.processBuffer();

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        act.getProgressDialog().setMessage(act.getTextInCurrentLang("saving_img_to_sd")); //will not show..
                    }
                });

                final int error_code = image_processor_.saveBufferToDisk();

                if(act.getProgressDialog() != null)
                    act.getProgressDialog().dismiss();

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CamActivity act = (CamActivity) PhotoHandler.this.context;
                        act.enableButtons();
                        if(error_code == -1){
                            Toast.makeText(context, act.getTextInCurrentLang("could_not_save_img1"),
                                    Toast.LENGTH_LONG).show();
                        }else if(error_code == 1) {
                            Toast.makeText(context, act.getTextInCurrentLang("saved_img1"),
                                    Toast.LENGTH_LONG).show();
                            }
                    }
                });

            };


        }).start();

        camera.stopPreview();
        camera.startPreview();

    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "YYY");
    }
}

