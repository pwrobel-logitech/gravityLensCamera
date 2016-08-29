package com.pwrobel.darkcam1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
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
    private int imgtype; /*ex. ImageType.JPEG*/

    private Bitmap preprocessed_bigimage;

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
    public void setImgBuffer(byte[] buff, int width, int height, int bpp, int type){
        this.imgtype = type;
            BitmapFactory.Options opt = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeByteArray(buff, 0, buff.length, opt);
            Log.i("decode bmp info: ", "decW: "+bitmap.getWidth()+" decH: "+bitmap.getHeight());
            this.buf = buff;
            this.preprocessed_bigimage = bitmap;
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
            final BufferedOutputStream bs = new BufferedOutputStream(fos, 1024 * 1024 * 16);
            this.preprocessed_bigimage.compress(Bitmap.CompressFormat.JPEG, 3, bs);
            bs.flush();
            bs.close();
            fos.close();
            //fos.write(this.buf);
            //fos.close();
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
