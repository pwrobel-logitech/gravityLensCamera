package com.pwrobel.darkcam1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;


public class CameraRenderer extends GLSurfaceView implements
        GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener{
    private Context mContext;

    /**
     * Camera and SurfaceTexture
     */
    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture;
    private int camera_width;
    private int camera_height;

    //private final FBORenderTarget mRenderTarget = new FBORenderTarget();
    private final OESTexture mCameraTexture = new OESTexture();
    private final Shader mOffscreenShader = new Shader();
    private int mWidth, mHeight;
    private boolean updateTexture = false;

    /**
     * OpenGL params
     */
    private ByteBuffer mFullQuadVertices;
    private float[] mTransformM = new float[16];
    private float[] mOrientationM = new float[16];
    private float[] mRatio = new float[2];
    private float[] fov_yx_ratio = new float[1];
    private float[] fov_x_deg = new float[1];
    private float[] phys_ratio = new float[1];

    double fovYdeg;

    private StaticPhotoRenderBackend image_processor_;

    /**
     * Choose and init the backend for processing the big image to the final effect
     */
    private boolean init_image_processing_backend(){
        image_processor_ = new NativeCPUBackend();
        if(!image_processor_.testMe()){
            image_processor_ = new CPUJavaBackend();
            Log.i("darkcam:", "Fall back to java backend renderer.");
        }else{
            Log.i("darkcam:", "Using native renderer for big pictures.");
        }
        image_processor_.Init();
        return true;
    }

    public CameraRenderer(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CameraRenderer(Context context, AttributeSet attrs){
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init(){

        this.phys_ratio[0] = (float)default_phys_ratio;
        if(!init_image_processing_backend()){
            Log.i("Darkcam_processor", "failed to init CPU backend ");
        };

        //Create full scene quad buffer
        final byte FULL_QUAD_COORDS[] = {-1, 1, -1, -1, 1, 1, 1, -1};
        mFullQuadVertices = ByteBuffer.allocateDirect(4 * 2);
        mFullQuadVertices.put(FULL_QUAD_COORDS).position(0);

        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private static double default_phys_ratio = 0.007f;  // (4*G*M)/(l*c^2) - l:blackhole-observer distance=1m, M= 2.36e24kg
    private double scale_factor;

    public double getDefault_phys_ratio(){
        return default_phys_ratio;
    }

    public void updateBlackHoleScale(double scale_factor){
        this.phys_ratio[0] = (float)(scale_factor * default_phys_ratio);
        this.scale_factor = scale_factor;
        this.updateTexture = true;
        if(this.image_processor_ != null){
            this.image_processor_.setBlackHoleInfo(this.phys_ratio[0], 1.0, this.fov_x_deg[0], this.fovYdeg);
        }
    }

    public String getBlackHolesStorageDir(){
        if(this.image_processor_ != null){
            String dir = this.image_processor_.getDir().toString();
            return dir;
        }
        return "";
    }

    private float data_fov1=0, data_fov2=0;
    public float getDataFov1(){
        return data_fov1;
    }

    public float getDataFov2(){
        return data_fov2;
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture){
        updateTexture = true;
        requestRender();
    }


    @Override
    public synchronized void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //load and compile shader

        try {
            mOffscreenShader.setProgram(R.raw.vsh, R.raw.fsh, mContext);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @SuppressLint("NewApi")
    @Override
    public synchronized void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight= height;

        //generate camera texture------------------------
        mCameraTexture.init();

        //set up surfacetexture------------------
        SurfaceTexture oldSurfaceTexture = mSurfaceTexture;
        mSurfaceTexture = new SurfaceTexture(mCameraTexture.getTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        if(oldSurfaceTexture != null){
            oldSurfaceTexture.release();
        }


        //set camera para-----------------------------------
        camera_width =0;
        camera_height =0;

        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }

        try{
            mCamera = Camera.open();
        }catch(Exception e){
            Log.i("darkcam:", "failed to open camera");
            return;
        }

        try{
            mCamera.setPreviewTexture(mSurfaceTexture);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        Camera.Parameters param = mCamera.getParameters();
        List<Size> psize = param.getSupportedPreviewSizes();
        if(psize.size() > 0 ){
            int i;
            for (i = 0; i < psize.size(); i++){
                if(psize.get(i).height == 1280 || psize.get(i).width == 1280)
                    break;
            }
            if(i==psize.size())
                for (i = 0; i < psize.size(); i++){
                    if((psize.get(i).height > 800 && psize.get(i).height < 1300)
                            || (psize.get(i).width > 800 && psize.get(i).width < 1300))
                        break;
                }
            if(i==psize.size())
                i = psize.size() - 2;

            if(i<0)
                i=0;
//i=9;
            param.setPreviewSize(psize.get(i).width, psize.get(i).height);

            camera_width = psize.get(i).width;
            camera_height= psize.get(i).height;

        }

        //Log.i("Dcam_prewiev_set_size", "width: " + String.valueOf(camera_width) + ", height: " + String.valueOf(camera_height));

        //setup the fovX (the smaller one) angle in degrees
        double thetaV = (param.getVerticalViewAngle());
        double thetaH = (param.getHorizontalViewAngle());

        this.data_fov1 = (float)thetaH;
        this.data_fov2 = (float)thetaV;

        Log.i("DcamFOV", "Vangle: " + String.valueOf(thetaV) + ", Hangle: " + String.valueOf(thetaH));
        if(thetaV < 10.0)
            thetaV = 10.0;
        if(thetaV > 150.0)
            thetaV = 150.0;

        if(thetaH < 10.0)
            thetaH = 10.0;
        if(thetaH > 150.0)
            thetaH = 150.0;

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);

        float rot_angle = 0.0f;
       // if(camera_width > camera_height) {
            rot_angle = info.orientation;

            float a=0,b=0; //rotated parameters
            if(info.orientation % 180 == 0){
                a = camera_height;
                b = camera_width;
            }else{
                a = camera_width;
                b = camera_height;
            }

            if( ((float)a)/((float)b) < ((float)height)/((float)width) ){
                //camera preview more square than surface to present
                mRatio[0] = ((((float)b)*((float)height))/(((float)a)*((float)width))); //multiply by q>1
                mRatio[1] = 1.0f;

                fovYdeg = (float) thetaH;
                this.fov_x_deg[0] = (float)((180.0/Math.PI) * ((float) Math.atan(Math.tan(Math.PI*thetaV/180.0)/mRatio[0])));
                //Log.i("DcamFOV2.c1", "setXfov: " + this.fov_x_deg[0] );
            }else{
                mRatio[0] = 1.0f;
                mRatio[1] = (((float)a)/((float)b))/(((float)height)/((float)width)); //multiply by the same(inv) q < 1
                this.fov_x_deg[0] = (float) thetaV;
                fovYdeg = (180.0/Math.PI) * ((float) Math.atan(Math.tan(Math.PI*thetaH/180.0)/mRatio[1]));
                //Log.i("DcamFOV2.c2", "setXfov: " + this.fov_x_deg[0] );
            }
            fov_yx_ratio[0] = ((float)b)/((float)a);
            //default_phys_ratio = 0.007 * (this.fov_x_deg[0]/36.1);
       /* }else{
            rot_angle = 0.0f;
            this.fov_x_deg[0] = (float)thetaH;
            fovYdeg = (float) thetaV;
            if( ((float)camera_height)/((float)camera_width) < ((float)height)/((float)width) ){
                //camera preview more square than surface to present
                mRatio[0] = ((((float)camera_width)*((float)height))/(((float)camera_height)*((float)width)));
                mRatio[1] = 1.0f;
            }else{
                mRatio[0] = 1.0f;
                mRatio[1] = (((float)camera_height)/((float)camera_width))/(((float)height)/((float)width));
            }
            fov_yx_ratio[0] = ((float)camera_width)/((float)camera_height);
        }*/

        Matrix.setRotateM(mOrientationM, 0, rot_angle, 0f, 0f, 1f);

        //set physical ratio parameters containing the "normalized" black hole mass
        //this.phys_ratio[0] = (float)default_phys_ratio;
        this.image_processor_.setBlackHoleInfo(this.phys_ratio[0], 1.0, thetaV, thetaH);
        //start camera-----------------------------------------
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        if(mCamera != null){
            mCamera.setParameters(param);
            mCamera.startPreview();
        }

        //start render---------------------
        requestRender();
    }

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //render the texture to FBO if new frame is available
        if(updateTexture && (mCamera != null)){
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mTransformM);

            updateTexture = false;

            GLES20.glViewport(0, 0, mWidth, mHeight);

            mOffscreenShader.useProgram();

            int uTransformM = mOffscreenShader.getHandle("uTransformM");
            int uOrientationM = mOffscreenShader.getHandle("uOrientationM");
            int uRatioV = mOffscreenShader.getHandle("ratios");
            int uFovRatio = mOffscreenShader.getHandle("fov_yx_ratio");
            int uFovDeg = mOffscreenShader.getHandle("fov_x_deg");
            int uPhysRatio = mOffscreenShader.getHandle("phys_ratio");

            GLES20.glUniformMatrix4fv(uTransformM, 1, false, mTransformM, 0);
            GLES20.glUniformMatrix4fv(uOrientationM, 1, false, mOrientationM, 0);
            GLES20.glUniform2fv(uRatioV, 1, mRatio, 0);
            GLES20.glUniform1fv(uFovRatio, 1, fov_yx_ratio, 0);
            GLES20.glUniform1fv(uFovDeg, 1, fov_x_deg, 0);
            GLES20.glUniform1fv(uPhysRatio, 1, phys_ratio, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCameraTexture.getTextureId());

            renderQuad(mOffscreenShader.getHandle("aPosition"));
        }



    }

    public void
    requestBigPic(){
        if(mCamera == null)
            return;
        Camera.Parameters param;
        param = mCamera.getParameters();

        param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPictureSizes();
        bestSize = sizeList.get(0);
        //Log.i("dark cam", "Take picture, supported size, rawwidth: "+bestSize.width+", rawheight: " + bestSize.height);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
                //Log.i("dark cam", "Take picture, supported size, rawwidth: "+bestSize.width+", rawheight: " + bestSize.height);
            }
        }
        param.setPictureSize(bestSize.width, bestSize.height);

        double thetaV = (param.getVerticalViewAngle());
        double thetaH = (param.getHorizontalViewAngle());
        //mCamera.setParameters(param);

        //Log.i("QQQQQDcamFOVBig", "Vangle: "+String.valueOf(thetaV)+", Hangle: "+String.valueOf(thetaH));

        /* choose the maximum aera size for the preview - currently unused
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }

        List<Integer> supportedPreviewFormats = param.getSupportedPreviewFormats();
        Iterator<Integer> supportedPreviewFormatsIterator = supportedPreviewFormats.iterator();
        while(supportedPreviewFormatsIterator.hasNext()){
            Integer previewFormat =supportedPreviewFormatsIterator.next();
            if (previewFormat == ImageFormat.YV12) {
                param.setPreviewFormat(previewFormat);
            }
        }

        param.setPreviewSize(bestSize.width, bestSize.height);
        param.setPictureSize(bestSize.width, bestSize.height);
        */

        //if(bestSize.width > bestSize.height)
        //    param.setRotation(90);

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);
        ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);


        int rotation = ((CamActivity)this.mContext).getWindowManager().getDefaultDisplay()
                .getRotation();

        ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        //mCamera.setDisplayOrientation(result);
        param.setRotation(result);

        param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        if(mCamera == null)
            return;
        mCamera.setParameters(param);

        mCamera.takePicture(null, null, new PhotoHandler(mContext, this.image_processor_));
    }

    private void renderQuad(int aPosition){
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0, mFullQuadVertices);
        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onDestroy(){
        updateTexture = false;
        if(mSurfaceTexture != null)
            mSurfaceTexture.release();
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
        }

        mCamera = null;
    }

}
