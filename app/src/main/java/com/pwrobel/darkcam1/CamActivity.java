package com.pwrobel.darkcam1;

//http://simpleicon.com/wp-content/uploads/gear-1.png - gear icon - probably free licence

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CamActivity extends ActionBarActivity {

    private CameraRenderer mRenderer;

    private ProgressDialog progress;
    private Set<String> languages;
    private String current_lang;

    public ProgressDialog getProgressDialog(){
        return this.progress;
    }

    public String getCurrentLang(){
        return current_lang;
    }

    public void setCurrentLang(String lang){
        if(this.languages.contains(lang)){
            this.current_lang = lang;
        }
    }

    public Set<String> getLanguages(){
        return this.languages;
    }

    public void addLanguage(String lang){
        if(!this.languages.contains(lang)){
            this.languages.add(lang);
        }
    }

    private String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }

    public String getTextInCurrentLang(String text){
        return (this.getStringResourceByName(this.current_lang + "_" + text));
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.i("darkcam activity", "Activity onStart");
        super.onCreate(savedInstanceState);
        Log.i("darkcam activity", "Activity onStart finished");
    }

    @Override
    public void onStart(){

        this.languages = new HashSet<String>();

        this.addLanguage("en");
        this.addLanguage("pl");
        this.setCurrentLang("pl");
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cam);

        this.mfm = getFragmentManager();
        this.mMassChooser = new DialogMassChooser();

        mRenderer = (CameraRenderer)findViewById(R.id.renderer_view);
        massInfoTextArea = (TextView)findViewById(R.id.mass_info);
        this.addListeners();

        Log.i("darkcam activity", "Activity onStart");
        super.onStart();
        this.enableButtons();
        Log.i("darkcam activity", "Activity onStart finished");
    }


    @Override
    public void onPause(){
        Log.i("darkcam activity", "Activity onPause");
        super.onPause();
        this.hideMassChooserDialog();
        if(this.progress != null)
            if(this.progress.isShowing())
                this.progress.dismiss();
        this.enableButtons();
        if(mRenderer != null)
            mRenderer.onDestroy();
        Log.i("darkcam activity", "Activity onPause finished");
    }

    @Override
    public void onResume(){

        Log.i("darkcam activity", "Activity onResume");
        super.onResume();
        mRenderer.onResume();
        this.enableButtons();
        Log.i("darkcam activity", "Activity onResume finished");
    }


    public void disableButtons(){
        this.buttonOne.setEnabled(false);
        this.gearButton.setEnabled(false);
        this.massInfoTextArea.setEnabled(false);
        this.hideMassChooserDialog();
    }

    public void enableButtons(){
        this.buttonOne.setEnabled(true);
        this.gearButton.setEnabled(true);
        this.massInfoTextArea.setEnabled(true);
    }

    ImageButton buttonOne = null;
    ImageButton gearButton = null;
    TextView massInfoTextArea = null;

    private void addListeners(){

        this.buttonOne = (ImageButton) findViewById(R.id.button);
        this.gearButton = (ImageButton) findViewById(R.id.gearbutton);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                Context context = getApplicationContext();
                //CharSequence text = "Picture order dispatched!";
                //int duration = Toast.LENGTH_SHORT;

                //Toast toast = Toast.makeText(context, text, duration);
                //toast.show();

                CamActivity.this.disableButtons();
                final ProgressDialog progress = new ProgressDialog(CamActivity.this);
                CamActivity.this.progress = progress;
                progress.setTitle(CamActivity.this.getTextInCurrentLang("progress_title"));
                progress.setMessage(CamActivity.this.getTextInCurrentLang("progress_msg"));
                progress.setCancelable(false);
                progress.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CamActivity.this.mRenderer.requestBigPic();
                    }
                }).start();//fire and forget, do not wait for the completion here, like in run()

            }
        });

        this.massInfoTextArea.setOnClickListener(new TextView.OnClickListener(){
            public void onClick(View v) {
                CamActivity.this.showMassChooserDialog();
            }
        });

        gearButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                //CamActivity.this.showMassChooserDialog();
            }
        });

        this.mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        this.mRenderer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });

    }

    FragmentManager mfm;
    DialogMassChooser mMassChooser;
    private void showMassChooserDialog() {
        if(this.mMassChooser != null)
            this.mMassChooser.show(mfm, "fragment_edit_name");
    }

    private void hideMassChooserDialog() {
        if(this.mMassChooser != null)
            if(this.mMassChooser.isVisible())
                this.mMassChooser.dismiss();
    }

    ScaleGestureDetector mScaleDetector = null;
    private double mScaleFactor = 1.0f;

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            // Intentionally empty
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 2.0f));

            mRenderer.invalidate();
            Log.i("darkcam ", "Scale changed : " + String.valueOf(mScaleFactor));
            mRenderer.updateBlackHoleScale(mScaleFactor);
            return true;
        }
    }
}
