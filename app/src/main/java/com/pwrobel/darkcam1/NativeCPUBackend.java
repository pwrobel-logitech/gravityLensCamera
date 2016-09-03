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

    private native double internal_numerical_test(double in);

    private native void process_buffer(int []in, int []out, int buff_w, int buff_h,
                                       double phys_ratio, double fovX_deg, int numThr);

}
