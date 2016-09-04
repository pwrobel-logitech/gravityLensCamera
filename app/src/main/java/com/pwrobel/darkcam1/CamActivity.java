package com.pwrobel.darkcam1;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import android.content.Context;

import java.util.List;
import java.util.Set;

public class CamActivity extends ActionBarActivity {

    private CameraRenderer mRenderer;

    private ProgressDialog progress;
    private Set<String> languages;

    public ProgressDialog getProgressDialog(){
        return this.progress;
    }

    public Set<String> getLanguages(){
        return this.languages;
    }

    public void addLanguage(String lang){
        if(!this.languages.contains(lang)){
            this.languages.add(lang);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this.addLanguage("en");
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_cam);
        mRenderer = (CameraRenderer)findViewById(R.id.renderer_view);
        this.addListeners();
    }

    @Override
    public void onStart(){
        Log.i("darkcam activity", "Activity onStart");
        super.onStart();
        Log.i("darkcam activity", "Activity onStart finished");
    }


    @Override
    public void onPause(){
        Log.i("darkcam activity", "Activity onPause");
        super.onPause();
        if(mRenderer != null)
            mRenderer.onDestroy();
        Log.i("darkcam activity", "Activity onPause finished");
    }

    @Override
    public void onResume(){
        Log.i("darkcam activity", "Activity onResume");
        super.onResume();
        mRenderer.onResume();
        Log.i("darkcam activity", "Activity onResume finished");
    }

    private void addListeners(){

        Button buttonOne = (Button) findViewById(R.id.button);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                Context context = getApplicationContext();
                //CharSequence text = "Picture order dispatched!";
                //int duration = Toast.LENGTH_SHORT;

                //Toast toast = Toast.makeText(context, text, duration);
                //toast.show();

                final ProgressDialog progress = new ProgressDialog(CamActivity.this);
                CamActivity.this.progress = progress;
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CamActivity.this.mRenderer.requestBigPic();
                    }
                }).start();//fire and forget, do not wait for the completion here, like in run()

            }
        });

    }
}
