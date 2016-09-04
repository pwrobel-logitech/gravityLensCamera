package com.pwrobel.darkcam1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by pwrobel on 05.06.16.
 */
public class CPUJavaBackend implements StaticPhotoRenderBackend {

    private int width;
    private int height;
    private int bpp;
    protected int[] RGB_buf;
    protected int[] postprocessed_RGB_buf;
    private int imgtype; /*ex. ImageType.JPEG*/

    protected Bitmap preprocessed_bigimage;

    String log_prefix = "darkcam";

    //physical parameters
    double mass;//multiple of the mass of earth
    double distance; //in meters from the observer
    double fovX; //in degrees along the horizontal axis
    protected double phys_ratio;


    //initialize CPU renderer backend
    public void Init(){

    };

    //Java backend is always capable of processing
    public boolean testMe(){
        return true;
    }

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

    private void private_process_buff() { //from the RGB_buf to postprocessed_RGB_buf
        int numCPUs = this.getNumberOfCores();
        int numThreads = numCPUs;
        if(numThreads <= 0) //support 1, 2 or 4 threads
            numThreads = 1;
        if(numThreads == 3)
            numThreads = 2;
        if(numThreads > 4)
            numThreads = 4;

        if(numThreads == 1){
            this.process_on_1_thread();
        }else if(numThreads == 2){
            this.process_on_2_threads();
        }else if(numThreads == 4){
            this.process_on_4_threads();
        }else{
            this.process_on_1_thread();
        }
    }

    private void process_on_1_thread(){
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int w = CPUJavaBackend.this.preprocessed_bigimage.getWidth();
                int h = CPUJavaBackend.this.preprocessed_bigimage.getHeight();
                for (int j = 0; j < h; j++)
                    for (int i = 0; i < w; i++){
                        process_pixel(i, j, w, h);
                    }
            }
        });
        t1.run();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("darkcam: ", "Failed to join the Big image processor thread.");
        }
    }

    private void process_on_2_threads(){
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int w = CPUJavaBackend.this.preprocessed_bigimage.getWidth();
                int h = CPUJavaBackend.this.preprocessed_bigimage.getHeight();
                for (int j = 0; j < h/2; j++)
                    for (int i = 0; i < w; i++){
                        process_pixel(i, j, w, h);
                    }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int w = CPUJavaBackend.this.preprocessed_bigimage.getWidth();
                int h = CPUJavaBackend.this.preprocessed_bigimage.getHeight();
                for (int j = h/2; j < h; j++)
                    for (int i = 0; i < w; i++){
                        process_pixel(i, j, w, h);
                    }
            }
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("darkcam: ", "Failed to join the Big image processor 2 threads.");
        }
    }

    private void process_on_4_threads(){
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int w = CPUJavaBackend.this.preprocessed_bigimage.getWidth();
                int h = CPUJavaBackend.this.preprocessed_bigimage.getHeight();
                for (int j = 0; j < h/2; j++)
                    for (int i = 0; i < w/2; i++){
                        process_pixel(i, j, w, h);
                    }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int w = CPUJavaBackend.this.preprocessed_bigimage.getWidth();
                int h = CPUJavaBackend.this.preprocessed_bigimage.getHeight();
                for (int j = h/2; j < h; j++)
                    for (int i = 0; i < w/2; i++){
                        process_pixel(i, j, w, h);
                    }
            }
        });
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                int w = CPUJavaBackend.this.preprocessed_bigimage.getWidth();
                int h = CPUJavaBackend.this.preprocessed_bigimage.getHeight();
                for (int j = 0; j < h/2; j++)
                    for (int i = w/2; i < w; i++){
                        process_pixel(i, j, w, h);
                    }
            }
        });
        Thread t4 = new Thread(new Runnable() {
            @Override
            public void run() {
                int w = CPUJavaBackend.this.preprocessed_bigimage.getWidth();
                int h = CPUJavaBackend.this.preprocessed_bigimage.getHeight();
                for (int j = h/2; j < h; j++)
                    for (int i = w/2; i < w; i++){
                        process_pixel(i, j, w, h);
                    }
            }
        });
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("darkcam: ", "Failed to join the Big image processor 4 threads.");
        }
    }

    private void process_pixel(int x, int y, int w, int h){ //imitate pixel shader in software
        double phys_ratio = this.phys_ratio;
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
        String photoFile = "BlackHole_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            int size = 256 + this.preprocessed_bigimage.getWidth() * this.preprocessed_bigimage.getHeight();
            final BufferedOutputStream bs = new BufferedOutputStream(fos, size);
            this.preprocessed_bigimage.compress(Bitmap.CompressFormat.JPEG, 90, bs);
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
        this.phys_ratio = mass;
    };



    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "BlackHoles");
    }


    protected int getNumberOfCores() {
        if(Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        }
        else {
            // Use saurabh64's answer
            return getNumCoresOldPhones();
        }
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    private int getNumCoresOldPhones() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

}
