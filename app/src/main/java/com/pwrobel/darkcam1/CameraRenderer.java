package com.pwrobel.darkcam1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;



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
        //Create full scene quad buffer
        final byte FULL_QUAD_COORDS[] = {-1, 1, -1, -1, 1, 1, 1, -1};
        mFullQuadVertices = ByteBuffer.allocateDirect(4 * 2);
        mFullQuadVertices.put(FULL_QUAD_COORDS).position(0);

        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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
            mCamera.release();
            mCamera = null;
        }

        mCamera = Camera.open();
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
                i = psize.size() - 2;

            param.setPreviewSize(psize.get(i).width, psize.get(i).height);

            camera_width = psize.get(i).width;
            camera_height= psize.get(i).height;

        }

        float rot_angle = 0.0f;
        if(camera_width > camera_height) {
            rot_angle = 90.0f;
            if( ((float)camera_width)/((float)camera_height) < ((float)height)/((float)width) ){
                //camera preview more square than surface to present
                mRatio[0] = ((((float)camera_height)*((float)height))/(((float)camera_width)*((float)width)));
                mRatio[1] = 1.0f;
            }else{
                mRatio[0] = 1.0f;
                mRatio[1] = (((float)camera_width)/((float)camera_height))/(((float)height)/((float)width));
            }
            fov_yx_ratio[0] = ((float)camera_height)/((float)camera_width);
        }else{
            rot_angle = 0.0f;
            if( ((float)camera_height)/((float)camera_width) < ((float)height)/((float)width) ){
                //camera preview more square than surface to present
                mRatio[0] = ((((float)camera_width)*((float)height))/(((float)camera_height)*((float)width)));
                mRatio[1] = 1.0f;
            }else{
                mRatio[0] = 1.0f;
                mRatio[1] = (((float)camera_height)/((float)camera_width))/(((float)height)/((float)width));
            }
            fov_yx_ratio[0] = ((float)camera_width)/((float)camera_height);
        }

        Matrix.setRotateM(mOrientationM, 0, rot_angle, 0f, 0f, 1f);

        //setup the fovX (the smaller one) angle in degrees
        this.fov_x_deg[0] = 30.0f;

        //start camera-----------------------------------------
        mCamera.setParameters(param);
        mCamera.startPreview();

        //start render---------------------
        requestRender();
    }

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //render the texture to FBO if new frame is available
        if(updateTexture){
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

            GLES20.glUniformMatrix4fv(uTransformM, 1, false, mTransformM, 0);
            GLES20.glUniformMatrix4fv(uOrientationM, 1, false, mOrientationM, 0);
            GLES20.glUniform2fv(uRatioV, 1, mRatio, 0);
            GLES20.glUniform1fv(uFovRatio, 1, fov_yx_ratio, 0);
            GLES20.glUniform1fv(uFovDeg, 1, fov_x_deg, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCameraTexture.getTextureId());

            renderQuad(mOffscreenShader.getHandle("aPosition"));
        }

    }

    private void renderQuad(int aPosition){
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0, mFullQuadVertices);
        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onDestroy(){
        updateTexture = false;
        mSurfaceTexture.release();
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
        }

        mCamera = null;
    }

}
