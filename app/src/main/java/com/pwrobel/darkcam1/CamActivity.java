package com.pwrobel.darkcam1;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import android.content.Context;

public class CamActivity extends ActionBarActivity {

    private CameraRenderer mRenderer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_cam);
        mRenderer = (CameraRenderer)findViewById(R.id.renderer_view);
        this.addListeners();
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

    private void addListeners(){

        Button buttonOne = (Button) findViewById(R.id.button);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                Context context = getApplicationContext();
                CharSequence text = "Picture order dispatched!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                CamActivity.this.mRenderer.requestBigPic();
            }
        });

    }
}
