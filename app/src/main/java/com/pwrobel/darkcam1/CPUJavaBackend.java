package com.pwrobel.darkcam1;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pwrobel on 05.06.16.
 */
public class CPUJavaBackend implements StaticPhotoRenderBackend {

    private int width;
    private int height;
    private int bpp;
    private byte[] buf;

    String log_prefix = "darkcam";

    //physical parameters
    double mass;//multiple of the mass of earth
    double distance; //in meters from the observer
    double fovX; //in degrees along the horizontal axis

    //initialize CPU renderer backend
    public void Init(){

    };

    //0 = backend is processing the frame, >0 - denotes some reason of why it's busy
    public int getBackendBusy(){
        return 0;
    };

    //set camera full resolution data
    public void setImgBuffer(byte[] buff, int width, int height, int bpp, ImgDataType type){
        if(type != ImgDataType.JPG) {
            if (width * height * bpp != this.width * this.height * this.bpp)
                this.buf = new byte[width * height * bpp];
            this.width = width;
            this.height = height;
            this.bpp = bpp;
            for (int j = 0; j < height; j++)
                for (int i = 0; i < width; i++) {
                    for (int k = 0; k < bpp; k++)
                        this.buf[bpp * (j * width + i) + k] = buff[bpp * (j * width + i) + k];
                }
        }else{
            this.buf = buff;
        }
    };

    //apply shader effect in CPU on the buffer
    public int processBuffer(){
        return 1;
    };

    //save the processed buffer on the disk
    public int saveBufferToDisk(){

        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(log_prefix, "Can't create directory to save image.");
            return -1;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "PQQQQ_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(this.buf);
            fos.close();
            //Toast.makeText(context, "New Image saved:" + photoFile,
            //        Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Log.d(log_prefix, "File" + filename + "not saved: "
                    + error.getMessage());
            return -1;
            //Toast.makeText(context, "Image could not be saved.",
            //        Toast.LENGTH_LONG).show();
        }
        return 1;
    };

    //set info about the object, mass, distance and field of viev in x-direction - horizontal
    public void setBlackHoleInfo(double mass, double distance, double fovXdeg){
        this.mass = mass;
        this.distance = distance;
        this.fovX = fovXdeg;
    };



    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "YYY");
    }
}
