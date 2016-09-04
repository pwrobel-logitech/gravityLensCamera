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
        int format = parameters.getPreviewFormat();

        this.image_processor_.setImgBuffer(data, ImageFormat.JPEG);
        this.image_processor_.processBuffer();

        act.getProgressDialog().setMessage(act.getTextInCurrentLang("saving_img_to_sd")); //will not show..

        int error_code = this.image_processor_.saveBufferToDisk();

        if(error_code == -1){
            Toast.makeText(context, act.getTextInCurrentLang("could_not_save_img1"),
                    Toast.LENGTH_LONG).show();
        }else if(error_code == 1){
            Toast.makeText(context, act.getTextInCurrentLang("saved_img1"),
                    Toast.LENGTH_LONG).show();
        };

        act.getProgressDialog().dismiss();
        
        camera.stopPreview();
        camera.startPreview();

    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "YYY");
    }
}

