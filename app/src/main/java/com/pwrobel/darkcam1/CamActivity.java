package com.pwrobel.darkcam1;

//MobileTuxedo.com - give credit for the pinch icon

//http://simpleicon.com/wp-content/uploads/gear-1.png - gear icon - probably free licence

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CamActivity extends Activity implements MassSelectedListener {

    @Override
    public void onMassSelected(double selector) {
        this.mScaleFactor = selector;
        mRenderer.invalidate();
        Log.i("darkcam ", "Scale changed from dialog: " + String.valueOf(mScaleFactor));
        mRenderer.updateBlackHoleScale(mScaleFactor);
    }



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
        Log.i("darkcam activity", "Activity onStart");
        super.onStart();
        Log.i("darkcam activity", "Activity onStart finished");
    }


    @Override
    public void onPause(){
        Log.i("darkcam activity", "Activity onPause");
        super.onPause();
        this.hideMassChooserDialog();
        this.hideZoomInfoDialog();
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

        this.mScaleFactor = 1.0f;

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
        this.mMassChooser.setLanguagesInfo(this.languages, this.current_lang);
        this.mMassChooser.setFactorScaleRange(this.mScaleFactor_min, this.mScaleFactor_max);
        this.mMassChooser.setFactorScale(this.mScaleFactor);

        mRenderer = (CameraRenderer)findViewById(R.id.renderer_view);
        this.mMassChooser.setDefault_phys_ratio(mRenderer.getDefault_phys_ratio());

        this.zoomInfoWin = new ZoomWindowInfo();
        this.zoomInfoWin.setLanguagesInfo(this.languages, this.current_lang);

        massInfoTextArea = (ImageButton)findViewById(R.id.mass_info);


        this.addListeners();
        this.enableButtons();

        Log.i("darkcam activity", "Activity onResume");
        super.onResume();
        mRenderer.onResume();
        this.enableButtons();
        Log.i("darkcam activity", "Activity onResume finished");
    }


    private int which_tab_about_selected=0;

    public void disableButtons(){
        this.buttonOne.setEnabled(false);
        this.gearButton.setEnabled(false);
        this.massInfoTextArea.setEnabled(false);
        this.hideMassChooserDialog();
        this.hideZoomInfoDialog();
    }

    public void enableButtons(){
        this.buttonOne.setEnabled(true);
        this.gearButton.setEnabled(true);
        this.massInfoTextArea.setEnabled(true);
    }

    ImageButton buttonOne = null;
    ImageButton gearButton = null;
    ImageButton massInfoTextArea = null;

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
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                int rotation = getWindowManager().getDefaultDisplay()
                        .getRotation();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                CamActivity.this.disableButtons();
                final ProgressDialog progress = new ProgressDialog(CamActivity.this);
                CamActivity.this.progress = progress;
                progress.setTitle(CamActivity.this.getTextInCurrentLang("progress_title")+" R"+rotation);
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

        this.massInfoTextArea.setOnClickListener(new TextView.OnClickListener() {
            public void onClick(View v) {
                CamActivity.this.showMassChooserDialog();
                //CamActivity.this.showZoomInfoDialog();
            }
        });

        gearButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                //CamActivity.this.openOptionsMenu();
                CamActivity.this.showPopUpMenu();
                //CamActivity.this.mySettingsMenu.performIdentifierAction(R.id.me)
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
    ZoomWindowInfo zoomInfoWin;
    private void showMassChooserDialog() {
        if(this.mMassChooser != null) {
            this.mMassChooser.show(mfm, "fragment_edit_name");
            this.mMassChooser.setFactorScaleRange(this.mScaleFactor_min, this.mScaleFactor_max);
            this.mMassChooser.setFactorScale(this.mScaleFactor);
        }
    }

    private void hideZoomInfoDialog() {
        if(this.zoomInfoWin != null){
            if(this.zoomInfoWin.isVisible())
                this.zoomInfoWin.dismiss();
            this.zoomInfoWin = null;
        }
    }

    private void showZoomInfoDialog() {
        this.zoomInfoWin = new ZoomWindowInfo();
        this.zoomInfoWin.setLanguagesInfo(this.languages, this.current_lang);
        this.zoomInfoWin.setWhichTabSelectedByDefault(this.which_tab_about_selected);
        this.zoomInfoWin.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        this.zoomInfoWin.show(mfm, "fragment_edit_name2");
    }

    private void hideMassChooserDialog() {
        if(this.mMassChooser != null)
            if(this.mMassChooser.isVisible())
                this.mMassChooser.dismiss();
    }


    private PopupMenu mySettingsMenu = null;
    // Initiating Menu xml (menu_settings.xml) - this is called by the android
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(this.gearButton == null)
            return false;
        this.showPopUpMenu();
        return true;
    }

    private void showPopUpMenu(){
        if(this.gearButton == null)
            return;
        this.mySettingsMenu = new PopupMenu(CamActivity.this, this.gearButton);;
        this.mySettingsMenu.inflate(R.menu.menu_settings);

        this.mySettingsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.menu_aboutapp:
                        //Toast.makeText(CamActivity.this, "AboutApp is Selected", Toast.LENGTH_SHORT).show();
                        CamActivity.this.which_tab_about_selected = 1;
                        CamActivity.this.showZoomInfoDialog();
                        return true;

                    case R.id.menu_preferences:
                        //Toast.makeText(CamActivity.this, "Preferences is Selected", Toast.LENGTH_SHORT).show();
                        CamActivity.this.which_tab_about_selected = 0;
                        CamActivity.this.showZoomInfoDialog();
                        return true;
                }
                return false;
            }
        });

        MenuItem menu_preferences = this.mySettingsMenu.getMenu().findItem(R.id.menu_preferences);
        MenuItem menu_aboutapp = this.mySettingsMenu.getMenu().findItem(R.id.menu_aboutapp);

        if(menu_preferences != null)
            menu_preferences.setTitle(this.getTextInCurrentLang("menu_preferences"));
        if(menu_aboutapp != null)
            menu_aboutapp.setTitle(this.getTextInCurrentLang("menu_aboutapp"));

        this.mySettingsMenu.show();
    }


    ScaleGestureDetector mScaleDetector = null;
    private double mScaleFactor;

    private double mScaleFactor_min = 0.5f;
    private double mScaleFactor_max = 2.0f;

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
            mScaleFactor = Math.max(CamActivity.this.mScaleFactor_min, Math.min(mScaleFactor, CamActivity.this.mScaleFactor_max));
            if(CamActivity.this.mMassChooser != null){
                CamActivity.this.mMassChooser.setFactorScale(CamActivity.this.mScaleFactor);
            }
            mRenderer.invalidate();
            Log.i("darkcam ", "Scale changed : " + String.valueOf(mScaleFactor));
            mRenderer.updateBlackHoleScale(mScaleFactor);
            return true;
        }
    }
}
