package com.pwrobel.darkcam1;

import android.util.Log;

/**
 * Created by pwrobel on 03.09.16.
 */
public class NativeCPUBackend extends CPUJavaBackend {

    static {
        try {
            System.loadLibrary("backend_native");
        } catch (UnsatisfiedLinkError e) {
            Log.i("darkcam", "Unable to load native backend library");
        }
    }

    NativeCPUBackend(){
        super();
    }

    @Override
    public boolean testMe(){
        double obtained_from_backend = internal_numerical_test(Math.PI/4.0);
        double java_obtained = Math.sin(Math.PI/4.0)+Math.PI;
        return Math.abs((obtained_from_backend - java_obtained)/java_obtained) < 1e-5;
    }


    //apply shader effect in CPU native backend on the buffer
    @Override
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
        double phys_ratio = this.phys_ratio;
        this.process_buffer(this.RGB_buf, this.postprocessed_RGB_buf, this.preprocessed_bigimage.getWidth(),
                this.preprocessed_bigimage.getHeight(), phys_ratio, this.fovX, numThreads);
    }

    private native double internal_numerical_test(double in);

    private native void process_buffer(int []in, int []out, int buff_w, int buff_h,
                                       double phys_ratio, double fovX_deg, int numThr);

}
