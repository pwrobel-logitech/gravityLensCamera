package com.pwrobel.darkcam1;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class CamActivity extends ActionBarActivity {

    private CameraRenderer mRenderer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_cam);
        mRenderer = (CameraRenderer)findViewById(R.id.renderer_view);
    }

    @Override
    public void onStart(){
        super.onStart();

    }


    @Override
    public void onPause(){
        super.onPause();
        //mRenderer.onDestroy();

    }

    @Override
    public void onResume(){
        super.onResume();
        mRenderer.onResume();
    }
}
