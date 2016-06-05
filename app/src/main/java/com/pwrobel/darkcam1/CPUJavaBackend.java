package com.pwrobel.darkcam1;

/**
 * Created by pwrobel on 05.06.16.
 */
public class CPUJavaBackend implements StaticPhotoRenderBackend {

    private int width;
    private int height;
    private int bpp;
    private int[] buf;

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
    public void setImgBuffer(int[] buff, int width, int height, int bpp){
        if(width*height*bpp != this.width*this.height*this.bpp)
            this.buf = new int[width*height*bpp];
        this.width = width;
        this.height = height;
        this.bpp = bpp;
        for (int j = 0; j < height; j++)
            for (int i = 0; i < width; i++){
                for (int k = 0; k < bpp; k++)
	                this.buf[bpp*(j*width + i) + k] = buff[bpp*(j*width + i) + k];
            }
    };

    //apply shader effect in CPU on the buffer
    public int processBuffer(){
        return 1;
    };

    //save the processed buffer on the disk
    public int saveBufferToDisk(){
        return 1;
    };

    //set info about the object, mass, distance and field of viev in x-direction - horizontal
    public void setBlackHoleInfo(double mass, double distance, double fovXdeg){
        this.mass = mass;
        this.distance = distance;
        this.fovX = fovXdeg;
    };

}
