package com.pwrobel.darkcam1;

import android.graphics.ImageFormat;

/**
 * Created by pwrobel on 05.06.16.
 */

//when the user chooses to take the picture, this backend manages the processing and
//saving of the frame to the disk
public interface StaticPhotoRenderBackend {

    //initialize CPU renderer backend
    public void Init();

    /*
     *set camera full resolution data
     * if data_type is JPG, then width, height , bpp are not necessary
     */
    public void setImgBuffer(byte[] buff, /*ImageFormat constants*/int data_type);

    //apply shader effect in CPU on the buffer
    public int processBuffer();

    //save the processed buffer on the disk
    public int saveBufferToDisk();

    //inform about the last picture name with path saved to the disk.
    public String getLast_file_saved();

    //set info about the object, mass, distance and field of viev in x-direction - horizontal
    //two angles are used as an check control - one of that would be still used finally, based on the image size
    public void setBlackHoleInfo(double mass, double distance, double fovXdeg, double fovYdeg);

    //test backend, tell whether it is functional on a sample process
    public boolean testMe();
}
