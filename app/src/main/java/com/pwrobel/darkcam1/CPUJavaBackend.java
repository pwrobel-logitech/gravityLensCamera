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
    private int[] RGB_buf;
    private int[] postprocessed_RGB_buf;
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
    public void setImgBuffer(byte[] buff, int type){
        this.imgtype = type;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true; //set bitmap to mutable - able to operate on its pixels
        Bitmap bitmap = BitmapFactory.decodeByteArray(buff, 0, buff.length, opt);
        this.preprocessed_bigimage = bitmap;
        Log.i("decode bmp info: ", "decW: "+bitmap.getWidth()+" decH: "+bitmap.getHeight());
        int w = this.preprocessed_bigimage.getWidth();
        int h = this.preprocessed_bigimage.getHeight();
        this.RGB_buf = new int[w * h];
        this.postprocessed_RGB_buf = new int[w * h];
    };

    //apply shader effect in CPU on the buffer
    public int processBuffer(){
        int w = this.preprocessed_bigimage.getWidth();
        int h = this.preprocessed_bigimage.getHeight();
        this.preprocessed_bigimage.getPixels(this.RGB_buf, 0, w, 0, 0, w, h);
        this.private_process_buff();
        this.preprocessed_bigimage.setPixels(this.postprocessed_RGB_buf, 0, w, 0, 0, w, h);
        return 1;
    };

    private void private_process_buff(){ //from the RGB_buf to postprocessed_RGB_buf
        int w = this.preprocessed_bigimage.getWidth();
        int h = this.preprocessed_bigimage.getHeight();
        for (int j = 0; j < h; j++)
            for (int i = 0; i < w; i++){
                int p = this.RGB_buf[i + w * j];
                p = p & 0xff00ffff;
                this.postprocessed_RGB_buf[i + w * j] = p;
            }

        for (int j = 0; j < h; j++)
            for (int i = 0; i < w; i++){
                this.process_pixel(i, j, w, h);
            }
    }

    private void process_pixel(int x, int y, int w, int h){
        double phys_ratio = this.mass;
        double fov_yx_ratio = ((double)h)/((double)w);
        double fovX = this.fovX * (Math.PI/180.0) ;
        double fovY = fov_yx_ratio * fovX;

        double xnorm = ((double)x)/((double)w);
        double ynorm = ((double)y)/((double)h);

        double tx = Math.tan((xnorm - 0.5) * fovX);
        double ty = Math.tan((ynorm - 0.5) * fovY);
        double fi = Math.atan(Math.sqrt(tx*tx+ty*ty));

        int final_col;

        if(fi < phys_ratio){
            final_col = 0x00000000;
        }else{
            double coeff = 1.0 - phys_ratio * (1.0 / (fi * fi));
            double xn = clamp(0.5 + coeff*(xnorm-0.5),0.0,1.0);
            double yn = clamp(0.5 + coeff*(ynorm-0.5),0.0,1.0);
            int readcoord = ((int)(xn*w)) + w * ((int)(yn*h));
            if(readcoord < 0)
                readcoord = 0;
            if(readcoord >= w*h)
                readcoord = w*h - 1;
            final_col = this.RGB_buf[readcoord];
        }
        this.postprocessed_RGB_buf[x + y * w] = final_col;
                /*
        pi = 3.141593;
        fovyxratio = fov_yx_ratio;
        fovx = fov_x_deg * (pi/180.0);
        fovy = fovyxratio * fovx;


        x = vTextureCoord.x;
        y = vTextureCoord.y;

        tx = tan((x-0.5)*fovx);
        ty = tan((y-0.5)*fovy);

        fi  = atan(sqrt(tx*tx+ty*ty));

        ratio = phys_ratio;

        if(fi < ratio){
            gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        }else{
            coeff = 1.0 - ratio * (1.0/(fi*fi));
            xn = clamp(0.5 + (x-0.5)*coeff,0.0,1.0);
            yn = clamp(0.5 + (y-0.5)*coeff,0.0,1.0);

            gl_FragColor =texture2D(sTexture, vec2(xn,yn));
        }*/
    }

    public static double clamp(double a, double low, double high){
        if(a < low)
            return low;
        if(a > high)
            return high;
        return a;
    }

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
            this.preprocessed_bigimage.compress(Bitmap.CompressFormat.JPEG, 100, bs);
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
