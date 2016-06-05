package com.pwrobel.darkcam1;

/**
 * Created by pwrobel on 05.06.16.
 */

//when the user chooses to take the picture, this backend manages the processing and
//saving of the frame to the disk
public interface StaticPhotoRenderBackend {

    //initialize CPU renderer backend
    public void Init();

    //0 = backend is processing the frame, >0 - denotes some reason of why it's busy
    public int getBackendBusy();

    //set camera full resolution data
    public void setImgBuffer(int[] buff, int width, int height, int bpp);

    //apply shader effect in CPU on the buffer
    public int processBuffer();

    //save the processed buffer on the disk
    public int saveBufferToDisk();

}
